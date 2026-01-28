package com.emergency.util;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author liao
 * @version 1.0
 * @description: 自定义监听器-流程启动/结束监听器
 * @date 2026/1/26
 */
@Component
public class ProcessLifeCycleListener implements ExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(ProcessLifeCycleListener.class);

    @Override
    public void notify(DelegateExecution execution) {
        String eventName = execution.getEventName();
        String procInstId = execution.getProcessInstanceId();
        String procDdfId = execution.getProcessDefinitionId();

        if ("start".equals(eventName)) {
            logger.info("流程实例[{}]启动，流程定义[{}]", procInstId, procDdfId);
            // 可执行：发送启动通知、初始化业务数据等
        } else if ("end".equals(eventName)) {
            logger.info("流程实例[{}]结束，流程定义[{}]", procInstId, procDdfId);
            // 可执行：更新业务状态、发送结束通知等
        }
    }
    // TODO BPMN 中配置监听器
    /**
     * <process id="leaveProcess" name="Leave Process" isExecutable="true">
     *     <!-- 配置流程启动/结束监听器 -->
     *     <extensionElements>
     *         <activiti:executionListener event="start" delegateExpression="${processLifeCycleListener}"/>
     *         <activiti:executionListener event="end" delegateExpression="${processLifeCycleListener}"/>
     *     </extensionElements>
     *
     *     <!-- 其他节点省略 -->
     * </process>
     */
}
