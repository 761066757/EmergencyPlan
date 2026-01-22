package com.emergency.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 预案实体
 */
@Data
public class PlanDTO {
    private String id;
    private String planName;
    private String planType;
    private String planDesc;
    private String bpmnXml;
    private List<String> cameraIds;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}