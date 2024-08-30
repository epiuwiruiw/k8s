package com.beyond.ordersystem.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommonErrorDto {
    private int status_code;
    private String error_message;
}
