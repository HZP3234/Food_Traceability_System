package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.TraceCodeBind;
import org.apache.ibatis.annotations.Mapper;

/**
 * 溯源码绑定关系 Mapper — t_trace_code_bind
 * <p>
 * 继承 MyBatis-Plus BaseMapper，获得通用 CRUD 能力。
 *
 * @author GuangYao Liu
 * @since 2026-06-23
 */
@Mapper
public interface TraceCodeBindMapper extends BaseMapper<TraceCodeBind> {
}
