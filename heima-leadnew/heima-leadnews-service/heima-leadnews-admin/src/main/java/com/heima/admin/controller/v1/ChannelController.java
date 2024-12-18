package com.heima.admin.controller.v1;

import com.heima.admin.service.adUserService;
import com.heima.admin.service.channelService;
import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/channel")
public class ChannelController {

    @Autowired
    private channelService channelservice;

    @PostMapping("/list")
    public ResponseResult findList(@RequestBody ChannelDto dto){
        return channelservice.findList(dto);
    }

    @GetMapping("/del/{id}")
    public ResponseResult delChannel(@PathVariable int id){
        return channelservice.delChannel(id);
    }

    @PostMapping("/update")
    public ResponseResult updateChannel(@RequestBody AdChannel dto){
        return channelservice.updateChannel(dto);
    }
    @PostMapping("/save")
    public ResponseResult insertChannel(@RequestBody AdChannel dto){
        return channelservice.insertChannel(dto);
    }
}
