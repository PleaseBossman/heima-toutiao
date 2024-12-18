package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.SensitiveDto;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;

public interface WmSensitiveService extends IService<WmSensitive> {
    /**
     * 分页查询
     * @param dto
     * @return
     */
    ResponseResult findlist(SensitiveDto dto);

    /**
     * 插入数据
     * @param wmSensitive
     * @return
     */
    ResponseResult insert(WmSensitive wmSensitive);

    /**
     * 删除数据
     * @param id
     * @return
     */
    ResponseResult delete(Integer id);

    /**
     * 修改数据
     * @param wmSensitive
     * @return
     */
    ResponseResult update(WmSensitive wmSensitive);
}
