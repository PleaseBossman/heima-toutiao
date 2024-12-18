    package com.heima.article.stream;

    import com.alibaba.fastjson.JSON;
    import com.heima.common.constants.HotArticleConstants;
    import com.heima.model.mess.ArticleVisitStreamMess;
    import com.heima.model.mess.UpdateArticleMess;
    import lombok.extern.slf4j.Slf4j;
    import org.apache.commons.lang.StringUtils;
    import org.apache.kafka.streams.KeyValue;
    import org.apache.kafka.streams.StreamsBuilder;
    import org.apache.kafka.streams.kstream.*;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.web.servlet.HandlerInterceptor;

    import java.time.Duration;

    @Configuration  // 表示这是一个 Spring 配置类
    @Slf4j          // 使用 Lombok 提供的日志工具，便于在类中直接使用 log 对象记录日志
    public class HotArticleStreamHandler {

        // 定义一个 Kafka Streams 的处理方法，返回类型为 KStream
        @Bean
        public KStream<String, String> kStream(StreamsBuilder streamsBuilder) {
            // 接收来自指定主题的消息流
            //这一行表示从指定的 Kafka 主题（HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC）中读取数据。因此，只要这个主题有新的消息产生，定义的流处理逻辑就会被自动执行。
            KStream<String, String> stream = streamsBuilder.stream(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC);

            /**
             * 聚合操作使用了时间窗口（10 秒）。在每个时间窗口内，对特定文章的所有操作类型进行统计，并返回聚合结果。
             */
            // 对消息进行流式映射处理，将消息转换为键值对
            stream.map((key, value) -> {
                        // 将消息的内容从 JSON 字符串转换为 UpdateArticleMess 对象
                        UpdateArticleMess mess = JSON.parseObject(value, UpdateArticleMess.class);
                        // 返回一个新的键值对，键是文章 ID，值是类型与增量的组合字符串
                        return new KeyValue<>(mess.getArticleId().toString(), mess.getType().name() + ":" + mess.getAdd());
                    })
                    // 根据键（文章 ID）进行分组
                    .groupBy((key, value) -> key)
                    // 设置一个时间窗口，窗口大小为 10 秒
                    .windowedBy(TimeWindows.of(Duration.ofSeconds(10)))
                    // 聚合计算逻辑，使用 String 类型进行初始化和聚合
                    .aggregate(
                            new Initializer<String>() {
                                @Override
                                public String apply() {
                                    // 初始化聚合值，包含收藏、评论、点赞、浏览的数据
                                    return "COLLECTION:0,COMMENT:0,LIKES:0,VIEWS:0";
                                }
                            },
                            new Aggregator<String, String, String>() {
                                @Override
                                public String apply(String key, String value, String aggValue) {
                                    // 如果传入的值为空，则直接返回当前的聚合值
                                    if (StringUtils.isBlank(value)) {
                                        return aggValue;
                                    }

                                    // 将当前的聚合值拆分为各个数据类型和其对应的值
                                    String[] aggAry = aggValue.split(",");
                                    int col = 0, com = 0, lik = 0, vie = 0;
                                    for (String agg : aggAry) {
                                        String[] split = agg.split(":");
                                        // 根据数据类型（COLLECTION、COMMENT、LIKES、VIEWS）提取当前聚合值
                                        switch (UpdateArticleMess.UpdateArticleType.valueOf(split[0])) {
                                            case LIKES:
                                                lik = Integer.parseInt(split[1]);
                                                break;
                                            case COLLECTION:
                                                col = Integer.parseInt(split[1]);
                                                break;
                                            case COMMENT:
                                                com = Integer.parseInt(split[1]);
                                                break;
                                            case VIEWS:
                                                vie = Integer.parseInt(split[1]);
                                                break;
                                        }
                                    }

                                    // 将新的增量值拆分为类型和增量
                                    String[] valAry = value.split(":");
                                    switch (UpdateArticleMess.UpdateArticleType.valueOf(valAry[0])) {
                                        case LIKES:
                                            lik += Integer.parseInt(valAry[1]);
                                            break;
                                        case COLLECTION:
                                            col += Integer.parseInt(valAry[1]);
                                            break;
                                        case COMMENT:
                                            com += Integer.parseInt(valAry[1]);
                                            break;
                                        case VIEWS:
                                            vie += Integer.parseInt(valAry[1]);
                                            break;
                                    }

                                    // 组合新的聚合结果，返回格式为 "COLLECTION:x,COMMENT:x,LIKES:x,VIEWS:x"
                                    String formatStr = String.format("COLLECTION:%d,COMMENT:%d,LIKES:%d,VIEWS:%d", col, com, lik, vie);
                                    System.out.println("文章的id:" + key);
                                    System.out.println("当前时间窗口内的消息处理结果：" + formatStr);
                                    return formatStr;
                                }
                            },
                            // 将聚合结果存储在名为 "hot-atricle-stream-count-001" 的状态存储中
                            Materialized.as("hot-atricle-stream-count-001")
                    )
                    // 将聚合结果转换为 KStream 流
                    .toStream()
                    // 重新映射消息，将 Windowed<String> 键转换为普通键并格式化消息
                    .map((key, value) -> {
                        // 格式化消息的内容
                        return new KeyValue<>(key.key().toString(), formatObj(key.key().toString(), value));
                    })
                    // 将处理后的结果发送到指定的 Kafka 主题
                    .to(HotArticleConstants.HOT_ARTICLE_INCR_HANDLE_TOPIC);

            // 返回 Kafka 流对象
            return stream;
        }

        // 格式化消息对象，将聚合结果转换为 ArticleVisitStreamMess 对象，并序列化为 JSON 字符串
        private Object formatObj(String articledId, String value) {
            ArticleVisitStreamMess mess = new ArticleVisitStreamMess();
            mess.setArticleId(Long.valueOf(articledId));

            // 拆分聚合结果，更新到 ArticleVisitStreamMess 对象中
            String[] valAry = value.split(",");
            for (String val : valAry) {
                String[] split = val.split(":");
                switch (UpdateArticleMess.UpdateArticleType.valueOf(split[0])) {
                    case LIKES:
                        mess.setLike(Integer.parseInt(split[1]));
                        break;
                    case COLLECTION:
                        mess.setCollect(Integer.parseInt(split[1]));
                        break;
                    case COMMENT:
                        mess.setComment(Integer.parseInt(split[1]));
                        break;
                    case VIEWS:
                        mess.setView(Integer.parseInt(split[1]));
                        break;
                }
            }

            // 记录格式化的聚合结果日志信息
            log.info("聚合消息处理之后的结果为:{}", JSON.toJSONString(mess));

            // 返回 JSON 字符串形式的结果
            return JSON.toJSONString(mess);
        }

    }
