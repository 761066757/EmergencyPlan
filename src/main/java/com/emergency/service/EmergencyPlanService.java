package com.emergency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.emergency.dto.PlanDTO;
import com.emergency.dto.PlanStepRelationDTO;
import com.emergency.entity.EmergencyPlan;
import com.emergency.vo.PlanQueryVO;

import java.util.List;

public interface EmergencyPlanService extends IService<EmergencyPlan> {

    List<PlanQueryVO> getPlanList();
    boolean savePlan(PlanDTO plan, List<PlanStepRelationDTO> planStepRelations);

}
