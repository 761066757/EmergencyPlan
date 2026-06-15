package com.siasun.iscs.ep.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siasun.iscs.ep.dto.EmergencyTaskDTO;
import com.siasun.iscs.ep.entity.EmergencyTask;
import com.siasun.iscs.ep.mapper.EmergencyTaskMapper;
import com.siasun.iscs.ep.service.EmergencyTaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @description: 任务配置服务实现类
 * @author liao
 * @date 2026/6/4
 * @version 1.0
 */
@Service
public class EmergencyTaskServiceImpl extends ServiceImpl<EmergencyTaskMapper, EmergencyTask> implements EmergencyTaskService {

    @Resource
    private EmergencyTaskMapper taskMapper;

    /**
     * 查询全部任务（先按 type_code 升序，再按 task_code 升序）
     */
    @Override
    public List<EmergencyTask> queryAllTasks() {
        QueryWrapper<EmergencyTask> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("task_type", "task_code");
        return this.list(wrapper);
    }

    /**
     * 新增/编辑任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveTask(EmergencyTaskDTO taskDTO) {
        EmergencyTask task = new EmergencyTask();
        BeanUtils.copyProperties(taskDTO, task);
        if (StringUtils.isEmpty(task.getId())) {
            // TODO 临时处理：车站
            task.setStation("d03");
            // 新增：自动填充创建时间
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            task.setIsDeleted(0);
            return this.save(task);
        } else {
            // 编辑：更新时间
            task.setUpdateTime(LocalDateTime.now());
            return this.updateById(task);
        }
    }

    /**
     * 删除任务（逻辑删除）
     */
    @Override
    public boolean deleteTask(String id) {
        int i = baseMapper.deleteById(id);
        return i > 0;
    }

    /**
     * 根据任务类型查询任务名称
     */
    @Override
    public List<EmergencyTask> queryTaskByType(String type) {
        QueryWrapper<EmergencyTask> wrapper = new QueryWrapper<>();
        wrapper.eq("task_type", type);
        wrapper.orderByAsc("task_code");
        return this.list(wrapper);
    }
}