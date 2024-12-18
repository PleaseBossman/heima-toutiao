package com.heima.wemedia.service;

import com.heima.model.wemedia.pojos.WmNews;
import com.heima.wemedia.WemediaApplication;
import com.heima.wemedia.mapper.WmNewsMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class WmNewsAutoScanServiceTest {

    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Autowired
    private WmNewsMapper wmNewsMapper;

    @Test
    public void autoScanWmNews() {
            wmNewsAutoScanService.autoScanWmNews(6232);
    }

    @Test
    public void autoScanWmNews1() {
        WmNews wmNews = wmNewsMapper.selectById(6248);
        if (wmNews == null) {
            throw new RuntimeException("WmNewsAutoScanServiceImpl-文章不存在");
        }
        System.out.println(wmNews);
    }
}