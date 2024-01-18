package com.heima.model.user.dtos;

import com.heima.model.common.annotation.IdEncrypt;
import lombok.Data;

@Data
public class UserRelationDto {
    @IdEncrypt
    Long articleId;
    @IdEncrypt
    Integer authorId;

    Short operation;
}
