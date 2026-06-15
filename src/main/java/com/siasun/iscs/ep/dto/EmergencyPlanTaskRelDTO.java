package com.siasun.iscs.ep.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @description: 预案与任务关联表
 * @author liao
 * @date 2026/6/3
 * @version 1.0
 */
@Data
public class EmergencyPlanTaskRelDTO {

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
     * 任务类型
     */
    private String taskType;

    /**
     * Flowable节点ID
     */
    private String flowNodeId;

    /**
     * Flowable节点名称
     */
    private String flowNodeName;

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
