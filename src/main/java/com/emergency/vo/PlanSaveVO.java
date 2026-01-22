package com.emergency.vo;

import com.emergency.dto.PlanDTO;
import com.emergency.dto.PlanStepRelationDTO;
import lombok.Data;

import java.util.List;

// 预案保存VO
@Data
public class PlanSaveVO {
    private PlanDTO plan;
    private List<PlanStepRelationDTO> planStepRelations;
}