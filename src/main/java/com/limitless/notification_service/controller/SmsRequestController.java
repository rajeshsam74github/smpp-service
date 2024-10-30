package com.limitless.notification_service.controller;

import com.limitless.notification_service.dto.SmsRequestDto;
import com.limitless.notification_service.service.SmsSenderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/limitless/notification")
@Log4j2
public class SmsRequestController {
    @Autowired
    SmsSenderService smsSender;

   @PostMapping("/sendSms")
    public Object sendSms(@RequestBody SmsRequestDto smsRequestDto){
       return smsSender.sendSms(smsRequestDto);

    }
}
