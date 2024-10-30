package com.limitless.notification_service.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SmsRequestDto implements Serializable {

    String message;

    String toNumber;

    Long id;
}
