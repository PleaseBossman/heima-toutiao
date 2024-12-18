package com.heima.article.service;

import com.heima.model.article.dtos.ArticleInfoDto;
import com.heima.model.common.dtos.ResponseResult;

public interface LoadArticleService {
    ResponseResult loadArticleBehavior(ArticleInfoDto dto);
}
