package com.siasun.iscs.ep.controller;

import com.siasun.iscs.ep.dto.EmergencyFlowInstHiDTO;
import com.siasun.iscs.ep.entity.EmergencyFlowInstHi;
import com.siasun.iscs.ep.service.EmergencyFlowInstHiService;
import com.siasun.iscs.ep.vo.Result;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description: 流程实例历史控制器
 * @author liao
 * @date 2026/6/11
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/instHi")
public class EmergencyFlowInstHiController {

    @Resource
    private EmergencyFlowInstHiService emergencyFlowInstHiService;

    /**
     * 查询流程实例历史列表
     */
    @PostMapping("/queryHistory")
    public Result<List<EmergencyFlowInstHi>> queryList(@Valid @RequestBody EmergencyFlowInstHiDTO dto) {
        List<EmergencyFlowInstHi> list = emergencyFlowInstHiService.queryList(dto);
        return Result.success(list);
    }
}
