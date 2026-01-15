package com.emergency.vo;

import com.emergency.entity.EmergencyStep;
import lombok.Data;

// 步骤VO（含类型名称，前端展示用）
@Data
public class EmergencyStepVO extends EmergencyStep {
    private String stepTypeName;  // 步骤类型名称（从sys_dict.dictLabel获取）
}
