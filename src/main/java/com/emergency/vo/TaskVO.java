package com.emergency.vo;

import lombok.Data;
import java.util.Date;

// 配套TaskVO（确保字段齐全）
@Data
public class TaskVO {
    private String taskId;
    private String taskName;
    private Date createTime;
    private String bpmnElementId;
    private String planId;
}
