package com.foodtraceability.customers.service;

import com.foodtraceability.customers.dto.ConsumerUpdateDTO;
import com.foodtraceability.customers.entity.Consumer;

public interface ConsumerService {

    void sendCode(String phone);

    Consumer login(String phone, String code);

    Consumer getByPhone(String phone);

    Consumer updateConsumer(ConsumerUpdateDTO dto);
}
