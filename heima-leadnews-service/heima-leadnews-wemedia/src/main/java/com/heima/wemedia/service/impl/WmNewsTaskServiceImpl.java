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
        task.setParameters(ProtostuffUtil.serialize(wmNews));
        iScheduleClient.addTask(task);
        log.info("添加任务到延迟服务中----end");
    }

    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;


    /**
     * 消费延迟队列数据
     */
    @Override
    @Scheduled(fixedDelay = 1000)
    public void scanNewsByTask() {
        log.info("文章审核---消费任务执行---begin---");
        ResponseResult responseResult = iScheduleClient.poll(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType(), TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        if(responseResult.getCode().equals(200) &&responseResult.getData()!=null){
            Task task = JSON.parseObject(JSON.toJSONString(responseResult.getData()),Task.class);
            WmNews wmNews = ProtostuffUtil.deserialize(task.getParameters(),WmNews.class);
            wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        }
        log.info("文章审核---消费任务执行---end---");
    }


}
