package com.heima.schedule.service.impl;
import com.heima.model.schedule.pojos.Taskinfo;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;

import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskSerive;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class TaskSeriveImpl implements TaskSerive {
    /**
     * 添加延迟任务
     *
     * @param task
     * @return
     */
    @Override
    public long addTask(Task task) {

        boolean success = addTaskToDb(task);
        if(success){
            addTaskToCache(task);
        }

        return task.getTaskId();
    }



    @Autowired
    private CacheService cacheService;

    /**
     * 把任务添加到redis中
     * @param task
     */
    private void addTaskToCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();

        Calendar calendar= Calendar.getInstance();
        calendar.add(Calendar.MINUTE ,5);
        long nextScheduleTime = calendar.getTimeInMillis();


        if(task.getExecuteTime() <= System.currentTimeMillis()){
            cacheService.lLeftPush(ScheduleConstants.TOPIC + key, JSON.toJSONString(task));
        }else if(task.getExecuteTime() <= nextScheduleTime){
            cacheService.zAdd(ScheduleConstants.FUTURE + key, JSON.toJSONString(task), task.getExecuteTime());
        }
    }

    @Autowired
    private TaskinfoMapper taskinfoMapper;

    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;

    private boolean addTaskToDb(Task task) {
        Boolean flag = false;
        try {
            Taskinfo taskinfo = new Taskinfo();
            BeanUtils.copyProperties(task,taskinfo);
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoMapper.insert(taskinfo);

            task.setTaskId(taskinfo.getTaskId());

            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            BeanUtils.copyProperties(taskinfo,taskinfoLogs);
            taskinfoLogs.setVersion(1);
            taskinfoLogs.setVersion(ScheduleConstants.SCHEDULED);
            taskinfoLogsMapper.insert(taskinfoLogs);
            flag = true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 取消任务
     *
     * @param taskId
     * @return
     */
    @Override
    public boolean cancelTask(long taskId) {
        boolean flag = false;
        Task task = updateDb(taskId,ScheduleConstants.CANCELLED);

        if(task != null){
            removeTaskFromCache(task);
            flag = true;
        }

        return flag;
    }

    /**
     * 根据类型优先级拉取任务
     *
     * @param type
     * @param priority
     * @return
     */
    @Override
    public Task poll(int type, int priority) {
        Task task = null;
        try {
            String key = type + "_" + priority;
            String task_json = cacheService.lRightPop(ScheduleConstants.TOPIC + key);
            if(StringUtils.isNotBlank(task_json)){
                task = JSON.parseObject(task_json, Task.class);
                updateDb(task.getTaskId(),ScheduleConstants.EXECUTED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return task;
    }

    /**
     * 删除redis中的数据
     * @param task
     */
    private void removeTaskFromCache(Task task) {

        String key = task.getTaskType() + "_" + task.getPriority();
        if(task.getExecuteTime() <= System.currentTimeMillis()){
            cacheService.lRemove(ScheduleConstants.TOPIC + key ,0,JSON.toJSONString(task));
        }else {
            cacheService.zRemove(ScheduleConstants.FUTURE,JSON.toJSONString(task));
        }
    }

    /**
     *删除任务，更新日志
     * @param taskId
     * @param status
     * @return
     */
    private Task updateDb(long taskId, int status) {
        Task task = null;
        try {
            taskinfoMapper.deleteById(taskId);

            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status);
            taskinfoLogsMapper.updateById(taskinfoLogs);

            task = new Task();
            BeanUtils.copyProperties(taskinfoLogs,task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
        } catch (BeansException e) {
            e.printStackTrace();
        }
        return task;
    }

//这行代码使用Spring的@Scheduled注解来设置一个定时任务，使用Cron表达式定义了执行时间。
//    Scheduled注解会让方法在程序启动时自动运行，无需显式调用该方法。
    @Scheduled(cron = "0 */1 * * * ?")
    public void refresh(){
//尝试获取一个分布式锁，锁的名称是 "FUTURE_TASK_SYNC"，锁的有效期是 30 秒（1000 * 30 毫秒）
        String token = cacheService.tryLock("FUTURE_TASK_SYNC", 1000 * 30);
//检查是否成功获取锁，如果token不为空，表示锁获取成功，继续执行任务。
        if(StringUtils.isNotBlank(token)) {
            log.info("未来数据定时刷新---定时任务");

            System.out.println(System.currentTimeMillis() / 1000 + "执行了定时任务");
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
            for (String futureKey : futureKeys) {
                String topicKey = ScheduleConstants.TOPIC + futureKey.split(ScheduleConstants.FUTURE)[1];

                Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());
                if (!tasks.isEmpty()) {
                    cacheService.refreshWithPipeline(futureKey, topicKey, tasks);
                    System.out.println("成功的将" + futureKey + "下的当前需要执行的任务数据刷新到" + topicKey + "下");

                }
            }
        }
    }

    // 使用 @Scheduled 注解定义一个定时任务
    @Scheduled(cron = "0 */5 * * * ?")
    @PostConstruct//表示该方法会在构造函数执行之后、对象被使用之前被调用。这个注解通常用于初始化操作，特别是当你需要在对象创建后做一些额外的初始化工作时
    public void reloadData() {
        // 1. 清除缓存中的旧数据
        clearCache();
        log.info("数据库数据同步到缓存");

        // 2. 获取当前时间，并将时间增加 5 分钟，
        // 表示查询未来 5 分钟内的任务这段代码的作用是将当前日期和时间增加5分钟。例如，如果当前时间是下午3:30，执行这段代码后，calendar对象将表示的时间将是下午3:35
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);

        // 3. 从数据库中查询所有执行时间小于未来 5 分钟的任务
        List<Taskinfo> allTasks = taskinfoMapper.selectList(
                Wrappers.<Taskinfo>lambdaQuery().lt(Taskinfo::getExecuteTime, calendar.getTime())
        );

        // 4. 如果查询到的任务列表不为空，则将任务同步到缓存中
        if (allTasks != null && allTasks.size() != 0) {
            for (Taskinfo taskinfo : allTasks) {
                // 5. 创建一个新的任务对象，将从数据库中查询到的 Taskinfo 对象属性复制到 Task 对象
                Task task = new Task();
                BeanUtils.copyProperties(taskinfo, task);

                // 6. 设置任务的执行时间为时间戳格式（毫秒）
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());

                // 7. 将转换后的任务添加到缓存中
                addTaskToCache(task);
            }
        }
    }

    // 清除缓存中的旧数据
    private void clearCache() {
        // 1. 扫描 Redis 缓存中未来数据集合的所有键（以 ScheduleConstants.FUTURE 开头的键）
        Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");

        // 2. 扫描 Redis 缓存中当前消费者队列的所有键（以 ScheduleConstants.TOPIC 开头的键）
        Set<String> topicKeys = cacheService.scan(ScheduleConstants.TOPIC + "*");

        // 3. 删除 Redis 缓存中扫描到的未来数据集合和消费者队列的所有键
        cacheService.delete(futureKeys);
        cacheService.delete(topicKeys);
    }

}
