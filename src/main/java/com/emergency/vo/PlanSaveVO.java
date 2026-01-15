package com.emergency.vo;

import com.emergency.entity.EmergencyPlan;
import com.emergency.entity.EmergencyPlanStepRelation;
import lombok.Data;

import java.util.List;

// 预案保存VO
@Data
public class PlanSaveVO {
    private EmergencyPlan plan;
    private List<EmergencyPlanStepRelation> relations;
}