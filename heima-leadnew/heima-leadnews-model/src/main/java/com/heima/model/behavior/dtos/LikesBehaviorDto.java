package com.heima.model.behavior.dtos;


import com.heima.model.common.annotation.IdEncrypt;
import lombok.Data;

@Data
public class LikesBehaviorDto {

    @IdEncrypt
    Long articleId;

    Short operation;

    Short type;
}
