package com.emergency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emergency.dto.StepDTO;
import com.emergency.entity.EmergencyStep;
import com.emergency.mapper.EmergencyStepMapper;
import com.emergency.service.EmergencyStepService;
import com.emergency.vo.EmergencyStepVO;
import com.emergency.vo.StepNameVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 步骤配置服务实现类
 */
@Service
public class EmergencyStepServiceImpl extends ServiceImpl<EmergencyStepMapper, EmergencyStep> implements EmergencyStepService {

    @Resource
    private EmergencyStepMapper stepMapper;

    /**
     * 保存/编辑步骤
     */
    // 保存步骤（新增/编辑）
    @Transactional(rollbackFor = Exception.class)
    public boolean saveStep(EmergencyStep step) {
        if (StringUtils.isEmpty(step.getId())) {
            // 新增：自动填充创建时间
            step.setCreateTime(LocalDateTime.now());
            step.setUpdateTime(LocalDateTime.now());
            step.setIsDeleted(0);
            return this.save(step);
        } else {
            // 编辑：更新时间
            step.setUpdateTime(LocalDateTime.now());
            return this.updateById(step);
        }
    }

    @Override
    public boolean saveOrUpdateStep(StepDTO stepDTO) {
        return false;
    }

    // 批量查询步骤名称（供Flowable调用）
    @Override
    public List<StepNameVO> getStepNameByIds(List<String> stepIds) {
        return baseMapper.getStepNameByIds(stepIds);
    }

    // 复用现有查询步骤列表的方法
    @Override
    public List<EmergencyStep> listByModule(String moduleCode) {
        LambdaQueryWrapper<EmergencyStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmergencyStep::getIsDeleted, 0);
        // 如需按模块过滤，可添加：wrapper.eq(EmergencyStep::getModuleCode, moduleCode);
        return this.list(wrapper);
    }

    @Override
    // 按模块查询步骤（含类型名称）
    public List<EmergencyStepVO> listStepVOByModule(String moduleCode) {
        return baseMapper.selectStepVOByModule(moduleCode);
    }

    @Override
    // 删除步骤（逻辑删除）
    public boolean deleteStep(String id) {
        EmergencyStep step = new EmergencyStep();
        step.setId(id);
        step.setIsDeleted(1);
        step.setUpdateTime(LocalDateTime.now());
        return this.updateById(step);
    }
}