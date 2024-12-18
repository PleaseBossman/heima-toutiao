package com.heima.behavior.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.behavior.service.ApReadBehaviorService;
import com.heima.common.constants.BehaviorConstants;
import com.heima.common.constants.HotArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mess.UpdateArticleMess;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class ApReadBehaviorServiceImpl implements ApReadBehaviorService {

    // 注入缓存服务，用于与 Redis 交互
    @Autowired
    private CacheService cacheService;

    // 注入 Kafka 模板，用于发送消息到 Kafka 主题
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 处理用户的阅读行为
     * @param dto 包含阅读行为信息的 Data Transfer Object
     * @return 响应结果
     */
    @Override
    public ResponseResult readBehavior(ReadBehaviorDto dto) {
        // 1.检查参数，如果 dto 或者文章 ID 为 null，返回参数无效错误
        if (dto == null || dto.getArticleId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 2.检查用户是否登录，从本地线程中获取当前用户信息
        ApUser user = AppThreadLocalUtil.getUser();
        // 如果用户未登录，返回需要登录的错误信息
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // 更新阅读次数
        // 从缓存中读取当前用户对指定文章的阅读行为记录
        String readBehaviorJson = (String) cacheService.hGet(
                BehaviorConstants.READ_BEHAVIOR + dto.getArticleId().toString(), // Redis 哈希表的主键，使用文章 ID 生成
                user.getId().toString() // 用户 ID 作为哈希键
        );
        // 如果缓存中存在该阅读记录，则解析 JSON 数据，并更新阅读次数
        if (StringUtils.isNotBlank(readBehaviorJson)) {
            // 解析缓存中的阅读行为数据为 ReadBehaviorDto 对象
            ReadBehaviorDto readBehaviorDto = JSON.parseObject(readBehaviorJson, ReadBehaviorDto.class);
            // 更新阅读次数，累加新的阅读计数
            dto.setCount((short) (readBehaviorDto.getCount() + dto.getCount()));
        }

        // 保存当前阅读行为到缓存中
        log.info("保存当前key:{} {} {}", dto.getArticleId(), user.getId(), dto); // 打印日志信息
        cacheService.hPut(
                BehaviorConstants.READ_BEHAVIOR + dto.getArticleId().toString(), // 使用文章 ID 作为主键
                user.getId().toString(), // 用户 ID 作为哈希键
                JSON.toJSONString(dto) // 将当前阅读行为转为 JSON 字符串保存
        );

        // 更新文章的阅读次数
        // 创建一个 UpdateArticleMess 消息对象，用于更新文章的阅读数据
        UpdateArticleMess mess = new UpdateArticleMess();
        mess.setArticleId(dto.getArticleId()); // 设置文章 ID
        mess.setType(UpdateArticleMess.UpdateArticleType.VIEWS); // 设置更新类型为 "阅读次数"
        mess.setAdd(1); // 增加阅读次数 1

        // 将更新信息发送到指定的 Kafka 主题，用于更新热点文章的评分
        kafkaTemplate.send(
                HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC, // Kafka 主题名称
                JSON.toJSONString(mess) // 将更新信息转换为 JSON 字符串并发送
        );

        // 返回成功的响应结果
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
