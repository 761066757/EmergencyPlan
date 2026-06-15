package com.siasun.iscs.ep.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.List;

/**
 * @description: 预案表
 * @author liao
 * @date 2026/6/3
 * @version 1.0
 */
@Data
@TableName("emergency_plan")
public class EmergencyPlan {
    /**
     * 主键ID
     */
    // @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 车站
     */
    private String station;

    /**
     * 预案名称
     */
    private String planName;

    /**
     * 预案类型
     */
    private String planType;

    /**
     * 预案描述
     */
    private String planDesc;

    /**
     * 预案文档
     */
    private String planDoc;

    /**
     * 预案状态：0-未发布（未部署），1-已发布（已部署deploy），2-执行中（执行start）
     */
    private Integer planStatus;

    /**
     * 预案BPMN XML
     */
    private String bpmnXml;

    /**
     * 关联摄像头ID
     */
    private String cameraIds;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer isDeleted;
}