package com.siasun.iscs.ep.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siasun.iscs.ep.entity.EmergencyFlowInstHi;
import com.siasun.iscs.ep.entity.EmergencyPlan;
import com.siasun.iscs.ep.entity.EmergencyPlanFlowRel;
import com.siasun.iscs.ep.mapper.EmergencyFlowInstHiMapper;
import com.siasun.iscs.ep.mapper.EmergencyPlanFlowRelMapper;
import com.siasun.iscs.ep.mapper.EmergencyPlanMapper;
import com.siasun.iscs.ep.service.EmergencyFlowInstHiService;
import com.siasun.iscs.ep.service.EmergencyPlanFlowRelService;
import com.siasun.iscs.ep.vo.EmergencyCurrentTaskQueryVO;
import com.siasun.iscs.ep.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricDetail;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liao
 * @version 1.0
 * @description: 核心业务接口（流程控制）
 * @date 2026/6/9
 */
@Slf4j
@Service
public class EmergencyPlanFlowRelServiceImpl extends ServiceImpl<EmergencyPlanFlowRelMapper, EmergencyPlanFlowRel> implements EmergencyPlanFlowRelService {

    // Flowable仓库服务
    @Resource
    private RepositoryService repositoryService;
    // Flowable实例服务
    @Resource
    private RuntimeService runtimeService;
    // Flowable任务服务
    @Resource
    private TaskService taskService;
    // Flowable历史服务
    @Resource
    private HistoryService historyService;
    @Resource
    private EmergencyPlanMapper emergencyPlanMapper;
    @Resource
    private EmergencyPlanFlowRelMapper relMapper;
    @Resource
    private EmergencyFlowInstHiMapper instHiMapper;
    @Resource
    private EmergencyFlowInstHiService emergencyFlowInstHiService;

    /**
     * 发布预案-创建部署
     */
    @Override
    public Result<String> deployFlow(String planId) {
        try {
            // 1. 查询预案信息
            EmergencyPlan plan = emergencyPlanMapper.selectById(planId);
            if (plan == null || plan.getIsDeleted() == 1) {
                return Result.error(404, "预案不存在或已删除");
            }
            if (StringUtils.isEmpty(plan.getBpmnXml())) {
                return Result.error(400, "该预案未配置BPMN流程XML");
            }
            // 2. 查询关联表记录（检查是否已有运行中的流程）
            QueryWrapper<EmergencyPlanFlowRel> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("plan_id", planId);
            EmergencyPlanFlowRel rel = relMapper.selectOne(queryWrapper);
            if (rel != null && rel.getFlowStatus() != null && rel.getFlowStatus() == 1) {
                return Result.error(400, "该预案已有运行中的流程，请勿重复部署");
            }
            // 3. 创建 Flowable 部署【创建部署】 （act_re_deployment、act_re_procdef、）
            Deployment deployment = repositoryService.createDeployment()
                    .name("预案流程-" + plan.getPlanName())
                    .addString(planId + ".bpmn20.xml", plan.getBpmnXml())
                    .deploy();
            // 4. 【查询流程定义】
            List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .list();
            log.info("查询流程定义：" + processDefinitions);
            // 5. 创建关联表记录
            if (rel == null) {
                rel = new EmergencyPlanFlowRel();
                rel.setPlanId(planId);
                rel.setDeployId(deployment.getId());
                rel.setStation(plan.getStation());
                // TODO 临时处理：操作人
                rel.setOptName("默认32");
                rel.setFlowStatus(0);
                relMapper.insert(rel);
            }
            // 6. 更新预案状态为 1-已发布
            emergencyPlanMapper.update(null,
                    new LambdaUpdateWrapper<EmergencyPlan>()
                            .eq(EmergencyPlan::getId, planId)
                            .eq(EmergencyPlan::getIsDeleted, 0)
                            .set(EmergencyPlan::getPlanStatus, 1));
            return Result.success("流程部署成功", deployment.getId());
        } catch (Exception e) {
            log.error("部署流程失败", e);
            return Result.error("部署流程失败：" + e.getMessage());
        }
    }

    /**
     * 撤回预案-删除部署
     */
    @Override
    public Result<String> revokeFlow(String planId) {
        try {
            // 1. 查询预案信息
            EmergencyPlan plan = emergencyPlanMapper.selectById(planId);
            if (plan == null || plan.getIsDeleted() == 1) {
                return Result.error(404, "预案不存在或已删除");
            }
            // 2. 查询关联表记录
            QueryWrapper<EmergencyPlanFlowRel> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("plan_id", planId);
            EmergencyPlanFlowRel rel = relMapper.selectOne(queryWrapper);
            if (rel == null) {
                return Result.error(400, "该预案未部署流程");
            }
            // 3. 检查是否有运行中的流程实例
            if (rel.getFlowStatus() != null && rel.getFlowStatus() == 1) {
                return Result.error(400, "该预案流程正在运行中，无法撤回");
            }
            // 4. 删除 Flowable 部署【删除部署】
            if (StringUtils.isNotEmpty(rel.getDeployId())) {
                repositoryService.deleteDeployment(rel.getDeployId(), true);
            }
            // 5. 更新关联表记录
            relMapper.updateById(rel);
            // 逻辑删除：（撤回预案后可重新编辑，xml可能有所变化，保证再次发布时创建新的部署实例）
            relMapper.deleteById(rel.getId());
            // 6. 更新预案状态为 0-未发布
            emergencyPlanMapper.update(null,
                    new LambdaUpdateWrapper<EmergencyPlan>()
                            .eq(EmergencyPlan::getId, planId)
                            .eq(EmergencyPlan::getIsDeleted, 0)
                            .set(EmergencyPlan::getPlanStatus, 0));
            return Result.success("流程撤回成功", rel.getDeployId());
        } catch (Exception e) {
            log.error("撤回流程失败", e);
            return Result.error("撤回流程失败：" + e.getMessage());
        }
    }

    /**
     * 启动预案-启动流程实例（基于已部署的流程）
     */
    @Override
    public Result<String> startFlow(String planId) {
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
            // 2. 检查是否已启动实例【查询流程实例】
            if (StringUtils.isNotEmpty(rel.getCurProcInstId())) {
                ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                        .processInstanceId(rel.getCurProcInstId())
                        .singleResult();
                if (instance != null) {
                    return Result.success("流程实例已启动", rel.getCurProcInstId());
                }
            }
            // 3. 通过部署ID查询流程定义，获取流程KEY（确保启动的是该部署对应的流程）
            List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(rel.getDeployId())
                    .list();
            if (processDefinitions.isEmpty()) {
                return Result.error(404, "该部署下无流程定义");
            }
            // 获取流程KEY（如xml.id："emptyProcess"）
            String processKey = processDefinitions.get(0).getKey();
            log.info("流程KEY：{}", processKey);
            // 4. 启动流程实例（关联预案ID作为业务主键）【启动流程实例】
            Map<String, Object> variables = new HashMap<>();
            // 传递预案ID到流程变量 【设置流程变量】
            variables.put("planId", planId);
            // 【启动流程实例】
            // （act_ru_execution：IS_SCOPE_ = true 流程实例 | IS_SCOPE_ = false 执行实例、act_hi_procinst、act_hi_actinst）
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
            // 6. 保存流程实例关联关系（此处的procInstId每次start都更新覆盖）
            rel.setCurProcInstId(instance.getId());
            rel.setCurrentTaskId(firstTaskId);
            rel.setCurrentTaskName(firstTaskName);
            rel.setFlowStatus(1);
            relMapper.updateById(rel);

            // 7. 保存流程实例历史
            EmergencyFlowInstHi instHi = new EmergencyFlowInstHi();
            BeanUtils.copyProperties(plan, instHi, "id", "createTime", "updateTime", "isDeleted");
            instHi.setPlanId(plan.getId());
            instHi.setDeployId(instance.getDeploymentId());
            instHi.setHiProcInstId(instance.getId());
            instHi.setStartTime(LocalDateTime.now());
            instHi.setIsHistory(0);
            // TODO 临时处理：操作人
            instHi.setOptName("默认78");
            instHiMapper.insert(instHi);

            // 更新预案状态为 2-执行中
            emergencyPlanMapper.update(null,
                    new LambdaUpdateWrapper<EmergencyPlan>()
                            .eq(EmergencyPlan::getId, planId)
                            .eq(EmergencyPlan::getIsDeleted, 0)
                            .set(EmergencyPlan::getPlanStatus, 2));
            return Result.success("流程实例启动成功", instance.getId());
        } catch (Exception e) {
            log.error("启动流程失败", e);
            return Result.error("启动流程失败：" + e.getMessage());
        }
    }

    /**
     * 查询当前待办任务(包含并行任务)（显示UI上的“当前任务”）
     */
    @Override
    public Result<List<EmergencyCurrentTaskQueryVO>> getCurrentTask(String planId) {
        try {
            // 1. 查询预案流程关联记录
            EmergencyPlanFlowRel rel = relMapper.selectOne(
                    new LambdaQueryWrapper<EmergencyPlanFlowRel>()
                            .eq(EmergencyPlanFlowRel::getPlanId, planId)
                            .eq(EmergencyPlanFlowRel::getIsDeleted, 0));
            if (rel == null || rel.getCurProcInstId() == null || rel.getFlowStatus() != 1) {
                return Result.success(new ArrayList<>());
            }
            // 2. 查询该流程实例下的所有活跃任务（并行网关会返回多个，用list）
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(rel.getCurProcInstId())
                    .active()
                    .orderByTaskCreateTime()
                    .asc()
                    .list();
            // 3.  转换为VO，必须包含 bpmnElementId
            List<EmergencyCurrentTaskQueryVO> taskVOList = tasks.stream().map(task -> {
                EmergencyCurrentTaskQueryVO vo = new EmergencyCurrentTaskQueryVO();
                vo.setPlanId(planId);
                vo.setTaskId(task.getId());
                vo.setTaskName(task.getName());
                // 核心：BPMN节点ID，用于前端高亮
                vo.setBpmnElementId(task.getTaskDefinitionKey());
                vo.setCreateTime(task.getCreateTime());
                return vo;
            }).toList();
            return Result.success(taskVOList);
        } catch (Exception e) {
            log.error("查询当前任务失败", e);
            return Result.error("获取待办任务失败：" + e.getMessage());
        }
    }

    /**
     * 完成步骤-流程流转到下一个节点
     */
    @Override
    public Result<Map<String, Object>> completeTask(String taskId, String executeNote) {
        try {
            // 1. 校验任务是否存在
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
            if (planId == null || planId.trim().isEmpty()) {
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                        .processInstanceId(procInstId)
                        .singleResult();
                if (processInstance != null) {
                    planId = processInstance.getBusinessKey();
                }
            }
            // 双重兜底：若仍为空，直接从关联表反向查询（杜绝null）
            if (planId == null || planId.trim().isEmpty()) {
                EmergencyPlanFlowRel rel = relMapper.selectOne(
                        new LambdaQueryWrapper<EmergencyPlanFlowRel>()
                                .eq(EmergencyPlanFlowRel::getCurProcInstId, procInstId)
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
            // 5. 获取下一个任务信息
            if (nextTasks.isEmpty()) {
                // (1)流程结束
                rel.setFlowStatus(2);
                result.put("msg", "流程已完成");
                result.put("nextTaskId", "");
                result.put("nextTaskName", "流程结束");
                emergencyPlanMapper.update(null,
                        new LambdaUpdateWrapper<EmergencyPlan>()
                                .eq(EmergencyPlan::getId, planId)
                                .eq(EmergencyPlan::getIsDeleted, 0)
                                .set(EmergencyPlan::getPlanStatus, 1));
                // 更新流程实例历史表
                EmergencyFlowInstHi instHi = instHiMapper.selectOne(
                        new QueryWrapper<EmergencyFlowInstHi>()
                                .eq("hi_proc_inst_id", rel.getCurProcInstId()));
                if (instHi != null) {
                    instHi.setEndTime(LocalDateTime.now());
                    instHi.setIsHistory(1);
                    instHiMapper.updateById(instHi);
                    // 异步任务保存录像回放
                    emergencyFlowInstHiService.asyncSaveVideoPlayback(instHi.getHiProcInstId());
                }
            } else if (nextTasks.size() == 1) {
                // (2)流程未结束
                Task nextTask = nextTasks.get(0);
                rel.setCurrentTaskId(nextTask.getId());
                rel.setCurrentTaskName(nextTask.getName());
                result.put("msg", "流程已流转到下一步");
                result.put("isParallelTask", false);
                result.put("nextTaskId", nextTask.getId());
                result.put("nextTaskName", nextTask.getName());
            } else {
                // (3)并行任务
                StringBuilder parallelTaskIds = new StringBuilder();
                StringBuilder parallelTaskNames = new StringBuilder();
                for (Task nextTask : nextTasks) {
                    parallelTaskIds.append(nextTask.getId()).append(",");
                    parallelTaskNames.append(nextTask.getName()).append(",");
                }
                rel.setCurrentTaskId(parallelTaskIds.toString());
                rel.setCurrentTaskName(parallelTaskNames.toString());
                result.put("msg", "流程已流转到多个并行任务");
                result.put("isParallelTask", true);
                result.put("nextTaskId", parallelTaskIds.toString());
                result.put("nextTaskName", parallelTaskNames.toString());
            }
            // 统一更新关联表
            relMapper.updateById(rel);
            return Result.success(result);
        } catch (Exception e) {
            log.error("完成任务失败", e);
            return Result.error("完成任务失败：" + e.getMessage());
        }
    }

    /**
     * 终止预案-终止流程实例（根据预案ID）
     */
    @Override
    public Result<String> terminateFlow(String planId) {
        try {
            // 1. 查询预案和关联表信息
            EmergencyPlan plan = emergencyPlanMapper.selectById(planId);
            if (plan == null || plan.getIsDeleted() == 1) {
                return Result.error(404, "预案不存在或已删除");
            }
            // 2. 查询预案流程关联记录
            EmergencyPlanFlowRel rel = relMapper.selectOne(
                    new LambdaQueryWrapper<EmergencyPlanFlowRel>()
                            .eq(EmergencyPlanFlowRel::getPlanId, planId)
                            .eq(EmergencyPlanFlowRel::getIsDeleted, 0));
            if (rel == null || rel.getCurProcInstId() == null) {
                return Result.error(400, "该预案未启动流程实例");
            }
            String procInstId = rel.getCurProcInstId();
            // 3. 检查流程实例是否存在且正在运行
            ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(procInstId)
                    .singleResult();
            if (instance == null) {
                return Result.error(400, "流程实例不存在或已结束");
            }
            // 4. 终止流程实例【删除流程实例】
            runtimeService.deleteProcessInstance(procInstId, "管理员终止流程");
            // 5. 更新关联表状态为 3-终止
            rel.setFlowStatus(3);
            relMapper.updateById(rel);
            // 6. 更新流程实例历史表
            EmergencyFlowInstHi instHi = instHiMapper.selectOne(
                    new QueryWrapper<EmergencyFlowInstHi>()
                            .eq("hi_proc_inst_id", rel.getCurProcInstId()));
            if (instHi != null) {
                instHi.setEndTime(LocalDateTime.now());
                instHi.setIsHistory(1);
                instHiMapper.updateById(instHi);
                // 异步任务保存录像回放
                emergencyFlowInstHiService.asyncSaveVideoPlayback(instHi.getHiProcInstId());
            }
            // 7. 更新预案状态为 1-已发布
            emergencyPlanMapper.update(null,
                    new LambdaUpdateWrapper<EmergencyPlan>()
                            .eq(EmergencyPlan::getId, planId)
                            .eq(EmergencyPlan::getIsDeleted, 0)
                            .set(EmergencyPlan::getPlanStatus, 1));
            return Result.success("流程实例已终止", procInstId);
        } catch (Exception e) {
            log.error("终止流程实例失败", e);
            return Result.error("终止流程实例失败：" + e.getMessage());
        }
    }

    /**
     * 终止预案-终止流程实例（根据流程实例ID）
     */
    @Override
    public Result<String> terminateFlowByInstanceId(String procInstId) {
        try {
            // 1. 检查流程实例是否存在且正在运行
            ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(procInstId)
                    .singleResult();
            if (instance == null) {
                return Result.error(400, "流程实例不存在");
            }
            // 2. 终止流程实例【删除流程实例】
            runtimeService.deleteProcessInstance(procInstId, "管理员终止流程");
            // 3. 查询预案流程关联记录
            EmergencyPlanFlowRel rel = relMapper.selectOne(
                    new LambdaQueryWrapper<EmergencyPlanFlowRel>()
                            .eq(EmergencyPlanFlowRel::getCurProcInstId, procInstId)
                            .eq(EmergencyPlanFlowRel::getIsDeleted, 0));
            if (rel == null || rel.getCurProcInstId() == null) {
                return Result.error(400, "该预案未启动流程实例");
            }
            // 4. 更新关联表状态为 3-终止
            rel.setFlowStatus(3);
            relMapper.updateById(rel);
            // 5. 更新流程实例历史表
            EmergencyFlowInstHi instHi = instHiMapper.selectOne(
                    new QueryWrapper<EmergencyFlowInstHi>()
                            .eq("hi_proc_inst_id", rel.getCurProcInstId()));
            if (instHi != null) {
                instHi.setEndTime(LocalDateTime.now());
                instHi.setIsHistory(1);
                instHiMapper.updateById(instHi);
                // 异步任务保存录像回放
                emergencyFlowInstHiService.asyncSaveVideoPlayback(instHi.getHiProcInstId());
            }
            // 6. 更新预案状态为 1-已发布
            emergencyPlanMapper.update(null,
                    new LambdaUpdateWrapper<EmergencyPlan>()
                            .eq(EmergencyPlan::getId, rel.getPlanId())
                            .eq(EmergencyPlan::getIsDeleted, 0)
                            .set(EmergencyPlan::getPlanStatus, 1));
            return Result.success("流程实例已终止", procInstId);
        } catch (Exception e) {
            log.error("终止流程实例失败", e);
            return Result.error("终止流程实例失败：" + e.getMessage());
        }
    }

    /**
     * 查询流程历史（包括审批节点、处理人、耗时）
     * <p>业务流程：
     * 1. 查询历史流程实例（act_hi_procinst）：每次start对应一条
     * 2. 查询历史活动节点（act_hi_actinst）：每个节点的进入/离开
     * 3. 查询历史任务实例（act_hi_taskinst）：每次任务创建/完成
     * 4. 查询历史流程变量（act_hi_varinst）：流程中设置的变量
     * 5. 查询历史详细变更（act_hi_detail）：最细粒度的变更记录
     *
     * @param procInstId 流程实例ID
     * @return 流程历史数据（当前返回null，待完善返回结构）
     */
    @Override
    public String queryProcessHistory(String procInstId) {
        // 1. 查询历史流程实例（act_hi_procinst）：每次start对应一条
        if (procInstId == null || procInstId.isEmpty()) {
            List<HistoricProcessInstance> historicProcessInstanceList = historyService.createHistoricProcessInstanceQuery()
                    .list();
            log.info("历史流程实例：{}", historicProcessInstanceList);
        } else {
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(procInstId)
                    .singleResult();
            log.info("历史流程实例：{}", historicProcessInstance);
        }

        // 2. 查询历史活动节点（act_hi_actinst）：每个节点的进入/离开
        List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(procInstId)
                .list();
        log.info("历史活动节点：{}", historicActivityInstances);

        // 3. 查询历史任务实例（act_hi_taskinst）：每次任务创建/完成
        List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(procInstId)
                .orderByTaskCreateTime()
                .asc()
                .list();
        log.info("历史任务实例：{}", historicTasks);

        // 4. 查询历史流程变量（act_hi_varinst）：流程中设置的变量
        List<HistoricVariableInstance> variableInstances = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(procInstId)
                .list();
        log.info("历史流程变量：{}", variableInstances);

        // 5. 查询历史详细变更（act_hi_detail）：最细粒度的变更记录
        List<HistoricDetail> details = historyService.createHistoricDetailQuery()
                .processInstanceId(procInstId)
                .list();
        log.info("历史详细变更：{}", details);

        // TODO 封装返回结构（审批节点、处理人、耗时等）
        return null;
    }

    /**
     * 根据预案ID获取BPMN XML
     * <p>业务流程：
     * 1. 根据预案ID查询主表记录
     * 2. 返回 bpmnXml 字段，供前端渲染流程图
     *
     * @param planId 预案主键ID
     * @return BPMN XML字符串
     */
    @Override
    public Result<String> getBpmnXml(String planId) {
        EmergencyPlan plan = emergencyPlanMapper.selectById(planId);
        if (plan == null) {
            return Result.error(404, "预案不存在");
        }
        return Result.success(plan.getBpmnXml());
    }
}
