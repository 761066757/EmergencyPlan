package com.siasun.iscs.ep.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siasun.iscs.ep.entity.EmergencyPlanFlowRel;
import com.siasun.iscs.ep.vo.EmergencyCurrentTaskQueryVO;
import com.siasun.iscs.ep.vo.Result;

import java.util.List;
import java.util.Map;

/**
 * @author liao
 * @version 1.0
 * @description: 应急预案流程关联服务接口
 * @date 2026/6/4
 */
public interface EmergencyPlanFlowRelService extends IService<EmergencyPlanFlowRel> {

    /**
     * 发布预案-创建部署
     */
    Result<String> deployFlow(String planId);

    /**
     * 撤回预案-删除部署
     */
    Result<String> revokeFlow(String planId);

    /**
     * 启动预案-启动流程实例
     */
    Result<String> startFlow(String planId);

    /**
     * 查询当前待办任务(包含并行任务)
     */
    Result<List<EmergencyCurrentTaskQueryVO>> getCurrentTask(String planId);

    /**
     * 完成步骤-流程流转到下一个节点
     */
    Result<Map<String, Object>> completeTask(String taskId, String executeNote);

    /**
     * 终止预案-终止流程实例（根据预案ID）
     */
    Result<String> terminateFlow(String planId);

    /**
     * 终止预案-终止流程实例（根据流程实例ID）
     */
    Result<String> terminateFlowByInstanceId(String procInstId);

    /**
     * 查询流程历史（包括审批节点、处理人、耗时）
     */
    String queryProcessHistory(String procInstId);

    /**
     * 根据预案ID获取BPMN XML（供前端渲染）
     */
    Result<String> getBpmnXml(String planId);
}
