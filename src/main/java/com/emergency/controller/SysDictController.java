package com.emergency.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.emergency.entity.SysDict;
import com.emergency.service.SysDictService;
import com.emergency.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 步骤类型接口层
 */
@RestController
@RequestMapping("/api/dict")
public class SysDictController {

    @Autowired
    private SysDictService dictService;

    /**
     * 分页查询所有字典
     */
    @GetMapping("/page")
    public Result<Page<SysDict>> pageQuery(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String moduleCode,
            @RequestParam(required = false) String dictType,
            @RequestParam(required = false) String dictLabel) {
        Page<SysDict> page = new Page<>(pageNum, pageSize);
        // 可通过QueryWrapper添加多条件查询，示例：
        /*
        QueryWrapper<SysDict> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(moduleCode)) {
            wrapper.eq("module_code", moduleCode);
        }
        if (StringUtils.isNotBlank(dictType)) {
            wrapper.eq("dict_type", dictType);
        }
        if (StringUtils.isNotBlank(dictLabel)) {
            wrapper.like("dict_label", dictLabel);
        }
        wrapper.eq("is_deleted", 0);
        return dictService.page(page, wrapper);
        */
        return Result.success(dictService.page(page)) ; // 简化版，实际需加条件
    }

    /**
     * 按模块+类型查询下拉列表
     */
    @GetMapping("/options/module/{moduleCode}/type/{dictType}")
    public Result<List<Map<String, String>>> getDictOptionsByModuleAndType(
            // 显式指定参数名
            @PathVariable(value = "moduleCode") String moduleCode,
            @PathVariable(value = "dictType") String dictType) {
        return Result.success(dictService.getDictOptionsByModuleAndType(moduleCode, dictType));
    }

    /**
     * 按模块查询所有步骤类型
     */
    @GetMapping("/step-type/{moduleCode}")
    public Result<List<SysDict>> getStepTypeByModule(@PathVariable String moduleCode) {
        return Result.success(dictService.getStepTypeByModule(moduleCode));
    }

    /**
     * 新增步骤类型
     */
    @PostMapping
    public Result<Boolean> addDict(@RequestBody SysDict dict) {
        // 固定步骤类型的dictType和dictTypeName
        dict.setDictType("step_type");
        dict.setDictTypeName("步骤类型");
        return Result.success(dictService.save(dict));
    }

    /**
     * 编辑步骤类型
     */
    @PutMapping
    public Result<?> updateDict(@RequestBody SysDict dict) {
        boolean success = dictService.updateById(dict);
        return success ? Result.success() : Result.error("保存失败");
    }

    /**
     * 删除步骤类型（逻辑删除）
     */
    // 如果不想逐个改注解，可以配置编译器保留参数名，适配 Maven 的配置（启用 -parameters 编译参数）
    @DeleteMapping("/{id}")
    public Result<?> deleteDict(@PathVariable(value = "id") String id) {
        boolean success =  dictService.removeById(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}