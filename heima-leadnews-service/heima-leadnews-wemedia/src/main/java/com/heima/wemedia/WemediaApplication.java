package com.heima.wemedia;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("com.heima.article.feign")//这是我后面加的，搞了我两天
@MapperScan("com.heima.wemedia.mapper")
//pom文件要导入依赖才能扫描到
//@EnableFeignClients 注解通常用于指定 Spring Cloud 应用程序中 Feign 客户端接口所在的包
//Feign 客户端在 Spring Cloud 应用程序中通常是指那些使用 @FeignClient 注解的接口
@EnableFeignClients(basePackages = "com.heima.apis")//(basePackages = {"com.heima.apis", "com.heima.article.feign"})
@EnableAsync  //开启异步调用
@EnableScheduling
public class WemediaApplication {

    public static void main(String[] args) {
        SpringApplication.run(WemediaApplication.class,args);
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
