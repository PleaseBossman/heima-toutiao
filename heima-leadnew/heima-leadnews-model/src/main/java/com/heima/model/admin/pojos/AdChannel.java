package com.heima.model.admin.pojos;

import lombok.Data;

@Data
public class AdChannel {
    private String createdTime;
    private String description;
    private int id;
    private boolean isDefault;
    private String name;
    private int ord;
    private boolean status;
}
