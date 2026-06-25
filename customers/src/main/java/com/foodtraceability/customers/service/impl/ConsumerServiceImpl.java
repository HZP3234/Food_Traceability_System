package com.foodtraceability.customers.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.foodtraceability.customers.dto.ConsumerUpdateDTO;
import com.foodtraceability.customers.entity.Consumer;
import com.foodtraceability.customers.mapper.ConsumerMapper;
import com.foodtraceability.customers.service.ConsumerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerServiceImpl implements ConsumerService {

    private final ConsumerMapper consumerMapper;

    @Override
    public void sendCode(String phone) {
        log.info("向手机号 {} 发送验证码: 123456", phone);
    }

    @Override
    public Consumer login(String phone, String code) {
        if (!"123456".equals(code)) {
            throw new RuntimeException("验证码错误");
        }
        Consumer consumer = consumerMapper.selectOne(
                new LambdaQueryWrapper<Consumer>().eq(Consumer::getPhone, phone));
        if (consumer == null) {
            consumer = new Consumer();
            consumer.setConsumerUuid(UUID.randomUUID().toString().replace("-", ""));
            consumer.setPhone(phone);
            consumer.setNickName("用户" + phone.substring(phone.length() - 4));
            consumer.setStatus(1);
            consumer.setTotalScans(0);
            consumer.setComplaintCount(0);
            consumer.setCreateBy("system");
            consumer.setUpdateBy("system");
            consumerMapper.insert(consumer);
        }
        return consumer;
    }

    @Override
    public Consumer getByPhone(String phone) {
        return consumerMapper.selectOne(
                new LambdaQueryWrapper<Consumer>().eq(Consumer::getPhone, phone));
    }

    @Override
    public Consumer updateConsumer(ConsumerUpdateDTO dto) {
        Consumer consumer = consumerMapper.selectOne(
                new LambdaQueryWrapper<Consumer>().eq(Consumer::getPhone, dto.getPhone()));
        if (consumer == null) {
            throw new RuntimeException("用户不存在");
        }
        if (dto.getNickName() != null) {
            consumer.setNickName(dto.getNickName());
        }
        if (dto.getRealName() != null) {
            consumer.setRealName(dto.getRealName());
        }
        if (dto.getGender() != null) {
            consumer.setGender(dto.getGender());
        }
        if (dto.getRegion() != null) {
            consumer.setRegion(dto.getRegion());
        }
        consumer.setUpdateBy(consumer.getPhone());
        consumerMapper.updateById(consumer);
        return consumer;
    }
}
