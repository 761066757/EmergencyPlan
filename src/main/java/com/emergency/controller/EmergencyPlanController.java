package com.emergency.controller;

import com.emergency.entity.EmergencyPlan;
import com.emergency.entity.EmergencyPlanStepRelation;
import com.emergency.service.EmergencyPlanService;
import com.emergency.service.EmergencyPlanStepRelationService;
import com.emergency.vo.EmergencyStepVO;
import com.emergency.vo.PlanSaveVO;
import com.emergency.vo.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 预案Controller
@Slf4j
@RestController
@RequestMapping("plan")
public class EmergencyPlanController {
    @Resource
    private EmergencyPlanService planService;
    @Resource
    private EmergencyPlanStepRelationService planStepRelationService;

    //查询预案列表
    @GetMapping("/list")
    public Result<List<EmergencyPlan>> getPlanList() {
        try {
            List<EmergencyPlan> planList = planService.getPlanList();
            return Result.success(planList);
        } catch (Exception e) {
            log.error("查询预案列表失败", e);
            return Result.error("查询预案列表失败：" + e.getMessage());
        }
    }

    // 保存预案
    @PostMapping("/save")
    public Result<Boolean> savePlan(@RequestBody PlanSaveVO saveVO) {
        boolean success = planService.savePlan(saveVO.getPlan(), saveVO.getPlanStepRelations());
        return success ? Result.success(true) : Result.error("保存失败");
    }

    // 查询预案详情
    @GetMapping("/getById/{planId}")
    public Result<EmergencyPlan> getById(@PathVariable(value = "planId") String planId) {
        return Result.success(planService.getById(planId));
    }

    // 查询预案关联步骤
    @GetMapping("/getRelations{id}")
    public Result<List<EmergencyPlanStepRelation>> getRelations(@PathVariable(value = "id") String planId) {
        return Result.success(planStepRelationService.getByPlanId(planId));
    }
}
