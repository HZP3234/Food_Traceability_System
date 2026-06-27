package com.foodtraceability.customers.controller;

import com.foodtraceability.customers.component.CaptchaManager;
import com.foodtraceability.customers.dto.CaptchaCodeDTO;
import com.foodtraceability.customers.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consumer")
@RequiredArgsConstructor
@CrossOrigin
public class CaptchaController {

    private final CaptchaManager captchaManager;

    @GetMapping("/captcha")
    public Result<CaptchaCodeDTO> getCaptcha() {
        return Result.success(captchaManager.generate());
    }
}
