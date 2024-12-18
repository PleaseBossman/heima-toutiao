package com.heima.apis.article;

import com.heima.apis.article.fallback.IArticleClientFallback;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;

//name 和 value 是可以互换使用的属性，功能完全一致。
// 实际上，value 是 @FeignClient 注解中 name 属性的一个别名，两者的作用相同，都是指定要调用的服务名称。
//@FeignClient 注解会将接口注册为 Spring 容器中的一个 bean@FeignClient(value = "leadnews-article",fallback = IArticleClientFallback.class)

//调用接口远程调用，调用其他一些工具类方法
//leadnews-article是注册在服务发现工具（如Nacos）中的一个微服务的名称
@FeignClient(value = "leadnews-article",fallback = IArticleClientFallback.class)
public interface IArticleClient {

    @PostMapping("/api/v1/article/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto dto) ;
}