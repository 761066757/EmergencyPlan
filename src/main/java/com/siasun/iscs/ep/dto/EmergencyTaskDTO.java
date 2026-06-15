package com.siasun.iscs.ep.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @description: 任务DTO
 * @author liao
 * @date 2026/6/3
 * @version 1.0
 */
 
@Data
public class EmergencyTaskDTO {
    /**
     * 主键ID
     */
    private String id;

    /**
     * 车站
     */
    private String station;

    /**
     * 任务编码
     */
    private Integer taskCode;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务描述
     */
    private String taskDesc;

    /**
     * 是否允许跳过：0-否，1-是
     */
    private Integer isSkippable;

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
