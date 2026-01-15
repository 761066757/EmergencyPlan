package com.emergency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.emergency.entity.EmergencyPlan;
import com.emergency.entity.EmergencyPlanStepRelation;

import java.util.List;

public interface EmergencyPlanService extends IService<EmergencyPlan> {
    boolean savePlan(EmergencyPlan plan, List<EmergencyPlanStepRelation> relations);

}
