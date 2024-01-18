package com.heima.article.service.Impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.HotArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class HotArticleServiceImpl implements HotArticleService {

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private IWemediaClient wemediaClient;

    @Autowired
    private CacheService cacheService;
    /**
     * 计算热点文章
     */
    @Override
    public void computHotArticle() {
        Date date = DateTime.now().minusYears(5).toDate();
        List<ApArticle> articleListByLast5Days = apArticleMapper.findArticleListByLast5Days(date);
        List<HotArticleVo> hotArticleVo = computHotArticle(articleListByLast5Days);

        cacheTagToRedis(hotArticleVo);
    }

    private void cacheTagToRedis(List<HotArticleVo> hotArticleVoList) {
        ResponseResult responseResult = wemediaClient.getChannels();
        if(responseResult.getCode().equals(200)){
            Object data = responseResult.getData();
            String s = JSON.toJSONString(data);
            List<WmChannel> wmChannels = JSON.parseArray(s, WmChannel.class);
            if(wmChannels!=null && wmChannels.size()!=0){
                for (WmChannel wmChannel : wmChannels) {
                    List<HotArticleVo> hotArticleVos = hotArticleVoList.stream().filter(x -> x.getChannelId().equals(wmChannel.getId())).collect(Collectors.toList());

                    //文章排序取30条高的文章存入redis
                    hotArticleVos = hotArticleVos.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());
                    if(hotArticleVos.size() > 30){
                        hotArticleVos = hotArticleVos.subList(0,30);
                    }
                    cacheService.set(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + wmChannel.getId(),JSON.toJSONString(hotArticleVos));

                }
            }
        }
        //文章排序取30条高的文章存入redis
        hotArticleVoList = hotArticleVoList.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());
        if(hotArticleVoList.size() > 30){
            hotArticleVoList = hotArticleVoList.subList(0,30);
        }
        cacheService.set(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + ArticleConstants.DEFAULT_TAG,JSON.toJSONString(hotArticleVoList));


    }

    /**
     * 计算文章分值
     * @param articleListByLast5Days
     * @return
     */
    private List<HotArticleVo> computHotArticle(List<ApArticle> articleListByLast5Days) {

        List<HotArticleVo> hotArticleVos = new ArrayList<>();
        if(articleListByLast5Days != null && articleListByLast5Days.size()!=0){
            for (ApArticle apArticle : articleListByLast5Days) {
                HotArticleVo hot = new HotArticleVo();
                BeanUtils.copyProperties(apArticle,hot);
                Integer score = computeScore(apArticle);
                hot.setScore(score);
                hotArticleVos.add(hot);
            }
        }
        return hotArticleVos;
    }

    /**
     * 计算文章具体分值
     * @param apArticle
     * @return
     */
    private Integer computeScore(ApArticle apArticle) {
        Integer score = 0;
        if(apArticle.getLikes()!=null){
            score += apArticle.getLikes() * ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }
        if(apArticle.getViews()!=null){
            score += apArticle.getViews() ;
        }
        if(apArticle.getComment()!=null){
            score += apArticle.getComment() * ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
        if(apArticle.getCollection()!=null){
            score += apArticle.getCollection() * ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }
        return score;
    }
}
