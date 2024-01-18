package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.pojos.ApUserRealname;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.AuthDto;

public interface ApUserRealnameService extends IService<ApUserRealname> {
    /**
     * 分页查询
     * @param dto
     * @return
     */
    ResponseResult findList(AuthDto dto);

    /**
     * 修改状态
     * @param dto
     * @param status
     * @return
     */
    ResponseResult updateStatus(AuthDto dto, Short status);
}
