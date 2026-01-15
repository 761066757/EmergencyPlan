package com.emergency.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统二级代码（步骤类型）实体类
 */
@Data
@TableName("sys_dict")
public class SysDict {
    /** 主键ID（UUID） */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 业务模块编码（emergency：应急流程，inspect：巡检流程） */
    private String moduleCode;

    /** 字典类型（步骤类型固定为step_type） */
    private String dictType;

    /** 字典类型名称（步骤类型固定为：步骤类型） */
    private String dictTypeName;

    /** 步骤类型编码（同一模块下唯一） */
    private String dictCode;

    /** 步骤类型名称（前端展示） */
    private String dictLabel;

    /** 扩展值（JSON格式） */
    private String dictValue;

    /** 排序号 */
    private Integer sortNum;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间（自动填充+触发器刷新） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记：0-未删，1-已删 */
    @TableLogic
    private Integer isDeleted;
}