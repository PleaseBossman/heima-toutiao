package com.heima.model.schedule.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author itheima
 */
@Data
@TableName("taskinfo")
//Taskinfo 类可能需要存储到数据库或者通过网络传输，因此实现了 Serializable 接口，以确保该对象能够被序列化和反序列化。
//如果一个类不需要存储或通过网络传输，就不需要实现 Serializable 接口。
public class Taskinfo implements Serializable {

    //serialVersionUID 是 Java 序列化机制用来验证版本一致性的重要标识。当对象被序列化到文件或通过网络传输时，接收方（或重新加载方）会使用 serialVersionUID 来验证发送方和接收方的类是否兼容
    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     */
    @TableId(type = IdType.ID_WORKER)
    private Long taskId;

    /**
     * 执行时间
     */
    @TableField("execute_time")
    private Date executeTime;

    /**
     * 参数
     */
    @TableField("parameters")
    private byte[] parameters;

    /**
     * 优先级
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 任务类型
     */
    @TableField("task_type")
    private Integer taskType;


}
