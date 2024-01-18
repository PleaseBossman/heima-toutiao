package com.heima.article.controller.v1;

import com.heima.article.service.LoadArticleService;
import com.heima.model.article.dtos.ArticleInfoDto;
import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v1/article/load_article_behavior")
public class LoadArticleBehaviorController {

    @Autowired
    private LoadArticleService loadArticleService;

    @PostMapping
    public ResponseResult LoadArticle(@RequestBody ArticleInfoDto dto){
        return loadArticleService.loadArticleBehavior(dto);
    }
}
