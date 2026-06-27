package com.foodtraceability.customers.controller;

import com.foodtraceability.customers.dto.ConsumerUpdateDTO;
import com.foodtraceability.customers.dto.LoginDTO;
import com.foodtraceability.customers.dto.Result;
import com.foodtraceability.customers.dto.SendCodeDTO;
import com.foodtraceability.customers.entity.Consumer;
import com.foodtraceability.customers.service.ConsumerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consumer")
@RequiredArgsConstructor
@CrossOrigin
public class ConsumerController {

    private final ConsumerService consumerService;

    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeDTO dto) {
        consumerService.sendCode(dto.getPhone());
        return Result.success();
    }

    @PostMapping("/login")
    public Result<Consumer> login(@Valid @RequestBody LoginDTO dto) {
        Consumer consumer = consumerService.login(dto.getPhone(), dto.getCode(), dto.getCaptchaKey());
        return Result.success(consumer);
    }

    @GetMapping("/info")
    public Result<Consumer> info(@RequestParam String phone) {
        Consumer consumer = consumerService.getByPhone(phone);
        if (consumer == null) {
            return Result.fail(404, "用户不存在");
        }
        return Result.success(consumer);
    }

    @PutMapping("/update")
    public Result<Consumer> update(@Valid @RequestBody ConsumerUpdateDTO dto) {
        Consumer consumer = consumerService.updateConsumer(dto);
        return Result.success(consumer);
    }
}
