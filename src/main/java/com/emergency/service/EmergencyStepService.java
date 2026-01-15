package com.emergency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.emergency.dto.StepDTO;
import com.emergency.entity.EmergencyStep;
import com.emergency.vo.EmergencyStepVO;
import com.emergency.vo.StepNameVO;

import java.util.List;

/**
 * 步骤配置服务接口
 */
public interface EmergencyStepService extends IService<EmergencyStep> {

    /**
     * 保存/编辑步骤
     */
    boolean saveOrUpdateStep(StepDTO stepDTO);

    List<StepNameVO> getStepNameByIds(List<String> stepIds);

    List<EmergencyStep> listByModule(String moduleCode);

    List<EmergencyStepVO> listStepVOByModule(String moduleCode);

    boolean saveStep(EmergencyStep step);

    boolean deleteStep(String id);
}