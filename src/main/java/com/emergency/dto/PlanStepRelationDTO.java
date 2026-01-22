package com.emergency.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data

public class PlanStepRelationDTO {
    private String id;
    private String planId;
    private String stepId;
    //private String stepType;
    private String flowNodeId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
