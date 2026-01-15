package com.emergency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emergency.entity.SysDict;
import com.emergency.vo.DictOptionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 步骤类型数据访问层
 */
@Mapper
public interface SysDictMapper extends BaseMapper<SysDict> {
    /**
     * 按模块+类型查询下拉列表（前端选择器用）
     */
    List<Map<String, String>> selectDictOptionsByModuleAndType(@Param("moduleCode") String moduleCode, @Param("dictType") String dictType);

    /**
     * 按模块查询所有步骤类型（维护页面专用）
     */
    List<SysDict> selectStepTypeByModule(@Param("moduleCode") String moduleCode);


    // 按模块+类型查询字典（步骤类型专用）
    @Select("""
        SELECT dict_code as value, dict_label as label 
        FROM public.sys_dict 
        WHERE module_code = #{moduleCode} 
          AND dict_type = #{dictType} 
          AND status = 1 
          AND is_deleted = 0
        ORDER BY sort_num ASC
    """)
    List<DictOptionVO> selectDictOptionByModuleAndType(
            @Param("moduleCode") String moduleCode,
            @Param("dictType") String dictType);

}