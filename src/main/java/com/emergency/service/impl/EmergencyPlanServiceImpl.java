package com.emergency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emergency.dto.PlanDTO;
import com.emergency.dto.PlanStepRelationDTO;
import com.emergency.entity.EmergencyPlan;
import com.emergency.entity.EmergencyPlanStepRelation;
import com.emergency.mapper.EmergencyPlanMapper;
import com.emergency.service.EmergencyPlanService;
import com.emergency.service.EmergencyPlanStepRelationService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmergencyPlanServiceImpl extends ServiceImpl<EmergencyPlanMapper, EmergencyPlan> implements EmergencyPlanService {
    @Resource
    private EmergencyPlanStepRelationService planStepRelationService;

    @Resource
    private EmergencyPlanMapper emergencyPlanMapper;

    // 查询预案列表
    @Override
    public List<EmergencyPlan> getPlanList() {
        return emergencyPlanMapper.selectList(new LambdaQueryWrapper<>());
    }

    // 保存预案（含关联步骤）
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean savePlan(PlanDTO planDTO, List<PlanStepRelationDTO> planStepRelations) {
        String cameraIdsStr = String.join("|", planDTO.getCameraIds());
        EmergencyPlan plan = new EmergencyPlan();
        BeanUtils.copyProperties(planDTO, plan);
        plan.setCameraIds(cameraIdsStr);
        // 1、保存预案基础信息
        boolean save = this.saveOrUpdate(plan);
        // 核心优化：Stream + 空值判断 + 流式转换
        List<EmergencyPlanStepRelation> relations = CollectionUtils.isEmpty(planStepRelations)
                ? new ArrayList<>() // 空列表返回空，避免NPE
                : planStepRelations.stream()
                .map(dto -> {
                    EmergencyPlanStepRelation relation = new EmergencyPlanStepRelation();
                    BeanUtils.copyProperties(dto, relation);
                    return relation;
                })
                .collect(Collectors.toList());
        // 2、保存关联步骤
        planStepRelationService.saveRelations(plan.getId(), relations);
        return save;
    }
}
