package com.mytaxi.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomErrorResponse {

    private Integer status;
    private String message;

}
