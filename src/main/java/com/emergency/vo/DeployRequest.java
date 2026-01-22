package com.emergency.vo;

import lombok.Data;

// 新增请求体接收类（和Controller同包）
@Data
public class DeployRequest {
    private String deployUser;
    private String moduleCode;
    private String planId;
}