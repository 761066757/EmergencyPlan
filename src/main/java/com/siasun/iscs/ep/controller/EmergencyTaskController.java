package com.siasun.iscs.ep.controller;

import com.siasun.iscs.ep.dto.EmergencyTaskDTO;
import com.siasun.iscs.ep.service.EmergencyTaskService;
import com.siasun.iscs.ep.vo.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author liao
 * @version 1.0
 * @description: 任务配置控制器
 * @date 2026/5/6
 */

@Slf4j
@RestController
@RequestMapping("/task")
public class EmergencyTaskController {

    @Resource
    private EmergencyTaskService taskService;

    /**
     * 查询全部任务（按 task_code 升序）
     */
    @GetMapping("/query")
    public Result<?> queryTask() {
        return Result.success(taskService.queryAllTasks());
    }

    /**
     * 保存任务（新增/编辑）
     */
    @PostMapping("/save")
    public Result<Boolean> saveTask(@RequestBody EmergencyTaskDTO taskDTO) {
        try {
            boolean success = taskService.saveTask(taskDTO);
            return success ? Result.success(true) : Result.error("保存失败");
        } catch (Exception e) {
            log.error("保存任务失败", e);
            return Result.error("保存任务失败：" + e.getMessage());
        }
    }

    /**
     * 删除任务
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteTask(@RequestParam(value = "id") String id) {
        try {
            boolean success = taskService.deleteTask(id);
            return success ? Result.success(true) : Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除任务失败", e);
            return Result.error("删除任务失败：" + e.getMessage());
        }
    }

    /**
     * 根据任务类型（父任务）查询任务名称（子任务）
     */
    @GetMapping("/queryByType")
    public Result<?> queryTaskByType(@RequestParam(value = "type") String type) {
        return Result.success(taskService.queryTaskByType(type));
    }
}