package com.heima.schedule;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@MapperScan("com.heima.schedule.mapper")
@EnableScheduling//寻找那些用 @Scheduled 注解标记的方法，并按照指定的计划定时执行这些方法
public class ScheduleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScheduleApplication.class,args);
    }

    /**第三方bean对象
     * mybatis-plus乐观锁支持
     * 乐观锁的核心思路是：“先假设没人和我抢着修改这份数据，等我要提交的时候再检查一下，如果有冲突我再处理。”
     * 使用乐观锁可以避免数据库中的行锁，从而让大量用户可以同时查询库存。在扣减库存时，乐观锁可以检查是否有其他人已修改过库存，保证扣减数量准确。
     * @return
     */
    @Bean
    public MybatisPlusInterceptor optimisticLockerInterceptor(){
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
