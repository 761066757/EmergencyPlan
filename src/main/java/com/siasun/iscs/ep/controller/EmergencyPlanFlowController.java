package com.siasun.iscs.ep.controller;

import com.siasun.iscs.ep.service.EmergencyPlanFlowRelService;
import com.siasun.iscs.ep.vo.Result;
import com.siasun.iscs.ep.vo.EmergencyCurrentTaskQueryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import java.util.*;

/**
 * @author liao
 * @version 1.0
 * @description: 核心业务接口（流程控制）
 * @date 2026/1/14
 */
@Slf4j
@RestController
@RequestMapping("/flow")
public class EmergencyPlanFlowController {

    @Resource
    private EmergencyPlanFlowRelService emergencyPlanFlowRelService;

    /**
     * 发布预案-创建部署
     */
    @PostMapping("/deploy")
    public Result<String> deployFlow(@RequestParam("planId") String planId) {
        return emergencyPlanFlowRelService.deployFlow(planId);
    }

    /**
     * 撤回预案-删除部署
     */
    @PostMapping("/revoke")
    public Result<String> revokeFlow(@RequestParam("planId") String planId) {
        return emergencyPlanFlowRelService.revokeFlow(planId);
    }

    /**
     * 启动预案-启动流程实例
     */
    @PostMapping("/start")
    public Result<String> startFlow(@RequestParam("planId") String planId) {
        return emergencyPlanFlowRelService.startFlow(planId);
    }

    /**
     * 查询当前待办任务(包含并行任务)
     */
    @GetMapping("/task")
    public Result<List<EmergencyCurrentTaskQueryVO>> getCurrentTask(@RequestParam("planId") String planId) {
        return emergencyPlanFlowRelService.getCurrentTask(planId);
    }

    /**
     * 完成步骤-流程流转到下一个节点
     */
    @PostMapping("/next")
    public Result<Map<String, Object>> completeTask(@RequestParam("taskId") String taskId, @RequestParam("executeNote") String executeNote) {
        return emergencyPlanFlowRelService.completeTask(taskId, executeNote);
    }

    /**
     * 终止预案-终止流程实例（根据预案ID）
     */
    @PostMapping("/stop")
    public Result<String> terminateFlow(@RequestParam("planId") String planId) {
        return emergencyPlanFlowRelService.terminateFlow(planId);
    }

    /**
     * 终止预案-终止流程实例（根据流程实例ID）unused
     */
    @PostMapping("/stopByInst")
    public Result<String> terminateFlowByInstanceId(@RequestParam("procInstId") String procInstId) {
        return emergencyPlanFlowRelService.terminateFlowByInstanceId(procInstId);
    }

    /**
     * 查询流程历史（包括审批节点、处理人、耗时）
     */
    @GetMapping("/history")
    public String queryProcessHistory(@RequestParam("procInstId") String procInstId) {
        return emergencyPlanFlowRelService.queryProcessHistory(procInstId);
    }

    /**
     * 根据预案ID获取BPMN XML（供前端渲染）unused
     */
    @GetMapping("/bpmn")
    public Result<String> getBpmnXml(@RequestParam("planId") String planId) {
        return emergencyPlanFlowRelService.getBpmnXml(planId);
    }
}
