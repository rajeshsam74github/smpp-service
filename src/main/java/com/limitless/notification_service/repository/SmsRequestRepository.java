package com.limitless.notification_service.repository;

import com.limitless.notification_service.entity.SmsRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsRequestRepository extends JpaRepository<SmsRequest,Long> {
}
