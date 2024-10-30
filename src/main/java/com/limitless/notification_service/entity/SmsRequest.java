package com.limitless.notification_service.entity;


import com.limitless.notification_service.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;


@Data
@Entity
@Table(name = "sms_request")
@AllArgsConstructor
@NoArgsConstructor
public class SmsRequest implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "message", nullable = false,columnDefinition = "TEXT")
    String message;

    @Column(name = "to_number", nullable = false)
    String toNumber;

    @Column(name = "created_date", nullable = false)
    Date createdDate;

    @Column(name = "modified_date", nullable = true)
    Date modifiedDate;

    @Column(name = "retry_time", nullable = true)
    Date retryTime;

    @Column(name = "error_message", nullable = true, columnDefinition = "TEXT")
    String errorMessage;

    @Column(name = "retry_count", nullable = true, columnDefinition = "TEXT")
    Integer retryCount;

    @Column(name = "status", nullable = true, columnDefinition = "TEXT")
    @Enumerated(EnumType.STRING)
    Status status;

    @Column(name = "message_id", nullable = true)
    String messageId;

    @Column(name = "sender_id", nullable = true)
    String senderId;

}
