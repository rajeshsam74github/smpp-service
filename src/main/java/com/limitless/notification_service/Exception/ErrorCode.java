package com.limitless.notification_service.Exception;

public enum ErrorCode {
    INVALID_REQUEST(400, "Invalid request parameters"),
    UNAUTHORIZED(401, "Unauthorized access"),
    FORBIDDEN(403, "Forbidden action"),
    NOT_FOUND(404, "Resource not found"),
    EMAILID_NULL(405,"Please provide Valid email id"),
    USERIDENTITY_NULL(406,"email or phone number is null"),
    PASSWORD_NULL(407,"password is null"),
    CONFIRM_PASSWORD_NULL(408,"password is null"),
    PASSWORD_AND_CONFIRM_PASSWORD_NOT_EQUAL(409,"password and confirm password does not match"),
    INVALID_MOBILE_NUMBER_FORMAT(410,"Invalid mobile number format"),
    INVALID_EMAIL(411,"Invalid EMAIL  "),
    USER_ALREADY_REGISTERED(412,"User has already registered"),
    MESSAGE_NULL(413,"message is null"),
    INVALID_OTP(414,"Invalid otp  "),
    INTERNAL_SERVER_ERROR(500, "Internal server error"),
    SUCCESS(200,"SUCCESS");

    private final int code;
    private final String message;

    // Constructor to initialize the error code and message
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    // Getter to retrieve the error code
    public int getCode() {
        return code;
    }

    // Getter to retrieve the error message
    public String getMessage() {
        return message;
    }
}
