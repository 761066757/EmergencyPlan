package com.siasun.iscs.ep.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siasun.iscs.ep.dto.EmergencyFlowInstHiDTO;
import com.siasun.iscs.ep.entity.EmergencyFlowInstHi;
import com.siasun.iscs.ep.mapper.EmergencyFlowInstHiMapper;
import com.siasun.iscs.ep.service.EmergencyFlowInstHiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author liao
 * @version 1.0
 * @description: 流程实例历史服务实现类
 * @date 2026/6/10
 */
@Slf4j
@Service
public class EmergencyFlowInstHiServiceImpl extends ServiceImpl<EmergencyFlowInstHiMapper, EmergencyFlowInstHi> implements EmergencyFlowInstHiService {

    /**
     * 查询流程实例历史列表
     * <p>业务流程：
     * 1. 只查询已完成的历史记录（is_history=1）且未删除（is_deleted=0）
     * 2. 支持动态条件过滤：
     *    - planName：模糊匹配（LIKE）
     *    - startTime：起始时间（>=）
     *    - endTime：结束时间（<=）
     * 3. 按 start_time 降序排列，最新执行的记录在前
     *
     * @param dto 查询条件（字段均为可选）
     * @return 流程实例历史列表
     */
    @Override
    public List<EmergencyFlowInstHi> queryList(EmergencyFlowInstHiDTO dto) {
        return this.list(new QueryWrapper<EmergencyFlowInstHi>()
                .eq("is_deleted", 0)
                .eq("is_history", 1)
                .like(StringUtils.isNotBlank(dto.getPlanName()), "plan_name", dto.getPlanName())
                .ge(dto.getStartTime() != null, "start_time", dto.getStartTime())
                .le(dto.getEndTime() != null, "end_time", dto.getEndTime())
                .orderByDesc("start_time"));
    }

    /**
     * 异步保存录像回放
     * <p>业务流程：
     * 1. 根据 instHiId 查询流程实例历史记录
     * 2. 获取关联的摄像头ID列表（以;分割）
     * 3. 遍历摄像头，调用第三方服务获取录像回放URL（待对接）
     * 4. 将URL拼接保存到 video_urls 字段
     * <p>触发时机：流程结束或终止时异步调用，不阻塞主流程
     *
     * @param instHiId 流程实例历史记录主键ID
     */
    @Async
    @Override
    public void asyncSaveVideoPlayback(String instHiId) {
        try {
            log.info("开始异步保存录像回放, instHiId={}", instHiId);
            // 1. 查询流程实例历史记录
            EmergencyFlowInstHi instHi = this.getById(instHiId);
            if (instHi == null || StringUtils.isBlank(instHi.getCameraIds())) {
                log.warn("流程实例历史记录不存在或无摄像头, instHiId={}", instHiId);
                return;
            }
            // 2. 获取摄像头ID列表（以;分割）
            String[] cameraIds = instHi.getCameraIds().split(";");
            LocalDateTime startTime = instHi.getStartTime();
            LocalDateTime endTime = instHi.getEndTime();
            // 3. 遍历摄像头，获取录像回放URL
            for (String cameraId : cameraIds) {
                // TODO: 调用第三方服务获取录像回放URL并更新 video_urls 字段
                log.info("异步保存录像回放完成, cameraId={}", cameraId);

                // 测试数据（正式环境替换为真实录像URL）
                String testUrl = "https://www.w3schools.com/html/mov_bbb.mp4";
                instHi.setVideoUrls(instHi.getVideoUrls() + cameraId + ":" + testUrl + ";");
            }
        } catch (Exception e) {
            log.error("异步保存录像回放失败, instHiId={}", instHiId, e);
        }
    }
}
