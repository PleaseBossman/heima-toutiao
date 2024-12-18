package com.heima.behavior.service.impl;

// 引入需要的依赖
import com.alibaba.fastjson.JSON;
import com.heima.behavior.service.ApLikesBehaviorService;
import com.heima.common.constants.BehaviorConstants;
import com.heima.common.constants.HotArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.behavior.dtos.LikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mess.UpdateArticleMess;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 将此类声明为一个 Spring 服务，并开启事务管理
@Service
@Transactional
@Slf4j  // 用于日志记录
public class ApLikesBehaviorServiceImpl implements ApLikesBehaviorService {

    // 使用 @Autowired 自动注入 CacheService（Redis 缓存服务）
    @Autowired
    private CacheService cacheService;

    // 使用 @Autowired 自动注入 KafkaTemplate（Kafka 消息模板）
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    /**
     * 实现点赞功能
     * @param dto 点赞行为的数据传输对象
     * @return 响应结果
     */
    @Override
    public ResponseResult like(LikesBehaviorDto dto) {

        // 1.检查参数：如果传入的参数为空，或者参数不合法，返回参数错误的响应结果
        if (dto == null || dto.getArticleId() == null || checkParam(dto)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 2.是否登录：获取当前用户，如果用户未登录，返回需要登录的响应结果
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // 构建 UpdateArticleMess 对象，用于更新文章的点赞消息
        UpdateArticleMess mess = new UpdateArticleMess();
        mess.setArticleId(dto.getArticleId());
        mess.setType(UpdateArticleMess.UpdateArticleType.LIKES);

        // 3.点赞保存数据
        if (dto.getOperation() == 0) { // 如果操作类型为 0（表示点赞）
            // 从 Redis 中查询是否已存在当前用户对该文章的点赞记录
            Object obj = cacheService.hGet(BehaviorConstants.LIKE_BEHAVIOR + dto.getArticleId().toString(), user.getId().toString());
            if (obj != null) {
                // 如果已存在点赞记录，返回参数错误，提示“已点赞”
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "已点赞");
            }
            // 保存点赞行为到 Redis 缓存中
            log.info("保存当前key:{} ,{}, {}", dto.getArticleId(), user.getId(), dto);
            cacheService.hPut(BehaviorConstants.LIKE_BEHAVIOR + dto.getArticleId().toString(), user.getId().toString(), JSON.toJSONString(dto));
            mess.setAdd(1);  // 设置点赞增加量为 1
        } else {
            // 如果操作类型不为 0（表示取消点赞）
            log.info("删除当前key:{}, {}", dto.getArticleId(), user.getId());
            // 删除 Redis 中的点赞记录
            cacheService.hDelete(BehaviorConstants.LIKE_BEHAVIOR + dto.getArticleId().toString(), user.getId().toString());
            mess.setAdd(-1);  // 设置点赞增加量为 -1（减少）
        }

        // 发送点赞更新消息到 Kafka 中的指定主题（如热点文章的评分更新）
        //要发送一个 Java 对象（例如 POJO），通常需要使用 JSON 序列化器
        kafkaTemplate.send(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC, JSON.toJSONString(mess));
        // 返回成功的响应结果
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 检查参数是否合法
     * @param dto 点赞行为的数据传输对象
     * @return 如果参数非法，返回 true；否则返回 false
     */
    private boolean checkParam(LikesBehaviorDto dto) {
        // 如果 dto 中的类型不在 0 到 2 之间 或者 操作类型不在 0 和 1 之间，则返回 true（参数不合法）
        if (dto.getType() > 2 || dto.getType() < 0 || dto.getOperation() > 1 || dto.getOperation() < 0) {
            return true;
        }
        // 否则返回 false（参数合法）
        return false;
    }
}
