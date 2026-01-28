package com.emergency.controller;

import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.idm.api.User;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/leave")
public class FlowableExampleController {


    @Autowired
    private RepositoryService repositoryService;

    // 流程运行时服务：启动流程实例、设置流程变量等
    @Autowired
    private RuntimeService runtimeService;

    // 任务服务：查询任务、完成任务、认领任务等
    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private IdentityService identityService;

    // ####################### 常规 API 示例 ###############################

    /**
     * 1. 启动请假流程实例
     *
     * @param applicant   申请人
     * @param deptManager 部门经理
     * @param gm          总经理
     * @param leaveDays   请假天数
     * @return 流程实例ID
     */
    @PostMapping("/start")
    public String startLeaveProcess(
            @RequestParam String applicant,
            @RequestParam String deptManager,
            @RequestParam String gm,
            @RequestParam Integer leaveDays) {

        // 设置流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("applicant", applicant);
        variables.put("deptManager", deptManager);
        variables.put("gm", gm);
        variables.put("leaveDays", leaveDays);

        // 启动流程实例（流程定义ID为leaveProcess）
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leaveProcess", variables);
        return "流程启动成功，流程实例ID：" + processInstance.getId();
    }

    /**
     * 2. 查询指定用户的待办任务
     *
     * @param assignee 处理人
     * @return 待办任务列表
     */
    @GetMapping("/tasks")
    public List<Task> getTasks(@RequestParam String assignee) {
        // 查询指定处理人的未完成任务
        return taskService.createTaskQuery()
                .taskAssignee(assignee)
                .orderByTaskCreateTime()
                .desc()
                .list();
    }

    /**
     * 3. 完成任务（审批通过）
     *
     * @param taskId 任务ID
     * @return 操作结果
     */
    @PostMapping("/complete")
    public String completeTask(@RequestParam String taskId) {
        // 完成任务（如果需要传递审批意见，可添加流程变量）
        taskService.complete(taskId);
        return "任务[" + taskId + "]已完成";
    }

    // ####################### 进阶 API 示例 ###############################

    /**
     * 1. 暂停/激活流程定义
     */
    @GetMapping("/process-def/{procDefKey}/{action}")
    public String controlProcessDef(
            @PathVariable String procDefKey,
            @PathVariable String action) {
        // 查询最新版本的流程定义
        String procDefId = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(procDefKey)
                .latestVersion()
                .singleResult()
                .getId();

        if ("suspend".equals(action)) {
            repositoryService.suspendProcessDefinitionById(procDefId);
            return "流程定义[" + procDefKey + "]已暂停";
        } else if ("activate".equals(action)) {
            repositoryService.activateProcessDefinitionById(procDefId);
            return "流程定义[" + procDefKey + "]已激活";
        } else {
            return "不支持的操作：" + action;
        }
    }

    /**
     * 2. 任务委派（将任务转给其他人处理）
     */
    @GetMapping("/task/delegate/{taskId}/{userId}")
    public String delegateTask(
            @PathVariable String taskId,
            @PathVariable String userId) {
        // 委派任务
        taskService.delegateTask(taskId, userId);
        return "任务[" + taskId + "]已委派给用户[" + userId + "]";
    }

    /**
     * 3. 查询流程历史（包括审批节点、处理人、耗时）
     */
    @GetMapping("/history/proc-inst/{procInstId}")
    public String queryProcessHistory(@PathVariable String procInstId) {
        // 查询流程实例基本信息
        HistoricProcessInstance historicProcInst = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(procInstId)
                .singleResult();

        // 查询流程所有任务的执行记录
        List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(procInstId)
                .orderByTaskCreateTime()
                .asc()
                .list();

        StringBuilder sb = new StringBuilder();
        sb.append("流程实例：").append(historicProcInst.getProcessDefinitionName())
                .append("，业务主键：").append(historicProcInst.getBusinessKey())
                .append("，启动时间：").append(historicProcInst.getStartTime())
                .append("，结束时间：").append(historicProcInst.getEndTime())
                .append("，总耗时：").append(historicProcInst.getDurationInMillis() / 1000).append("秒\n");

        for (HistoricTaskInstance task : historicTasks) {
            sb.append("任务：").append(task.getName())
                    .append("，处理人：").append(task.getAssignee())
                    .append("，创建时间：").append(task.getCreateTime())
                    .append("，完成时间：").append(task.getEndTime())
                    .append("，耗时：").append(task.getDurationInMillis() / 1000).append("秒\n");
        }
        return sb.toString();
    }

    /**
     * 4. 内置用户管理（创建用户）
     */
    @GetMapping("/user/create/{userId}/{username}/{password}")
    public String createUser(
            @PathVariable String userId,
            @PathVariable String username,
            @PathVariable String password) {
        // note createUserQuery ()（查询用户）、createGroupQuery ()（查询组）、saveUser ()（保存用户）
        User user = identityService.newUser(userId);
        user.setFirstName(username);
        user.setPassword(password);
        identityService.saveUser(user);
        return "用户[" + userId + "]创建成功";
    }
}
