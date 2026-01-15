package com.emergency.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emergency.entity.EmergencyPlan;
import com.emergency.entity.EmergencyPlanStepRelation;
import com.emergency.mapper.EmergencyPlanMapper;
import com.emergency.service.EmergencyPlanService;
import com.emergency.service.EmergencyPlanStepRelationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmergencyPlanServiceImpl  extends ServiceImpl<EmergencyPlanMapper, EmergencyPlan> implements EmergencyPlanService {
    @Resource
    private EmergencyPlanStepRelationService planStepRelationService;

    // 保存预案（含关联步骤）
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean savePlan(EmergencyPlan plan, List<EmergencyPlanStepRelation> relations) {
        // 保存预案基础信息
        boolean save = this.saveOrUpdate(plan);
        // 保存关联步骤
        planStepRelationService.saveRelations(plan.getId(), relations);
        return save;
    }
}
