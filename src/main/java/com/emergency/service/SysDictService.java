package com.emergency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.emergency.entity.SysDict;
import com.emergency.vo.DictOptionVO;

import java.util.List;
import java.util.Map;

/**
 * 步骤类型业务层接口
 */
public interface SysDictService extends IService<SysDict> {
    /**
     * 按模块+类型查询下拉列表
     */
    List<Map<String, String>> getDictOptionsByModuleAndType(String moduleCode, String dictType);

    /**
     * 按模块查询所有步骤类型
     */
    List<SysDict> getStepTypeByModule(String moduleCode);

    List<DictOptionVO> getStepTypeDict(String moduleCode);
}