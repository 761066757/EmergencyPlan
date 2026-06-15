package com.siasun.iscs.ep.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author liao
 * @version 1.0
 * @description: 查询当前任务配套VO（确保字段齐全）
 * @date 2026/6/4
 */
@Data
public class EmergencyCurrentTaskQueryVO {

    /**
     * 预案ID
     */
    private String planId;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * BPMN节点ID，用于前端高亮
     */
    private String bpmnElementId;

    /**
     * 创建时间
     */
    private Date createTime;

}
