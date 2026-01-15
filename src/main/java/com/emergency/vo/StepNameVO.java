package com.emergency.vo;

import lombok.Data;

/**
 * 步骤名称VO（供Flowable调用）
 */
@Data
public class StepNameVO {
    private String id;
    private String stepName;
}