package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.TraceCode;
import org.apache.ibatis.annotations.Mapper;

/**
 * 溯源码 Mapper — t_trace_code
 * <p>
 * 继承 MyBatis-Plus BaseMapper，获得通用 CRUD 能力。
 * 复杂查询在 Service 层通过 QueryWrapper 构建。
 *
 * @author GuangYao Liu
 * @since 2026-06-23
 */
@Mapper
public interface TraceCodeMapper extends BaseMapper<TraceCode> {
}
