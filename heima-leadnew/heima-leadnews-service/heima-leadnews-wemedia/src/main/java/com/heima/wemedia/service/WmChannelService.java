package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;

public interface WmChannelService extends IService<WmChannel> {

    /**
     * 查询所有频道
     * @return
     */
    public ResponseResult findAll();

    /**
     *  查询分页频道列表
     * @param dto
     */
    ResponseResult findList(ChannelDto dto);

    /**
     * 删除频道
     * @param id
     * @return
     */
    ResponseResult delChannel(Integer id);

    /**
     * update
     * @param dto
     * @return
     */
    ResponseResult updateChannel(WmChannel dto);

    /**
     * insert
     * @param dto
     * @return
     */
    ResponseResult insertChannel(WmChannel dto);
}