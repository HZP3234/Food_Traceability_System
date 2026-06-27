package com.foodtraceability.customers.dto;

import lombok.Data;

@Data
public class CaptchaCodeDTO {

    private String captchaKey;
    private String imageBase64;
}
