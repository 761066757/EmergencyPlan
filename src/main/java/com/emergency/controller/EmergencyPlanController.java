/*
 * @Author: liao liao@siasun.com
 * @Date: 2026-01-13 14:02:38
 * @LastEditors: liao liao@siasun.com
 * @LastEditTime: 2026-02-06 11:25:27
 * @FilePath: \EmergencyPlan\src\main\java\com\emergency\controller\EmergencyPlanController.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.emergency.controller;

import com.emergency.entity.EmergencyPlan;
import com.emergency.entity.EmergencyPlanStepRelation;
import com.emergency.service.EmergencyPlanService;
import com.emergency.service.EmergencyPlanStepRelationService;
import com.emergency.vo.EmergencyStepVO;
import com.emergency.vo.PlanQueryVO;
import com.emergency.vo.PlanSaveVO;
import com.emergency.vo.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 预案Controller
@Slf4j
@RestController
@RequestMapping("plan")
public class EmergencyPlanController {
    @Resource
    private EmergencyPlanService planService;
    @Resource
    private EmergencyPlanStepRelationService planStepRelationService;

    // 查询预案列表
    @GetMapping("/list")
    public Result<List<PlanQueryVO>> getPlanList() {
        try {
            List<PlanQueryVO> planList = planService.getPlanList();
            return Result.success(planList);
        } catch (Exception e) {
            log.error("查询预案列表失败", e);
            return Result.error("查询预案列表失败：" + e.getMessage());
        }
    }

    // 保存预案
    @PostMapping("/save")
    public Result<Boolean> savePlan(@RequestBody PlanSaveVO saveVO) {
        boolean success = planService.savePlan(saveVO.getPlan(), saveVO.getPlanStepRelations());
        return success ? Result.success(true) : Result.error("保存失败");
    }

    // 查询预案详情
    @GetMapping("/getById/{planId}")
    public Result<EmergencyPlan> getById(@PathVariable(value = "planId") String planId) {
        return Result.success(planService.getById(planId));
    }

    // 查询预案关联步骤
    @GetMapping("/getRelations{id}")
    public Result<List<EmergencyPlanStepRelation>> getRelations(@PathVariable(value = "id") String planId) {
        return Result.success(planStepRelationService.getByPlanId(planId));
    }

    @PostMapping("/importBpmn")
    public Result<Map<String, String>> importBpmn(@RequestParam("file") MultipartFile file) {
        // 1. 基础校验：文件为空
        if (file.isEmpty()) {
            return Result.error("上传失败：请选择有效的BPMN文件");
        }

        String originalFilename = file.getOriginalFilename();
        // 2. 文件名非空校验，防止空指针
        if (originalFilename == null || originalFilename.trim().isBlank()) {
            return Result.error("上传失败：文件名称无效，无法识别");
        }

        // 3. 校验文件后缀：仅支持.xml,.bpmn,.bpmn20.xml，忽略大小写兼容.BPMN
        int suffixIndex = originalFilename.lastIndexOf(".");
        if (suffixIndex == -1 || suffixIndex == originalFilename.length() - 1) {
            return Result.error("上传失败：文件无有效后缀，仅支持BPMN格式");
        }
        String fileSuffix = originalFilename.substring(suffixIndex).toLowerCase();
        // 定义允许的文件后缀集合
        if (!(".xml".equals(fileSuffix) || ".bpmn".equals(fileSuffix) || ".bpmn20.xml".equals(fileSuffix))) {
            return Result.error("上传失败：仅支持.xml,.bpmn,.bpmn20.xml格式文件，当前为" + fileSuffix);
        }

        // 4. 文件大小限制，可根据业务调整（字节）
        long maxSize = 2000 * 1024L;
        if (file.getSize() > maxSize) {
            return Result.error("上传失败：文件大小超过限制，最大支持2MB");
        }

        // 5. 核心：安全读取文件流，解析为BPMN XML字符串（try-with-resources自动关流，无资源泄漏）
        String bpmnXml;
        try (InputStream is = file.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr)) {

            StringBuilder xmlSb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                xmlSb.append(line);
            }
            bpmnXml = xmlSb.toString();

            // 校验XML内容非空，防止空文件/空白文件
            if (bpmnXml.trim().isBlank()) {
                return Result.error("上传失败：BPMN文件内容为空，是无效文件");
            }

        } catch (Exception e) {
            // 捕获文件读取所有异常（IO/编码/损坏等），记录日志+返回友好提示
            log.error("BPMN文件读取失败，文件名：{}，异常信息：", originalFilename, e);
            return Result.error("上传失败：文件读取异常，请检查文件是否损坏");
        }

        // 6. 封装返回数据：前端直接取 data.bpmnXml 即可
        Map<String, String> response = new HashMap<>();
        response.put("bpmnXml", bpmnXml);
        return Result.success(response);
    }
}
