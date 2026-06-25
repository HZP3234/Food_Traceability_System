package com.foodtraceability.customers.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.customers.entity.Consumer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConsumerMapper extends BaseMapper<Consumer> {

    Consumer selectByPhone(@Param("phone") String phone);

    Consumer selectByConsumerUuid(@Param("consumerUuid") String consumerUuid);

    List<Consumer> selectByNickName(@Param("nickName") String nickName);

    List<Consumer> selectByRegion(@Param("region") String region);
}
