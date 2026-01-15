package com.emergency.controller;

import com.emergency.entity.EmergencyPlan;
import com.emergency.entity.EmergencyPlanStepRelation;
import com.emergency.service.EmergencyPlanService;
import com.emergency.service.EmergencyPlanStepRelationService;
import com.emergency.vo.PlanSaveVO;
import com.emergency.vo.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 预案Controller
@RestController
@RequestMapping("/plan")
public class EmergencyPlanController {
    @Resource
    private EmergencyPlanService planService;
    @Resource
    private EmergencyPlanStepRelationService planStepRelationService;

    // 保存预案
    @PostMapping("/save")
    public Result<Boolean> savePlan(@RequestBody PlanSaveVO saveVO) {
        boolean success = planService.savePlan(saveVO.getPlan(), saveVO.getRelations());
        return success ? Result.success(true) : Result.error("保存失败");
    }

    // 查询预案详情
    @GetMapping("/getById")
    public Result<EmergencyPlan> getById(@RequestParam(value = "id") String id) {
        return Result.success(planService.getById(id));
    }

    // 查询预案关联步骤
    @GetMapping("/getRelations")
    public Result<List<EmergencyPlanStepRelation>> getRelations(@RequestParam(value = "id") String planId) {
        return Result.success(planStepRelationService.getByPlanId(planId));
    }
}
