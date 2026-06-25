package com.foodtraceability.regulation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.regulation.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    SysUser selectByUsername(@Param("username") String username);

    List<SysUser> selectByRoleType(@Param("roleType") String roleType);

    List<SysUser> selectByEnterpriseUuid(@Param("uuid") String uuid);

    List<SysUser> selectByStatus(@Param("status") Integer status);

}