package com.limitless.notification_service.Exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Error extends Exception implements Serializable{
    String errorMessage;
    Object response;
    Object responseCode;


}
