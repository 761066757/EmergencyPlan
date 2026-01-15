package com.emergency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.emergency.entity.EmergencyPlanStepRelation;

import java.util.List;


public interface EmergencyPlanStepRelationService extends IService<EmergencyPlanStepRelation> {

    void saveRelations(String id, List<EmergencyPlanStepRelation> relations);

    List<EmergencyPlanStepRelation> getByPlanId(String planId);
}
