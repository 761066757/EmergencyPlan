package com.siasun.iscs.ep.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siasun.iscs.ep.dto.EmergencyPlanDTO;
import com.siasun.iscs.ep.dto.EmergencyPlanTaskRelDTO;
import com.siasun.iscs.ep.entity.EmergencyPlan;
import com.siasun.iscs.ep.vo.EmergencyPlanQueryVO;
import com.siasun.iscs.ep.vo.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @description: 应急预案服务接口
 * @author liao
 * @date 2026/6/3
 * @version 1.0
 */
public interface EmergencyPlanService extends IService<EmergencyPlan> {

    /**
     * 保存预案
     */
    boolean savePlan(EmergencyPlanDTO planDTO, List<EmergencyPlanTaskRelDTO> planTaskRelDTOList);

    /**
     * bpmn文件校验
     */
    Result<Map<String, String>> bpmnValidate(MultipartFile file);

    /**
     * 上传预案文档
     */
    Result<String> uploadPlanDoc(MultipartFile file);

    /**
     * 查询应急预案列表
     */
    List<EmergencyPlanQueryVO> getPlanList(EmergencyPlanDTO planDTO);

    /**
     * 查询预案详情
     */
    EmergencyPlan getPlanDetail(String planId);

    /**
     * 查看预案文档
     */
    String viewPlanDoc(String planId);

    /**
     * 实时监控
     */
    String realTimeMonitor(String planId);

}
