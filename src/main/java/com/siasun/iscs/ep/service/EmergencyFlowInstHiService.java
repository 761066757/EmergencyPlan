package com.siasun.iscs.ep.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siasun.iscs.ep.dto.EmergencyFlowInstHiDTO;
import com.siasun.iscs.ep.entity.EmergencyFlowInstHi;

import java.util.List;

/**
 * @description: 流程实例历史服务接口
 * @author liao
 * @date 2026/6/11
 * @version 1.0
 */
public interface EmergencyFlowInstHiService extends IService<EmergencyFlowInstHi> {

    /**
     * 查询流程实例历史列表
     */
    List<EmergencyFlowInstHi> queryList(EmergencyFlowInstHiDTO dto);

    /**
     * 异步保存录像回放
     * @param instHiId 流程实例历史ID
     */
    void asyncSaveVideoPlayback(String instHiId);
}
