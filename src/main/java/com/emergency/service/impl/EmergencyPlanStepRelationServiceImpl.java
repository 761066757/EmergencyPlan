package com.emergency.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emergency.entity.EmergencyPlanStepRelation;
import com.emergency.mapper.EmergencyPlanStepRelationMapper;
import com.emergency.service.EmergencyPlanStepRelationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmergencyPlanStepRelationServiceImpl extends ServiceImpl<EmergencyPlanStepRelationMapper, EmergencyPlanStepRelation> implements EmergencyPlanStepRelationService {

    @Resource
    private EmergencyPlanStepRelationMapper baseMapper;

    // 批量保存关联关系（先删后加）
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRelations(String planId, List<EmergencyPlanStepRelation> relations) {
        // 逻辑删除原有关联
        baseMapper.deleteByPlanId(planId);
        // 批量新增
        if (CollUtil.isNotEmpty(relations)) {
            relations.forEach(rel -> {
                rel.setPlanId(planId);
                rel.setIsDeleted(0);
                rel.setCreateTime(LocalDateTime.now());
                rel.setUpdateTime(LocalDateTime.now());
            });
            this.saveBatch(relations);
        }
    }

    // 根据预案ID查询关联步骤
    @Override
    public List<EmergencyPlanStepRelation> getByPlanId(String planId) {
        return baseMapper.selectByPlanId(planId);
    }
}

