package com.siasun.iscs.ep.controller;

import com.siasun.iscs.ep.dto.EmergencyPlanDTO;
import com.siasun.iscs.ep.entity.EmergencyPlan;
import com.siasun.iscs.ep.service.EmergencyPlanService;
import com.siasun.iscs.ep.vo.EmergencyPlanSaveVO;
import com.siasun.iscs.ep.vo.EmergencyPlanQueryVO;
import com.siasun.iscs.ep.vo.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @author liao
 * @version 1.0
 * @description: 预案管理
 * @date 2026/6/11
 */
@Slf4j
@RestController
@RequestMapping("plan")
public class EmergencyPlanController {
    @Resource
    private EmergencyPlanService planService;


    /**
     * 保存预案
     */
    @PostMapping("/savePlan")
    public Result<Boolean> savePlan(@Valid @RequestBody EmergencyPlanSaveVO saveVO) {
        boolean success = planService.savePlan(saveVO.getPlan(), saveVO.getPlanTaskRelList());
        return success ? Result.success(true) : Result.error("保存失败");
    }

    /**
     * BPMN文件校验
     */
    @PostMapping("/bpmnValidate")
    public Result<Map<String, String>> bpmnValidate(@RequestParam("file") MultipartFile file) {
        return planService.bpmnValidate(file);
    }

    /**
     * 预案文档上传
     */
    @PostMapping("/uploadPlanDoc")
    public Result<String> uploadPlanDoc(@RequestParam("file") MultipartFile file) {
        return planService.uploadPlanDoc(file);
    }


    // ======================================================================================================================

    /**
     * 查询预案列表
     */
    @PostMapping("/queryPlanList")
    public Result<List<EmergencyPlanQueryVO>> getPlanList(@Valid @RequestBody EmergencyPlanDTO planDTO) {
        try {
            List<EmergencyPlanQueryVO> planList = planService.getPlanList(planDTO);
            return Result.success(planList);
        } catch (Exception e) {
            log.error("查询预案列表失败", e);
            return Result.error("查询预案列表失败：" + e.getMessage());
        }
    }

    /**
     * 查询预案详情
     */
    @PostMapping("/getPlanDetail")
    public Result<EmergencyPlan> getPlanDetail(@RequestParam("planId") String planId) {
        return Result.success(planService.getPlanDetail(planId));
    }


    /**
     * 实时监控
     */
    @PostMapping("/realTimeMonitor")
    public Result<String> realTimeMonitor(@RequestParam(value = "planId") String planId, @RequestParam(value = "cameraId") String cameraId) {
        return Result.success(planService.realTimeMonitor(planId, cameraId));
    }

}

