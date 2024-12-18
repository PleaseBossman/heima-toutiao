package com.heima.admin.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.admin.mapper.AdChannelMapper;
import com.heima.admin.mapper.AdUserMapper;
import com.heima.admin.service.adUserService;
import com.heima.admin.service.channelService;
import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class channelServiceImpl extends ServiceImpl<AdChannelMapper, AdChannel> implements channelService {



    /**
     * 查询分页频道列表
     *
     * @param dto
     */
    @Override
    public ResponseResult findList(ChannelDto dto) {
        dto.checkParam();
        IPage page = new Page(dto.getPage(),dto.getSize());
        LambdaQueryWrapper<AdChannel> lambdaQueryWrapper = new LambdaQueryWrapper();
        if(StringUtils.isBlank(dto.getName())){
            lambdaQueryWrapper.like(AdChannel::getName,dto.getName());
        }
        page = page(page, lambdaQueryWrapper);

        //3.结果返回
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        responseResult.setData(page.getRecords());


        return responseResult;
    }

    /**
     * 删除频道
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult delChannel(int id) {
        AdChannel adChannel = getById(id);
        if(adChannel.isStatus() == false){
            removeById(id);
        }else{
            return ResponseResult.errorResult(AppHttpCodeEnum.NO_OPERATOR_AUTH);
        }
        return ResponseResult.okResult(id);
    }

    /**
     * update
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult updateChannel(AdChannel dto) {
        AdChannel adChannel = new AdChannel();
        BeanUtils.copyProperties(dto,adChannel);
        updateById(adChannel);
        return ResponseResult.okResult(adChannel);
    }

    /**
     * insert
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult insertChannel(AdChannel dto) {
        insertChannel(dto);
        return ResponseResult.okResult(dto);
    }


}
