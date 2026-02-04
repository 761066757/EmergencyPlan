package com.emergency.vo;

import com.emergency.dto.PlanDTO;
import com.emergency.dto.PlanStepRelationDTO;
import com.emergency.entity.EmergencyPlan;
import com.emergency.entity.EmergencyPlanFlowRel;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// 预案查询VO
@Data
public class PlanQueryVO {
    // ==================== 原 EmergencyPlan 字段 ====================
    /** 预案主键ID */
    private String planId; // 字段名修改为 planId（更直观，避免与流程关系ID冲突）
    /** 预案名称 */
    private String planName;
    /** 预案类型 */
    private String planType;
    /** 预案描述 */
    private String planDesc;
    /** BPMN流程xml */
    private String bpmnXml;
    /** 摄像头ID集合 */
    private String cameraIds;
    /** 预案创建时间 */
    private LocalDateTime planCreateTime; // 加前缀避免与流程关系的创建时间冲突
    /** 预案更新时间 */
    private LocalDateTime planUpdateTime;
    /** 预案逻辑删除标记（0-未删除，1-已删除） */
    private Integer planIsDeleted;

    // ==================== 原 EmergencyPlanFlowRel 字段 ====================
    /** 流程关系主键ID */
    private String relId;
    /** 关联的预案ID（与 planId 一致，可选保留，方便前端核对） */
    private String relPlanId;
    /** Flowable部署ID */
    private String deployId;
    /** Flowable流程实例ID */
    private String procInstId;
    /** 当前待办任务ID */
    private String currentTaskId;
    /** 当前待办任务名称 */
    private String currentTaskName;
    /** 流程状态（1-运行中 2-已结束 3-终止） */
    private Integer flowStatus;
    /** 流程关系创建时间 */
    private LocalDateTime relCreateTime;
    /** 流程关系更新时间 */
    private LocalDateTime relUpdateTime;
    /** 流程关系逻辑删除标记（0-未删除，1-已删除） */
    private Integer relIsDeleted;
}
