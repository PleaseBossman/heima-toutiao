package com.heima.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;

public interface channelService extends IService<AdChannel> {

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
    ResponseResult delChannel(int id);

    /**
     * update
     * @param dto
     * @return
     */
    ResponseResult updateChannel(AdChannel dto);

    /**
     * insert
     * @param dto
     * @return
     */
    ResponseResult insertChannel(AdChannel dto);
}
