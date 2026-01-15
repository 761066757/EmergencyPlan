package com.emergency.controller;

import com.emergency.dto.StepDTO;
import com.emergency.entity.EmergencyStep;
import com.emergency.service.EmergencyStepService;
import com.emergency.service.SysDictService;
import com.emergency.vo.DictOptionVO;
import com.emergency.vo.EmergencyStepVO;
import com.emergency.vo.Result;
import com.emergency.vo.StepNameVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 步骤配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/step")
public class EmergencyStepController {

    @Resource
    private EmergencyStepService stepService;

    @Resource
    private SysDictService dictService;

    /**
     * 步骤列表查询
     */
    @GetMapping("/listAll")
    public Result<?> getStepList() {
        return Result.success(stepService.list());
    }

    // 1. 按模块查询步骤列表（含类型名称）
    @GetMapping("/list")
    public Result<List<EmergencyStepVO>> getStepList(@RequestParam(value = "moduleCode") String moduleCode) {
        try {
            List<EmergencyStepVO> stepList = stepService.listStepVOByModule(moduleCode);
            return Result.success(stepList);
        } catch (Exception e) {
            log.error("查询步骤列表失败", e);
            return Result.error("查询步骤列表失败：" + e.getMessage());
        }
    }

    // 2. 保存步骤（新增/编辑）
    @PostMapping("/save")
    public Result<Boolean> saveStep(@RequestBody EmergencyStep step) {
        try {
            boolean success = stepService.saveStep(step);
            return success ? Result.success(true) : Result.error("保存失败");
        } catch (Exception e) {
            log.error("保存步骤失败", e);
            return Result.error("保存步骤失败：" + e.getMessage());
        }
    }

    // 3. 删除步骤
    @PostMapping("/delete/{id}")
    public Result<Boolean> deleteStep(@PathVariable(value = "id") String id) {
        try {
            boolean success = stepService.deleteStep(id);
            return success ? Result.success(true) : Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除步骤失败", e);
            return Result.error("删除步骤失败：" + e.getMessage());
        }
    }

    // 4. 批量查询步骤名称（供Flowable调用）
    @PostMapping("/getStepNameByIds")
    public Result<List<StepNameVO>> getStepNameByIds(@RequestBody List<String> stepIds) {
        try {
            List<StepNameVO> stepNames = stepService.getStepNameByIds(stepIds);
            return Result.success(stepNames);
        } catch (Exception e) {
            log.error("批量查询步骤名称失败", e);
            return Result.error("批量查询步骤名称失败：" + e.getMessage());
        }
    }

    // 5. 查询步骤类型字典（下拉用）
    @GetMapping("/getStepTypeDict")
    public Result<List<DictOptionVO>> getStepTypeDict(@RequestParam(value = "moduleCode") String moduleCode) {
        try {
            List<DictOptionVO> dictList = dictService.getStepTypeDict(moduleCode);
            return Result.success(dictList);
        } catch (Exception e) {
            log.error("查询步骤类型字典失败", e);
            return Result.error("查询步骤类型字典失败：" + e.getMessage());
        }
    }

}