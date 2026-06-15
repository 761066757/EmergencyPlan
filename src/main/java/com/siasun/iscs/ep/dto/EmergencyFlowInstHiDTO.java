package com.siasun.iscs.ep.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @description: 流程实例历史DTO
 * @author liao
 * @date 2026/6/10
 * @version 1.0
 */
@Data
public class EmergencyFlowInstHiDTO {

    /**
     * 主键ID
     */
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
     * 录像回放
     */
    private String videoUrls;

    /**
     * Flowable部署ID
     */
    private String deployId;

    /**
     * Flowable流程实例ID
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
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标记 0-未删除 1-已删除
     */
    private Integer isDeleted;
}
