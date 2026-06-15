package com.siasun.iscs.ep.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siasun.iscs.ep.dto.EmergencyTaskDTO;
import com.siasun.iscs.ep.entity.EmergencyTask;

import java.util.List;

/**
 * @description: 任务配置服务接口
 * @author liao
 * @date 2026/6/4
 * @version 1.0
 */
public interface EmergencyTaskService extends IService<EmergencyTask> {

    // 查询全部任务（按 task_code 升序）
    List<EmergencyTask> queryAllTasks();

    // 新增/编辑
    boolean saveTask(EmergencyTaskDTO taskDTO);

    // 删除
    boolean deleteTask(String id);

    // 根据任务类型查询
    List<EmergencyTask> queryTaskByType(String type);

}