package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.common.constants.UserConstants;
import com.heima.model.admin.pojos.ApUserRealname;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.AuthDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.mapper.ApUserRealnameMapper;
import com.heima.user.service.ApUserRealnameService;
import com.sun.corba.se.impl.orbutil.concurrent.ReentrantMutex;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
public class ApUserRealnameServiceImpl extends ServiceImpl<ApUserRealnameMapper, ApUserRealname> implements ApUserRealnameService{


    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findList(AuthDto dto) {
        if(dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        LambdaQueryWrapper<ApUserRealname> lambdaQueryWrapper = new LambdaQueryWrapper();
        IPage page = new Page(dto.getPage(),dto.getSize());
        if(dto.getStatus() != null){
            lambdaQueryWrapper.eq(ApUserRealname::getStatus, dto.getStatus());
        }
        page = page(page,lambdaQueryWrapper);
        ResponseResult responseResult = new PageResponseResult(dto.getPage(),dto.getSize(),(int)page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }

    /**
     * 修改状态
     *
     * @param dto
     * @param status
     * @return
     */
    @Override
    public ResponseResult updateStatus(AuthDto dto, Short status) {
        if(dto == null || status == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApUserRealname apUserRealname = new ApUserRealname();
        apUserRealname.setUserId(dto.getId());
        apUserRealname.setStatus(status);
        apUserRealname.setUpdatedTime(new Date());
        if(StringUtils.isBlank(dto.getMsg())){
            apUserRealname.setReason(dto.getMsg());
        }
        updateById(apUserRealname);

        if(status.equals(UserConstants.PASS_AUTH)){
            ResponseResult responseResult = createWmUserAndAuthor(dto);
            if(responseResult != null){
                return responseResult;
            }
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }


    @Autowired
    private ApUserMapper apUserMapper;
    @Autowired
    private IWemediaClient wemediaClient;

    private ResponseResult createWmUserAndAuthor(AuthDto dto) {
        Integer userRealnameId = dto.getId();
        ApUserRealname userRealname = getById(userRealnameId);
        if(userRealname == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        Integer userId = userRealname.getUserId();
        ApUser apUser = apUserMapper.selectById(userId);
        if(apUser == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        WmUser wmUser = wemediaClient.findWmUserByName(apUser.getName());
        if(wmUser == null){
            wmUser = new WmUser();
            wmUser.setApUserId(apUser.getId());
            wmUser.setCreatedTime(new Date());
            wmUser.setName(apUser.getName());
            wmUser.setPassword(apUser.getPassword());
            wmUser.setSalt(apUser.getSalt());
            wmUser.setPhone(apUser.getPhone());
            wmUser.setStatus(9);
            wemediaClient.saveWmUser(wmUser);

        }
        apUser.setFlag((short)1);
        apUserMapper.updateById(apUser);
        return null;
    }
}
