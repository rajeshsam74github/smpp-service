package com.limitless.notification_service.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limitless.notification_service.dto.SmsRequestDto;
import com.limitless.notification_service.entity.SmsRequest;
import com.limitless.notification_service.repository.SmsRequestRepository;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Log4j2
public class SMSRetryService {

    @Value("${spring.kafka.topic-sms}")
    public String topic;

    @Value("${spring.kafka.consumer.group-id}")
    public String groupId;

    @Autowired
    KafkaProducerService kafkaProducerService;

    @Autowired
    SmsRequestRepository smsRequestRepository;


    @Scheduled(fixedRate = 30000)
    @Transactional
    public void retry() throws JsonProcessingException {
        try {
            log.info("Retry started");
            List<SmsRequest> smsRequestList = smsRequestRepository.findSMSRetryForUpdate(5, new Date());

            log.info("smsRequestList size " + smsRequestList.size());
            for (SmsRequest smsRequest : smsRequestList) {
                try {
                    SmsRequestDto smsRequestDto = new SmsRequestDto();

                    smsRequestDto.setMessage(smsRequest.getMessage());
                    smsRequestDto.setToNumber(smsRequest.getToNumber());
                    smsRequestDto.setId(smsRequest.getId());
                    smsRequest.setRetryCount(smsRequest.getRetryCount() + 1);
                    Date currentDate = new Date();
                    System.out.println("Current Date: " + currentDate);

                    // Using Calendar to add 2 minutes
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(currentDate);
                    calendar.add(Calendar.MINUTE, 2); // Add 2 minutes

                    Date newDate = calendar.getTime();
                    smsRequest.setRetryTime(newDate);
                    smsRequestRepository.save(smsRequest);
                    String message = new ObjectMapper().writeValueAsString(smsRequestDto);
                    kafkaProducerService.sendMessage(topic, message);
                    log.info("Processed smsRequest  id : " + smsRequest.getId());
                } catch (Exception e) {
                    log.error("Exception", e);
                    log.info("Processed smsRequest  id  error : " + smsRequest.getId());
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }
}
