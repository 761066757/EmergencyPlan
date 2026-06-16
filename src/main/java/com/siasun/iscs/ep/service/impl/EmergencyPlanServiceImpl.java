package com.siasun.iscs.ep.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siasun.iscs.ep.entity.EmergencyPlan;
import com.siasun.iscs.ep.entity.EmergencyPlanTaskRel;
import com.siasun.iscs.ep.mapper.EmergencyPlanMapper;
import com.siasun.iscs.ep.service.EmergencyPlanService;
import com.siasun.iscs.ep.service.EmergencyPlanTaskRelService;
import com.siasun.iscs.ep.dto.EmergencyPlanDTO;
import com.siasun.iscs.ep.dto.EmergencyPlanTaskRelDTO;
import com.siasun.iscs.ep.vo.EmergencyPlanQueryVO;
import com.siasun.iscs.ep.vo.Result;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liao
 * @version 1.0
 * @description: 预案服务实现类
 * @date 2026/6/3
 */
@Service
public class EmergencyPlanServiceImpl extends ServiceImpl<EmergencyPlanMapper, EmergencyPlan> implements EmergencyPlanService {
    @Resource
    private EmergencyPlanTaskRelService planStepRelService;

    @Resource
    private EmergencyPlanMapper emergencyPlanMapper;

    /**
     * 保存预案（含关联任务）
     *
     * @param planDTO            预案信息
     * @param planTaskRelDTOList 预案关联任务信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean savePlan(EmergencyPlanDTO planDTO, List<EmergencyPlanTaskRelDTO> planTaskRelDTOList) {
        // 1、处理摄像头ID列表
        EmergencyPlan plan = new EmergencyPlan();
        BeanUtils.copyProperties(planDTO, plan);
        // TODO 临时处理：车站
        plan.setStation("d03");
        // 默认设置为 0-未发布
        plan.setPlanStatus(0);

        // 2、保存预案基础信息
        boolean save = this.saveOrUpdate(plan);

        // 核心优化：Stream + 空值判断 + 流式转换
        List<EmergencyPlanTaskRel> relations = CollectionUtils.isEmpty(planTaskRelDTOList)
                // 空列表返回空，避免NPE
                ? new ArrayList<>()
                : planTaskRelDTOList.stream()
                .map(dto -> {
                    EmergencyPlanTaskRel relation = new EmergencyPlanTaskRel();
                    BeanUtils.copyProperties(dto, relation);
                    return relation;
                })
                .collect(Collectors.toList());

        // 3、保存预案关联任务
        planStepRelService.saveRelations(plan.getId(), plan.getStation(), relations);
        return save;
    }

    /**
     * BPMN XML文件校验
     *
     * @param file BPMN XML文件
     * @return 校验结果
     */
    @Override
    public Result<Map<String, String>> bpmnValidate(MultipartFile file) {
        // 1. 基础校验：文件为空
        if (file.isEmpty()) {
            return Result.error("上传失败：请选择有效的BPMN文件");
        }

        String originalFilename = file.getOriginalFilename();
        // 2. 文件名非空校验，防止空指针
        if (originalFilename.trim().isBlank()) {
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
            log.error("BPMN文件读取失败，文件名：{}，异常信息：");
            return Result.error("上传失败：文件读取异常，请检查文件是否损坏");
        }

        // 6. 封装返回数据：前端直接取 data.bpmnXml 即可
        Map<String, String> response = new HashMap<>();
        response.put("bpmnXml", bpmnXml);
        return Result.success(response);
    }

    @Override
    public Result<String> uploadPlanDoc(MultipartFile file) {
        // TODO 调用第三方服务获取文件预览url-待完善
        return Result.success("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf");
    }

    /**
     * 查询预案列表
     */
    @Override
    public List<EmergencyPlanQueryVO> getPlanList(EmergencyPlanDTO planDTO) {
        return emergencyPlanMapper.selectPlanQueryVOList(planDTO);
    }


    /**
     * 获取预案详情
     */
    @Override
    public EmergencyPlan getPlanDetail(String planId) {
        return this.getById(planId);
    }


    /**
     * 实时监控
     */
    @Override
    public String realTimeMonitor(String planId, String cameraId) {
        EmergencyPlan plan = this.getById(planId);
        String[] cameraIds = plan.getCameraIds().split(";");
        if (!Arrays.asList(cameraIds).contains(cameraId)) {
            return "Invalid camera ID: " + cameraId;
        }

        // TODO 调用第三方服务获取实时视频预览流地址url-待完善

//        // 调用gRPC获取单个摄像头的流地址
//        ScsServiceProto.GetRealTimePreviewReq req = ScsServiceProto.GetRealTimePreviewReq.newBuilder()
//                .setDeviceId(cameraId)
//                .build();
//        ScsServiceProto.GetRealTimePreviewResp resp = scsGrpcClient.getRealTimePreview(req);

//        // 过滤无效响应或空URL
//        if (resp.getCode() == ScsServiceProto.Result.OK &&
//                StringUtils.isNotEmpty(resp.getUrl()) &&
//                !resp.getUrl().trim().isEmpty()) {
//            urlList.add(AlarmServiceProto.RealTimePreviewUrl.newBuilder()
//                    .setUrl(resp.getUrl())
//                    .build());
//        }

        // 测试数据
        return "rtsp://133.10.2.51:9100/dss/monitor/param/cameraid=1000017%240%26substream=1?token=142";
    }

}
