package com.heima.search.service.Impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.pojos.ApAssociateWords;
import com.heima.search.service.ApAssociateWordsService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class ApAssociateWordsServiceImpl implements ApAssociateWordsService {

    @Autowired
    private MongoTemplate mongoTemplate;
    /**
     * 联想词
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findAssociate(UserSearchDto dto) {
        if(StringUtils.isBlank(dto.getSearchWords())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        if(dto.getPageSize() >20){
            dto.setPageSize(20);
        }

        /**
         * Query.query(...): 这是用于构建一个 MongoDB 查询对象的方法。Query 是 Spring Data MongoDB 提供的一个类，用于封装查询条件。
         * Criteria.where("associateWords"): 这里的 Criteria 用于构造查询条件。在这个例子中，它指定了查询的字段是 associateWords，这个字段通常代表数据库中的某个列。
         * .regex(...): regex 方法用于基于正则表达式进行查询。这里是对 associateWords 字段进行模糊匹配。
         * 正则表达式：".*?\\" + dto.getSearchWords() + ".*"
         * .*?：匹配任意字符（非贪婪模式），表示可以在匹配的文本前后有任意字符。
         * dto.getSearchWords()：这是用户输入的搜索关键字。dto.getSearchWords() 返回的是一个字符串，代表用户想要查找的内容。
         * "\\：转义字符，用于在正则表达式中表示转义。
         * 整个正则表达式会匹配包含 dto.getSearchWords() 所指定的字符串的 associateWords 字段。
         */
        /**
         * ApAssociateWords.class: 这是查询结果的目标类。表示查询结果会被映射到 ApAssociateWords 这个类中，每一条匹配的数据会被转换成一个 ApAssociateWords 对象。
         */
        Query query = Query.query(Criteria.where("associateWords").regex(".*?\\" + dto.getSearchWords() + ".*"));
        List<ApAssociateWords> apAssociateWordsList = mongoTemplate.find(query, ApAssociateWords.class);
        return ResponseResult.okResult(apAssociateWordsList);
    }
}
