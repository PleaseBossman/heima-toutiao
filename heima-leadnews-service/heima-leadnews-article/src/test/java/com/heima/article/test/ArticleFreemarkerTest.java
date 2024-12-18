package com.heima.article.test;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.gson.JsonArray;
import com.heima.article.ArticleApplication;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleSerive;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成静态 HTML 文件并将其上传到 MinIO 文件存储服务的功能
 * 同时将生成的 URL 存储到数据库中
 */
@SpringBootTest(classes = ArticleApplication.class)//用于在测试中加载 Spring 应用程序上下文，classes = ArticleApplication.class 指定加载 ArticleApplication 作为启动类
@RunWith(SpringRunner.class)//在使用 JUnit 4 进行测试时，这个注解允许 Spring 提供测试支持。
public class ArticleFreemarkerTest {

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private ApArticleSerive apArticleSerive;
    @Autowired
    private Configuration configuration;//Freemarker 模板配置对象，用于生成 HTML。
    @Autowired
    private ApArticleMapper apArticleMapper;
    @Test
    public void createStaticUrlTest() throws Exception {
        //1.获取文章内容
        ApArticleContent apArticleContent = apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, 1302977178887004162L));
        if(apArticleContent != null && StringUtils.isNotBlank(apArticleContent.getContent())){
            //2.文章内容通过freemarker生成html文件
            StringWriter out = new StringWriter();//用于存储生成的 HTML 字符串
            Template template = configuration.getTemplate("article.ftl");

            Map<String, Object> params = new HashMap<>();
            params.put("content", JSONArray.parseArray(apArticleContent.getContent()));

            template.process(params, out);//将 params 数据应用到模板中，并将输出写入 out
            InputStream is = new ByteArrayInputStream(out.toString().getBytes());//将 out 转换为输入流 is，以便后续上传

            //3.把html文件上传到minio中
            String path = fileStorageService.uploadHtmlFile("", apArticleContent.getArticleId() + ".html", is);

            //4.修改ap_article表，保存static_url字段
            ApArticle article = new ApArticle();
            article.setId(apArticleContent.getArticleId());
            article.setStaticUrl(path);
            apArticleMapper.updateById(article);

        }
    }

}
