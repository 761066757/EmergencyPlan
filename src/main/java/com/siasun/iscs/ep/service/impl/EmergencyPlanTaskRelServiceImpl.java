package com.siasun.iscs.ep.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siasun.iscs.ep.entity.EmergencyPlanTaskRel;
import com.siasun.iscs.ep.mapper.EmergencyPlanTaskRelMapper;
import com.siasun.iscs.ep.service.EmergencyPlanTaskRelService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @description: 预案任务关联服务实现类
 * @author liao
 * @date 2026/6/4
 * @version 1.0
 */
@Service
public class EmergencyPlanTaskRelServiceImpl extends ServiceImpl<EmergencyPlanTaskRelMapper, EmergencyPlanTaskRel> implements EmergencyPlanTaskRelService {

    @Resource
    private EmergencyPlanTaskRelMapper baseMapper;

    /**
     * 批量保存预案关联任务（先删后加）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRelations(String planId, String station, List<EmergencyPlanTaskRel> relations) {
        // 1. 逻辑删除原有关联
        baseMapper.deleteByPlanId(planId);
        // 2.批量新增
        if (CollUtil.isNotEmpty(relations)) {
            relations.forEach(rel -> {
                rel.setPlanId(planId);
                rel.setStation(station);
                rel.setIsDeleted(0);
                rel.setCreateTime(LocalDateTime.now());
                rel.setUpdateTime(LocalDateTime.now());
            });
            this.saveBatch(relations);
        }
    }

}

