package com.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预案实体
 */
@Data
@TableName("emergency_plan")
public class EmergencyPlan {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String planName;
    private String planType;
    private String planDesc;
    private String bpmnXml;
    private String cameraIds;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}