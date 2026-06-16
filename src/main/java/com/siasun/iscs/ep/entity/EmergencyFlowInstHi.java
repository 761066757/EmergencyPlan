package com.siasun.iscs.ep.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @description: 流程实例历史表
 * @author liao
 * @date 2026/6/10
 * @version 1.0
 */
@Data
@TableName("emergency_flow_inst_hi")
public class EmergencyFlowInstHi {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 车站
     */
    private String station;

    /**
     * 预案ID
     */
    private String planId;

    /**
     * 预案名称
     */
    private String planName;

    /**
     * 预案类型
     */
    private String planType;

    /**
     * 预案文档
     */
    private String planDoc;

    /**
     * 预案BPMN XML
     */
    private String bpmnXml;

    /**
     * 关联摄像头ID
     */
    private String cameraIds;

    /**
     * 录像回放 cramerId1:videoUrl1;camerId2:videoUrl2;camerId3:videoUrl3...
     */
    private String videoUrls;

    /**
     * Flowable部署ID
     */
    private String deployId;

    /**
     * Flowable流程实例ID（历史）
     */
    private String hiProcInstId;

    /**
     * 是否历史记录：0-否，1-是
     */
    private Integer isHistory;

    /**
     * 操作人
     */
    private String optName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标记 0-未删除 1-已删除
     */
    @TableLogic
    private Integer isDeleted;
}
