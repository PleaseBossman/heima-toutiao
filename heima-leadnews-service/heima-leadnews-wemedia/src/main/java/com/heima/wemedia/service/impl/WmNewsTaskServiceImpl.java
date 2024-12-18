package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.schedule.IScheduleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.TaskTypeEnum;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.utils.common.ProtostuffUtil;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsService;
import com.heima.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.util.Date;

@Slf4j
@Service
public class WmNewsTaskServiceImpl implements WmNewsTaskService{

    @Autowired
    private IScheduleClient iScheduleClient;
    /**
     * 添加任务到延迟队列中
     *
     * @param id          文章的id
     * @param publishTime 发布的时间  可以做为任务的执行时间
     */
    @Override
    public void addNewToTAsk(Integer id, Date publishTime) {
        log.info("添加任务到延迟服务中----begin");
        Task task = new Task();
        task.setExecuteTime(publishTime.getTime());
        task.setTaskType(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType());
        task.setPriority(TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        WmNews wmNews = new WmNews();
        wmNews.setId(id);
        task.setParameters(ProtostuffUtil.serialize(wmNews));//serialize(wmNews) 表示对 wmNews 对象进行序列化，将其转换成一个字节数组
        iScheduleClient.addTask(task);
        log.info("添加任务到延迟服务中----end");
    }

    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;


    /**
     * 消费延迟队列数据
     */
    @Override
    @Scheduled(fixedDelay = 1000)//表示任务执行完毕后，等待 1000 毫秒（1 秒）再重新执行。
    public void scanNewsByTask() {
        // 日志输出：开始执行文章审核任务的消费
        log.info("文章审核---消费任务执行---begin---");

        // 调用 `iScheduleClient` 客户端的 `poll` 方法，从延迟队列中获取任务。传入的参数是任务类型和优先级。
        // `TaskTypeEnum.NEWS_SCAN_TIME.getTaskType()`：获取任务类型（如文章审核任务类型）
        // `TaskTypeEnum.NEWS_SCAN_TIME.getPriority()`：获取任务的优先级
        ResponseResult responseResult = iScheduleClient.poll(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType(), TaskTypeEnum.NEWS_SCAN_TIME.getPriority());

        // 检查请求结果，如果返回码为 200 且返回数据不为空，则表示成功获取到了任务
        if(responseResult.getCode().equals(200) && responseResult.getData() != null) {
            // 将返回的数据 `responseResult.getData()` 转换为 JSON 字符串，然后再解析为 `Task` 对象
            Task task = JSON.parseObject(JSON.toJSONString(responseResult.getData()), Task.class);

            // 从 `task` 对象中获取序列化的任务参数，并反序列化成 `WmNews` 对象
            // `ProtostuffUtil.deserialize()`：使用 `ProtostuffUtil` 工具类来进行反序列化，将字节数组转换成 `WmNews` 对象
            WmNews wmNews = ProtostuffUtil.deserialize(task.getParameters(), WmNews.class);

            // 调用 `wmNewsAutoScanService` 的 `autoScanWmNews` 方法，传入 `WmNews` 对象的 ID，进行文章的自动审核
            wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        }

        // 日志输出：完成执行文章审核任务的消费
        log.info("文章审核---消费任务执行---end---");
    }


}
