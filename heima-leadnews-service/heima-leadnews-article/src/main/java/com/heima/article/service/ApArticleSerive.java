package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;

import java.util.Map;

public interface ApArticleSerive extends IService<ApArticle> {
    /**
     * 加载文章列表
     * @param dto
     * @param type
     * @return
     */
    public ResponseResult load(ArticleHomeDto dto, Short type);

    /**
     * 加载文章列表
     * @param dto
     * @param type
     * @return
     */
    public ResponseResult load2(ArticleHomeDto dto, Short type, Boolean firstPage);
    /**
     *  保存app端相关文章
     * @param dto
     * @return
     */
    public ResponseResult saveArticle(ArticleDto dto);


}
