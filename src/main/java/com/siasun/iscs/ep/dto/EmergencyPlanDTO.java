package com.siasun.iscs.ep.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @description: 预案DTO
 * @author liao
 * @date 2026/6/3
 * @version 1.0
 */
@Data
public class EmergencyPlanDTO {
    /**
     * 主键ID
     */
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
     * 关联摄像头ID 以;分割
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
     * 是否删除：0-未删除，1-已删除
     */
    private Integer isDeleted;
}