package com.emergency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emergency.entity.EmergencyStep;
import com.emergency.vo.EmergencyStepVO;
import com.emergency.vo.StepNameVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 步骤配置Mapper接口
 */
@Mapper
public interface EmergencyStepMapper extends BaseMapper<EmergencyStep> {
    // 批量查询步骤名称（供Flowable调用）
    @Select("SELECT id, step_name as stepName FROM public.emergency_step WHERE id IN #{stepIds} AND is_deleted = 0")
    List<StepNameVO> getStepNameByIds(@Param("stepIds") List<String> stepIds);

    // 核心：按模块查询步骤，并关联字典表获取步骤类型名称
    @Select("""
        SELECT s.*, d.dict_label as step_type_name 
        FROM public.emergency_step s
        LEFT JOIN public.sys_dict d ON s.step_type = d.dict_code 
                             AND d.module_code = #{moduleCode} 
                             AND d.dict_type = 'step_type' 
                             AND d.is_deleted = 0
        WHERE s.is_deleted = 0 
        AND (d.module_code = #{moduleCode} OR d.module_code IS NULL)
        ORDER BY s.create_time DESC
    """)
    List<EmergencyStepVO> selectStepVOByModule(@Param("moduleCode") String moduleCode);

}



