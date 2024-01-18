package com.heima.model.behavior.dtos;

import com.heima.model.common.annotation.IdEncrypt;
import lombok.Data;

import java.util.Date;

@Data
public class CollectionBehaviorDto {
     @IdEncrypt
     Long entryId;

     Short operation;

     Date publishedTime;

     Short type;
}
