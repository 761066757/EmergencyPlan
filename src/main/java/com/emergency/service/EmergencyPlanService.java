package com.emergency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.emergency.dto.PlanDTO;
import com.emergency.dto.PlanStepRelationDTO;
import com.emergency.entity.EmergencyPlan;

import java.util.List;

public interface EmergencyPlanService extends IService<EmergencyPlan> {

    List<EmergencyPlan> getPlanList();
    boolean savePlan(PlanDTO plan, List<PlanStepRelationDTO> planStepRelations);

}
