package com.emergency.controller;

import com.emergency.vo.Result;
import lombok.Data;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * @description: TODO
 * @author liao
 * @date 2026/1/14
 * @version 1.0
 */

@RestController
@RequestMapping("/emergency/flow")
public class EmergencyFlowTestController {
    @Autowired
    private RepositoryService repositoryService; // 流程部署
    @Autowired
    private RuntimeService runtimeService;       // 流程启动
    @Autowired
    private TaskService taskService;             // 任务处理（下一步核心）
    @Autowired
    private HistoryService historyService;       // 流程历史

    // 1. 部署流程（项目启动时执行，也可手动调用）
    @PostMapping("/deploy")
    public Result deployFlow() {
        // 避免重复部署
        long count = repositoryService.createDeploymentQuery()
                .deploymentName("应急预案流程")
                .count();
        if (count > 0) {
            return Result.success("流程已部署");
        }
        // 部署XML文件
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("processes/emergency-plan-test.bpmn20.xml")
                .name("应急预案流程")
                .deploy();
        return Result.success("流程部署成功", deployment.getId());
    }

    // 2. 启动流程（关联预案ID）
    @PostMapping("/start/{planId}")
    public Result startFlow(@PathVariable String planId) {
        // 设置流程变量（传递业务参数）
        Map<String, Object> variables = new HashMap<>();
        variables.put("planId", planId);
        // 启动流程实例
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                "emergencyPlanProcess", // 对应XML中的process id
                planId, // 业务主键（关联预案ID）
                variables
        );
        return Result.success("流程启动成功", instance.getId());
    }

    // 3. 查询当前待办任务（显示UI上的“当前步骤”）
    @GetMapping("/task/{planId}")
    public Result getCurrentTask(@PathVariable String planId) {
        // 根据预案ID（业务主键）查询待办任务
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceBusinessKey(planId)
                .active() // 只查未完成的任务
                .list();
        if (tasks.isEmpty()) {
            return Result.success("无待办任务，流程已结束");
        }
        // 封装任务信息（给前端显示）
        Map<String, Object> taskInfo = new HashMap<>();
        Task currentTask = tasks.get(0);
        taskInfo.put("taskId", currentTask.getId()); // 任务ID（下一步需要）
        taskInfo.put("taskName", currentTask.getName()); // 步骤名称
        //taskInfo.put("stepId", currentTask.getVariable("stepId")); // 自定义步骤ID
        return Result.success("查询待办任务成功", taskInfo);
    }

    // 4. 下一步：完成当前任务，流程流转到下一个节点
    @PostMapping("/next/{taskId}")
    public Result completeTask(@PathVariable String taskId) {
        try {
            // 完成任务（核心：调用complete后，流程自动流转到下一个节点）
            taskService.complete(taskId);
            // 查询完成后的下一个任务（返回给前端）
            Task nextTask = taskService.createTaskQuery()
                    .taskId(taskId)
                    .active()
                    .singleResult();
            if (nextTask == null) {
                return Result.success("当前任务已完成，流程结束");
            }
            Map<String, Object> nextTaskInfo = new HashMap<>();
            nextTaskInfo.put("taskId", nextTask.getId());
            nextTaskInfo.put("taskName", nextTask.getName());
            return Result.success("流程已流转到下一步", nextTaskInfo);
        } catch (Exception e) {
            return Result.error(500, "完成任务失败：" + e.getMessage());
        }
    }

    /**
     * 获取 BPMN XML 接口
     */
    @GetMapping("/getBpmnXml")
    public Result getBpmnXml(@RequestParam String processKey) {
        // 根据流程Key查询部署的BPMN XML
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .latestVersion() // 获取最新版本
                .singleResult();
        if (processDefinition == null) {
            return Result.error(404, "流程定义不存在");
        }
        // 读取BPMN XML内容
        String bpmnXml = String.valueOf(repositoryService.getResourceAsStream(
                processDefinition.getDeploymentId(),
                processDefinition.getResourceName()
        ));
        return Result.success("获取BPMN XML成功", bpmnXml);
    }
}