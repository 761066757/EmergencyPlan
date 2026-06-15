package com.siasun.iscs.ep.vo;

import com.siasun.iscs.ep.dto.EmergencyPlanDTO;
import com.siasun.iscs.ep.dto.EmergencyPlanTaskRelDTO;
import lombok.Data;

import java.util.List;

/**
 * @author liao
 * @version 1.0
 * @description: 应急预案保存VO
 * @date 2026/6/3
 */
@Data
public class EmergencyPlanSaveVO {
    /**
     * 预案信息
     */
    private EmergencyPlanDTO plan;

    /**
     * 预案关联任务信息
     */
    private List<EmergencyPlanTaskRelDTO> planTaskRelList;
}