package com.emergency.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 步骤新增/编辑DTO
 */
@Data
public class StepDTO {
    /**
     * 主键ID（编辑时传）
     */
    private String id;

    /**
     * 步骤编码
     */
    @NotBlank(message = "步骤编码不能为空")
    private String stepCode;

    /**
     * 步骤名称
     */
    @NotBlank(message = "步骤名称不能为空")
    private String stepName;

    /**
     * 步骤类型
     */
    @NotBlank(message = "步骤类型不能为空")
    private String stepType;

    /**
     * 步骤描述
     */
    private String stepDesc;
}