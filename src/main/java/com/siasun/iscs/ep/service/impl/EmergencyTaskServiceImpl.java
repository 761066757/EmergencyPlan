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
     * 查询全部任务
     * <p>业务流程：
     * 1. 构建QueryWrapper，按 task_type 升序 + task_code 升序排序
     * 2. 返回全部未删除的任务列表（逻辑删除由@TableLogic自动过滤）
     *
     * @return 任务列表（已排序）
     */
    @Override
    public List<EmergencyTask> queryAllTasks() {
        QueryWrapper<EmergencyTask> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("task_type", "task_code");
        return this.list(wrapper);
    }

    /**
     * 新增/编辑任务
     * <p>业务流程：
     * 1. DTO转实体
     * 2. 判断是否为新增（id为空）：
     *    - 新增：补充默认字段（车站、创建时间、更新时间、逻辑删除标记）
     *    - 编辑：仅更新 updateTime
     * 3. 执行save/update操作
     *
     * @param taskDTO 任务DTO
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveTask(EmergencyTaskDTO taskDTO) {
        // 1. DTO转实体
        EmergencyTask task = new EmergencyTask();
        BeanUtils.copyProperties(taskDTO, task);
        // 2. 判断新增/编辑
        if (StringUtils.isEmpty(task.getId())) {
            // 新增：补充默认字段
            // TODO 临时处理：车站（后续从登录上下文获取）
            task.setStation("d03");
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            task.setIsDeleted(0);
            return this.save(task);
        } else {
            // 编辑：仅更新时间
            task.setUpdateTime(LocalDateTime.now());
            return this.updateById(task);
        }
    }

    /**
     * 删除任务（逻辑删除）
     * <p>业务流程：
     * 1. 调用 deleteById，@TableLogic注解自动将 UPDATE is_deleted=1，而非物理删除
     * 2. 根据受影响行数判断操作是否成功
     *
     * @param id 任务主键ID
     * @return 删除结果
     */
    @Override
    public boolean deleteTask(String id) {
        int i = baseMapper.deleteById(id);
        return i > 0;
    }

    /**
     * 根据任务类型查询任务名称
     * <p>业务流程：
     * 1. 根据 task_type 精确匹配过滤
     * 2. 按 task_code 升序排序，返回子任务列表
     * 3. 主要用于前端“任务类型”下拉联动“任务名称”下拉
     *
     * @param type 任务类型（父任务）
     * @return 该类型下的子任务列表
     */
    @Override
    public List<EmergencyTask> queryTaskByType(String type) {
        QueryWrapper<EmergencyTask> wrapper = new QueryWrapper<>();
        wrapper.eq("task_type", type);
        wrapper.orderByAsc("task_code");
        return this.list(wrapper);
    }
}