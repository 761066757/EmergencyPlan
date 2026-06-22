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
     * <p>业务流程：
     * 1. DTO转实体，补充默认字段（车站、状态）
     * 2. 保存/更新预案主表（saveOrUpdate，有ID则更新，无ID则新增）
     * 3. 将关联任务DTO列表转为实体列表（空值防御，避免NPE）
     * 4. 批量保存预案-任务关联关系（先删后加，保证幂等）
     *
     * @param planDTO            预案信息
     * @param planTaskRelDTOList 预案关联任务信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean savePlan(EmergencyPlanDTO planDTO, List<EmergencyPlanTaskRelDTO> planTaskRelDTOList) {
        // 1. DTO转实体，补充默认字段
        EmergencyPlan plan = new EmergencyPlan();
        BeanUtils.copyProperties(planDTO, plan);
        // TODO 临时处理：车站（后续从登录上下文获取）
        plan.setStation("d03");
        // 新建预案默认状态为 0-未发布
        plan.setPlanStatus(0);

        // 2. 保存预案主表（MyBatis-Plus根据id是否存在自动判断insert/update）
        boolean save = this.saveOrUpdate(plan);

        // 3. 关联任务DTO列表转实体列表（空值防御，避免NPE）
        List<EmergencyPlanTaskRel> relations = CollectionUtils.isEmpty(planTaskRelDTOList)
                ? new ArrayList<>()
                : planTaskRelDTOList.stream()
                .map(dto -> {
                    EmergencyPlanTaskRel relation = new EmergencyPlanTaskRel();
                    BeanUtils.copyProperties(dto, relation);
                    return relation;
                })
                .collect(Collectors.toList());

        // 4. 批量保存预案-任务关联关系（先删后加，保证幂等）
        planStepRelService.saveRelations(plan.getId(), plan.getStation(), relations);
        return save;
    }

    /**
     * BPMN XML文件校验
     * <p>校验流程：
     * 1. 文件非空校验
     * 2. 文件名非空校验（防止空指针）
     * 3. 文件后缀校验（仅支持 .xml / .bpmn / .bpmn20.xml）
     * 4. 文件大小校验（上限2MB）
     * 5. 文件内容读取及非空校验（try-with-resources自动关流）
     * 6. 封装返回数据（前端取 data.bpmnXml 即可）
     *
     * @param file BPMN XML文件
     * @return 校验结果（成功返回bpmnXml字符串，失败返回错误信息）
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

    /**
     * 上传预案文档
     * <p>业务流程：
     * 1. 上传文件到第三方存储服务（待对接）
     * 2. 获取文件预览URL并返回前端
     *
     * @param file 预案文档文件
     * @return 文件预览URL
     */
    @Override
    public Result<String> uploadPlanDoc(MultipartFile file) {
        // TODO 调用第三方服务上传文件并获取预览URL
        return Result.success("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf");
    }

    /**
     * 查询预案列表
     * <p>业务流程：
     * 1. 根据查询条件DTO（planName/planType等可选字段）过滤
     * 2. 联表查询预案主表+流程关联表，返回聚合VO
     * 3. XML中手写SQL，支持动态条件过滤
     *
     * @param planDTO 查询条件（字段均为可选）
     * @return 预案列表（含流程状态信息）
     */
    @Override
    public List<EmergencyPlanQueryVO> getPlanList(EmergencyPlanDTO planDTO) {
        return emergencyPlanMapper.selectPlanQueryVOList(planDTO);
    }


    /**
     * 获取预案详情
     * <p>业务流程：
     * 1. 根据预案ID查询主表记录（自动过滤逻辑删除）
     *
     * @param planId 预案主键ID
     * @return 预案实体
     */
    @Override
    public EmergencyPlan getPlanDetail(String planId) {
        return this.getById(planId);
    }


    /**
     * 实时监控
     * <p>业务流程：
     * 1. 根据预案ID查询预案记录，获取关联的摄像头ID列表
     * 2. 校验传入的cameraId是否在预案关联的摄像头列表中
     * 3. 调用第三方gRPC服务获取实时视频预览流地址（待对接）
     * 4. 返回RTSP流地址供前端播放
     *
     * @param planId   预案ID
     * @param cameraId 摄像头ID
     * @return RTSP视频流地址
     */
    @Override
    public String realTimeMonitor(String planId, String cameraId) {
        // 1. 查询预案记录，获取关联摄像头列表
        EmergencyPlan plan = this.getById(planId);
        // 2. 校验cameraId是否在预案关联的摄像头列表中（以;分割）
        String[] cameraIds = plan.getCameraIds().split(";");
        if (!Arrays.asList(cameraIds).contains(cameraId)) {
            return "Invalid camera ID: " + cameraId;
        }

        // TODO 调用gRPC服务获取实时视频预览流地址

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

        // 测试数据（正式环境替换为真实RTSP地址）
        return "rtsp://133.10.2.51:9100/dss/monitor/param/cameraid=1000017%240%26substream=1?token=142";
    }

}
