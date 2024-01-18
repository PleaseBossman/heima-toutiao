package com.heima.article.controller.v1;


import com.heima.article.service.CollectionBehaviorSerive;
import com.heima.model.behavior.dtos.CollectionBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v1/collection_behavior")
public class CollectionBehaviorController {

    @Autowired
    private CollectionBehaviorSerive collectionBehaviorSerive;

    @PostMapping
    public ResponseResult CollectionBehavior(@RequestBody CollectionBehaviorDto dto){
        return collectionBehaviorSerive.CollectionBehavior(dto);
    }
}
