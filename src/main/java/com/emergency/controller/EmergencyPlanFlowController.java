package com.emergency.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.emergency.entity.EmergencyPlan;
import com.emergency.entity.EmergencyPlanFlowRel;
import com.emergency.mapper.EmergencyPlanFlowRelMapper;
import com.emergency.mapper.EmergencyPlanMapper;
import com.emergency.vo.DeployRequest;
import com.emergency.vo.Result;
import com.emergency.vo.TaskVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    // 根据部署ID查流程定义
    @Autowired
    private RepositoryService repositoryService;
    // Flowable实例启动服务
    @Autowired
    private RuntimeService runtimeService;
    // Flowable任务处理服务
    @Autowired
    private TaskService taskService;
    // Flowable历史查询服务
    @Autowired
    private HistoryService historyService;
    // 预案Mapper
    @Autowired
    private EmergencyPlanMapper emergencyPlanMapper;
    // 关联表Mapper
    @Autowired
    private EmergencyPlanFlowRelMapper relMapper;

    /**
     * 1. 部署流程（从预案表读取BPMN XML部署）
     *
     * @param request 预案ID
     */
    @PostMapping("/deploy")
    public Result<String> deployFlow(@RequestBody DeployRequest request) {
        try {
            String planId = request.getPlanId();
            // 1. 查询预案信息（获取BPMN XML）
            EmergencyPlan plan = emergencyPlanMapper.selectById(planId);
            if (plan == null || plan.getIsDeleted() == 1) {
                return Result.error(404, "预案不存在或已删除");
            }
            if (StringUtils.isEmpty(plan.getBpmnXml())) {
                return Result.error(400, "该预案未配置BPMN流程XML");
            }

            // 2. 部署BPMN XML到Flowable引擎 NOTES：act_re_deployment、act_re_procdef
            // note 创建部署
            Deployment deployment = repositoryService.createDeployment()
                    // 部署名称
                    .name("预案流程-" + plan.getPlanName())
                    // XML内容
                    .addString(planId + ".bpmn20.xml", plan.getBpmnXml())
                    .deploy();

            // note 查询流程定义
            List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                    // 根据部署ID查流程定义
                    .deploymentId(deployment.getId())
                    .list();
            log.info("查询流程定义：" + processDefinitions);

            // note 删除部署
            // repositoryService.deleteDeployment(deployment.getId());

            // 3. 保存部署关联关系（先查后更，避免重复） NOTES：业务保存Flowable部署ID
            QueryWrapper<EmergencyPlanFlowRel> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("plan_id", planId);
            EmergencyPlanFlowRel rel = relMapper.selectOne(queryWrapper);
            if (rel == null) {
                rel = new EmergencyPlanFlowRel();
                rel.setPlanId(planId);
                rel.setDeployId(deployment.getId());
                // 运行中
                rel.setFlowStatus(1);
                relMapper.insert(rel);
            } else {
                rel.setDeployId(deployment.getId());
                relMapper.updateById(rel);
            }

            return Result.success("流程部署成功", deployment.getId());
        } catch (Exception e) {
            log.error("部署流程失败", e);
            return Result.error("部署流程失败：" + e.getMessage());
        }
    }

    /**
     * 2. 启动流程实例（基于已部署的流程）
     *
     * @param planId 预案ID
     */
    @PostMapping("/start/{planId}")
    public Result<String> startFlow(@PathVariable String planId) {
        try {
            // 1. 查询预案和关联表信息 及 校验部署状态
            EmergencyPlan plan = emergencyPlanMapper.selectById(planId);
            if (plan == null || plan.getIsDeleted() == 1) {
                return Result.error(404, "预案不存在或已删除");
            }
            QueryWrapper<EmergencyPlanFlowRel> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("plan_id", planId);
            EmergencyPlanFlowRel rel = relMapper.selectOne(queryWrapper);
            if (rel == null || StringUtils.isEmpty(rel.getDeployId())) {
                return Result.error(400, "请先部署流程");
            }

            // 2. 检查是否已启动实例
            if (StringUtils.isNotEmpty(rel.getProcInstId())) {
                // 检查实例是否还在运行 // note 查询流程实例
                ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                        .processInstanceId(rel.getProcInstId())
                        .singleResult();
                if (instance != null) {
                    return Result.success("流程实例已启动", rel.getProcInstId());
                }
            }

            // 3. 通过部署ID查询流程定义，获取流程KEY（确保启动的是该部署对应的流程）
            List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                    // 根据部署ID查流程定义
                    .deploymentId(rel.getDeployId())
                    .list();
            if (processDefinitions.isEmpty()) {
                return Result.error(404, "该部署下无流程定义");
            }
            // 获取流程KEY（如xml.id："emptyProcess"）
            String processKey = processDefinitions.get(0).getKey();

            // 4. 启动流程实例（关联预案ID作为业务主键）
            // 传递预案ID到流程变量 // note 设置流程变量
            Map<String, Object> variables = new HashMap<>();
            variables.put("planId", planId);
            // variables.put("applicant", applicant);
            // variables.put("deptManager", deptManager);
            // variables.put("gm", gm);
            // variables.put("leaveDays", leaveDays);

            // 重载方法：startProcessInstanceByKey(流程KEY, 业务主键, 流程变量) note 启动流程
            // NOTES：act_ru_execution：IS_SCOPE_ = true 流程实例 | IS_SCOPE_ = false 执行实例
            // 、act_hi_procinst、act_hi_actinst
            ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                    // 流程KEY（必填）
                    processKey,
                    // 业务主键（关联预案ID，方便后续查询）
                    planId,
                    // 流程变量
                    variables);

            // 5. 查询启动后的第一个待办任务
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(instance.getId())
                    .active()
                    .list();
            String firstTaskId = "";
            String firstTaskName = "";
            if (!tasks.isEmpty()) {
                firstTaskId = tasks.get(0).getId();
                firstTaskName = tasks.get(0).getName();
            }

            // 6. 更新关联表（记录实例ID和首个任务）
            rel.setProcInstId(instance.getId());
            rel.setCurrentTaskId(firstTaskId);
            rel.setCurrentTaskName(firstTaskName);
            // 运行中
            rel.setFlowStatus(1);
            relMapper.updateById(rel);

            return Result.success("流程实例启动成功", instance.getId());
        } catch (Exception e) {
            log.error("启动流程失败", e);
            return Result.error("启动流程失败：" + e.getMessage());
        }
    }

    /**
     * 3. 查询当前待办任务(包含并行任务)（显示UI上的“当前步骤”）
     */
    @GetMapping("/task/{planId}")
    public Result<List<TaskVO>> getCurrentTask(@PathVariable String planId) {
        try {
            // 1. 查询预案流程关联记录
            EmergencyPlanFlowRel rel = relMapper.selectOne(
                    new LambdaQueryWrapper<EmergencyPlanFlowRel>()
                            .eq(EmergencyPlanFlowRel::getPlanId, planId)
                            .eq(EmergencyPlanFlowRel::getIsDeleted, 0));
            if (rel == null || rel.getProcInstId() == null || rel.getFlowStatus() != 1) {
                // 无关联记录/未运行，返回空列表
                return Result.success(new ArrayList<>());
            }

            // 2. 查询该流程实例下的所有活跃任务（并行网关会返回多个，用list()）
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(rel.getProcInstId())
                    .active()
                    .orderByTaskCreateTime()
                    .asc()
                    .list();

            // 3. 转换为VO，必须包含 bpmnElementId = task.getTaskDefinitionKey()
            List<TaskVO> taskVOList = tasks.stream().map(task -> {
                TaskVO vo = new TaskVO();
                vo.setTaskId(task.getId()); // 任务ID
                vo.setTaskName(task.getName()); // 任务名称
                vo.setCreateTime(task.getCreateTime()); // 创建时间
                vo.setBpmnElementId(task.getTaskDefinitionKey()); // 核心：BPMN节点ID，用于前端高亮
                vo.setPlanId(planId); // 预案ID
                return vo;
            }).toList();

            return Result.success(taskVOList);
        } catch (Exception e) {
            log.error("获取待办任务失败", e);
            return Result.error("获取待办任务失败：" + e.getMessage());
        }
    }

    /**
     * 4. 下一步：完成当前任务，流程流转到下一个节点
     *
     * @param taskId 待办任务ID
     */
    @PostMapping("/next/{taskId}")
    public Result<Map<String, Object>> completeTask(@PathVariable String taskId) {
        try {
            // 1. 校验任务是否存在 // note 查询任务
            Task task = taskService.createTaskQuery()
                    .taskId(taskId)
                    .singleResult();
            if (task == null) {
                return Result.error(404, "任务不存在或已完成");
            }

            // 先缓存流程实例ID
            String procInstId = task.getProcessInstanceId();

            // 2. 优先从任务的流程变量获取planId，兼容流程最后一步（processInstance为null）
            String planId = (String) runtimeService.getVariable(procInstId, "planId");
            // 兜底：从流程实例的businessKey获取（需启动流程时设置）
            if (planId == null || planId.trim().isEmpty()) {
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                        .processInstanceId(procInstId)
                        .singleResult();
                if (processInstance != null) {
                    planId = processInstance.getBusinessKey();
                }
            }
            // 双重兜底：若仍为空，直接从关联表反向查询（终极方案，杜绝null）
            if (planId == null || planId.trim().isEmpty()) {
                EmergencyPlanFlowRel rel = relMapper.selectOne(
                        new LambdaQueryWrapper<EmergencyPlanFlowRel>()
                                .eq(EmergencyPlanFlowRel::getProcInstId, procInstId)
                                .eq(EmergencyPlanFlowRel::getIsDeleted, 0));
                if (rel != null) {
                    planId = rel.getPlanId();
                }
            }
            // 最终校验
            if (planId == null || planId.trim().isEmpty()) {
                return Result.error(400, "未关联预案ID，请检查流程启动时是否绑定预案");
            }

            // 3. 完成当前任务（核心：Flowable自动流转到下一个节点/结束流程）
            taskService.complete(taskId);

            // 4. 查询下一个待办任务（判断流程是否结束）
            List<Task> nextTasks = taskService.createTaskQuery()
                    .processInstanceId(procInstId)
                    .active()
                    .list();

            Map<String, Object> result = new HashMap<>();
            EmergencyPlanFlowRel rel = relMapper.selectOne(
                    new LambdaQueryWrapper<EmergencyPlanFlowRel>()
                            .eq(EmergencyPlanFlowRel::getPlanId, planId)
                            .eq(EmergencyPlanFlowRel::getIsDeleted, 0));
            if (rel == null) {
                return Result.error(404, "预案流程关联记录不存在");
            }

            if (nextTasks.isEmpty()) {
                // 无下一个任务 → 流程结束，更新关联表状态为【已结束】
                // 2-已结束
                rel.setFlowStatus(2);
                rel.setCurrentTaskId("");
                rel.setCurrentTaskName("");
                result.put("msg", "流程已完成");
                result.put("nextTaskId", "");
                result.put("nextTaskName", "流程结束");
            } else if (nextTasks.size() == 1) {
                // 单个任务逻辑 有下一个任务 → 返回第一个任务信息，更新关联表
                Task nextTask = nextTasks.get(0);
                rel.setCurrentTaskId(nextTask.getId());
                rel.setCurrentTaskName(nextTask.getName());
                result.put("msg", "流程已流转到下一步");
                result.put("isParallelTask", false);
                result.put("nextTaskId", nextTask.getId());
                result.put("nextTaskName", nextTask.getName());
            } else {
                // 并行任务逻辑：返回所有任务信息
                result.put("msg", "流程已流转到多个并行任务");
                result.put("isParallelTask", true);
                // 构建并行任务列表
                String parallelTaskIds = "";
                String parallelTaskNames = "";
                for (Task nextTask : nextTasks) {
                    parallelTaskIds += nextTask.getId() + ",";
                    parallelTaskNames += nextTask.getName() + ",";
                }
                rel.setCurrentTaskId(parallelTaskIds);
                rel.setCurrentTaskName(parallelTaskNames);
                // 并行任务逻辑：返回所有任务信息
                result.put("msg", "流程已流转到多个并行任务");
                result.put("isParallelTask", true);
                result.put("nextTaskIds", parallelTaskIds);
                result.put("nextTaskNames", parallelTaskNames);
            }

            // 统一更新关联表，避免重复代码
            // rel.setUpdateTime(LocalDateTime.now());
            relMapper.updateById(rel);

            return Result.success(result);
        } catch (Exception e) {
            log.error("完成任务失败", e);
            return Result.error("完成任务失败：" + e.getMessage());
        }
    }

    /**
     * 5. 查询流程历史（包括审批节点、处理人、耗时）
     */
    @GetMapping("/history/{procInstId}")
    public String queryProcessHistory(@PathVariable String procInstId) {
        // 查询流程实例基本信息 // note 查询历史流程
        HistoricProcessInstance historicProcInst = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(procInstId)
                .singleResult();

        // 查询流程所有任务的执行记录 // note 查询历史任务
        List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(procInstId)
                .orderByTaskCreateTime()
                .asc()
                .list();

        // 查询流程实例变量信息 // note 查询历史变量
        List<HistoricVariableInstance> variableInstances = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(procInstId)
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
     * 6. 辅助接口：根据预案ID获取BPMN XML（供前端渲染）
     *
     * @param planId 预案ID
     */
    @GetMapping("/bpmn/{planId}")
    public Result<String> getBpmnXml(@PathVariable String planId) {
        EmergencyPlan plan = emergencyPlanMapper.selectById(planId);
        if (plan == null) {
            return Result.error(404, "预案不存在");
        }
        return Result.success(plan.getBpmnXml());
    }
}