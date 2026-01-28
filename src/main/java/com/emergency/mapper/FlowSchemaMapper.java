package com.emergency.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author liao
 * @version 1.0
 * @description: TODO
 * @date 2026/1/26
 */
@Mapper
public interface FlowSchemaMapper {

    /**
     * 查询所有运行中的流程实例
     */
    List<Map<String, Object>> selectProcInst();

    /**
     * 查询某个流程实例下的所有执行实例
     *
     * @param procInstId 流程实例ID
     */
    List<Map<String, Object>> selectExecInst(@Param("procInstId") String procInstId);

    /**
     * 查询流程实例的历史基本信息
     *
     * @param procInstId 流程实例ID
     */
    Map<String, Object> selectProcInstHistory(@Param("procInstId") String procInstId);

    /**
     * 查询流程实例的所有节点执行历史
     *
     * @param procInstId 流程实例ID
     */
    List<Map<String, Object>> selectActivityHistory(@Param("procInstId") String procInstId);

    /**
     * 查询指定用户的任务（支持筛选流程、分页）
     *
     * @param userId               用户ID
     * @param finished             true=已完成，false=未完成，null=全部
     * @param processDefinitionKey 流程定义key（可选）
     * @param limit                分页大小
     * @param offset               分页偏移量
     */
    List<Map<String, Object>> selectUserTask(
            @Param("userId") String userId,
            @Param("finished") Boolean finished,
            @Param("processDefinitionKey") String processDefinitionKey,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset);

    /**
     * 按流程实例ID查询所有任务历史
     *
     * @param procInstId 流程实例ID
     */
    List<Map<String, Object>> selectTaskByProcInstId(@Param("procInstId") String procInstId);

    /**
     * 按时间范围查询任务
     *
     * @param startTime 开始时间（Timestamp）
     * @param endTime   结束时间（Timestamp）
     * @param assignee  处理人（可选）
     * @param limit     分页大小
     * @param offset    分页偏移量
     */
    List<Map<String, Object>> selectTaskByTimeRange(
            @Param("startTime") java.sql.Timestamp startTime,
            @Param("endTime") java.sql.Timestamp endTime,
            @Param("assignee") String assignee,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset);
}