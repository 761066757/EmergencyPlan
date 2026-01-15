package com.emergency.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emergency.entity.SysDict;
import com.emergency.mapper.SysDictMapper;
import com.emergency.service.SysDictService;
import com.emergency.vo.DictOptionVO;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * 步骤类型业务层实现
 */
@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {

    @Override
    public List<Map<String, String>> getDictOptionsByModuleAndType(String moduleCode, String dictType) {
        return baseMapper.selectDictOptionsByModuleAndType(moduleCode, dictType);
    }

    @Override
    public List<SysDict> getStepTypeByModule(String moduleCode) {
        return baseMapper.selectStepTypeByModule(moduleCode);
    }

    @Override
    // 查询步骤类型字典（下拉用）
    public List<DictOptionVO> getStepTypeDict(String moduleCode) {
        return baseMapper.selectDictOptionByModuleAndType(moduleCode, "step_type");
    }
}