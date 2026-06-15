package com.siasun.iscs.ep.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author liao
 * @version 1.0
 * @description: 应急预案查询VO
 * @date 2026/6/4
 */
@Data
public class EmergencyPlanQueryVO {

    // ====================  EmergencyPlan 字段 ====================
    /**
     * 预案主键ID
     */
    // 字段名修改为 planId（更直观，避免与流程关系ID冲突）
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
     * BPMN流程xml
     */
    private String bpmnXml;
    /**
     * 摄像头ID集合
     */
    private String cameraIds;
    /**
     * 预案创建时间
     */
    // 加前缀避免与流程关系的创建时间冲突
    private LocalDateTime planCreateTime;
    /**
     * 预案更新时间
     */
    private LocalDateTime planUpdateTime;
    /**
     * 预案逻辑删除标记（0-未删除，1-已删除）
     */
    private Integer planIsDeleted;

    // ==================== EmergencyPlanFlowRel 字段 ====================
    /**
     * 流程关系主键ID
     */
    private String relId;
    /**
     * 关联的预案ID（与 planId 一致，可选保留，方便前端核对）
     */
    private String relPlanId;
    /**
     * Flowable部署ID
     */
    private String deployId;
    /**
     * Flowable流程实例ID
     */
    private String procInstId;
    /**
     * 当前待办任务ID
     */
    private String currentTaskId;
    /**
     * 当前待办任务名称
     */
    private String currentTaskName;
    /**
     * 流程状态（1-运行中 2-已结束 3-终止）
     */
    private Integer flowStatus;
    /**
     * 流程关系创建时间
     */
    private LocalDateTime relCreateTime;
    /**
     * 流程关系更新时间
     */
    private LocalDateTime relUpdateTime;
    /**
     * 流程关系逻辑删除标记（0-未删除，1-已删除）
     */
    private Integer relIsDeleted;
}
