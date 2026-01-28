package com.emergency.util;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author liao
 * @version 1.0
 * @description: 自定义服务任务（自动执行业务逻辑）-请假审批通过后，自动更新订单状态
 * @date 2026/1/26
 */
@Component
public class UpdateLeaveStatusService implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(UpdateLeaveStatusService.class);

    @Override
    public void execute(DelegateExecution execution) {
        // 获取流程变量
        String applicant = (String) execution.getVariable("applicant");
        Integer leaveDays = (Integer) execution.getVariable("leaveDays");
        String procInstId = execution.getProcessInstanceId();

        // 执行业务逻辑：更新请假单状态为“已批准”
        logger.info("自动更新请假单状态：申请人[{}]，请假天数[{}]，流程实例[{}]", applicant, leaveDays, procInstId);

        // TODO 此处可调用业务Service，如 leaveService.updateStatus(applicant, "APPROVED");

        // TODO BPMN 中配置服务任务
        /**
         * <process id="leaveProcess" name="Leave Process" isExecutable="true">
         *     <!-- 其他节点省略 -->
         *     <!-- 服务任务：自动更新请假状态 -->
         *     <serviceTask id="updateStatusTask" name="更新请假状态" activiti:delegateExpression="${updateLeaveStatusService}"/>
         *
         *     <!-- 连线：审批完成后执行服务任务，再结束 -->
         *     <sequenceFlow id="deptManagerToUpdate" sourceRef="deptManagerTask" targetRef="updateStatusTask"/>
         *     <sequenceFlow id="gmToUpdate" sourceRef="gmTask" targetRef="updateStatusTask"/>
         *     <sequenceFlow id="updateToEnd" sourceRef="updateStatusTask" targetRef="endEvent"/>
         * </process>
         */
    }


    // FIXME need to implement
    // question1：前端通过 bpmn.js 设计的流程仅能定义基础流程结构（节点、网关、连线），无法直接配置 Flowable 专属的监听器、自定义服务任务等扩展属性。
    // answer1：前端只需传递 “纯 BPMN 2.0 标准 XML”（bpmn.js 原生输出），后端在部署流程前，通过解析 XML DOM 结构，自动为指定节点添加监听器、服务任务的 JavaDelegate 等 Flowable 专属配置，核心流程：
    /**
     * 1、前端bpmn.js设计流程 -> 2、传递纯BPMN XML给后端 -> 3、后端解析XML DOM -> 4、按规则为节点注入拓展配置 -> 5、部署修改后的XML到Flowable -> 6、流程正常执行监听器/服务任务
     */

    // question2：如何绑定节点为服务任务呢？
}