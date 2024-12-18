package com.heima.wemedia.controller.v1;

import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.service.WmChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/channel")
public class WmchannelController {

    @Autowired
    private WmChannelService wmChannelService;

    @GetMapping("/channels")
    public ResponseResult findAll(){
        return wmChannelService.findAll();
    }

    @PostMapping("/list")
    public ResponseResult findList(@RequestBody ChannelDto dto){
        return wmChannelService.findList(dto);
    }

    @GetMapping("/del/{id}")
    public ResponseResult delChannel(@PathVariable("id") Integer id){
        return wmChannelService.delChannel(id);
    }

    @PostMapping("/update")
    public ResponseResult updateChannel(@RequestBody WmChannel dto){
        return wmChannelService.updateChannel(dto);
    }
    @PostMapping("/save")
    public ResponseResult insertChannel(@RequestBody WmChannel dto){
        return wmChannelService.insertChannel(dto);
    }
}
