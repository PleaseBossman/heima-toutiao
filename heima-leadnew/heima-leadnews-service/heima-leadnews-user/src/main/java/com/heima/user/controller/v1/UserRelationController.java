package com.heima.user.controller.v1;


import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.UserRelationDto;
import com.heima.user.service.UserRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/user/user_follow")
public class UserRelationController {

    @Autowired
    private UserRelationService userRelationService;

    @PostMapping
    public ResponseResult UserRelation(@RequestBody UserRelationDto dto){
        return userRelationService.follow(dto);
    }
}
