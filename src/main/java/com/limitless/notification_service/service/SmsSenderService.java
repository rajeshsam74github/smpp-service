package com.limitless.notification_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.limitless.notification_service.dto.SmsRequestDto;
import com.limitless.notification_service.entity.SmsRequest;
import com.limitless.notification_service.enums.Status;
import com.limitless.notification_service.repository.SmsRequestRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.jsmpp.bean.*;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.SubmitSmResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class SmsSenderService {


    @Value("${spring.kafka.topic-sms}")
    public String topic;

    @Value("${spring.kafka.consumer.group-id}")
    public String groupId;


    @Value("${host}")
    String host ; // Replace with your SMPP host
    @Value("${port}")
    int port ; // Replace with your SMPP port
    @Value("${systemId}")
    String systemId ; // Your SMPP system ID
    @Value("${password}")
    String password ; // Your SMPP password
    @Value("${systemType}")
    String systemType;
    @Value("${senderid}")
    String senderid ;
    @Value("${serviceType}")
    String serviceType ;
    @Value("${protocolId}")
    byte protocolId ;
    @Value("${priorityFlag}")
    byte priorityFlag ;
    @Value("${replaceIfPresentFlag}")
    byte replaceIfPresentFlag;
    @Value("${validityPeriod}")
    Integer validityPeriod;
    @Value("${esmClass}")
    byte esmClass ;
    @Value("${registeredDelivery}")
    byte registeredDelivery;
    @Value("${dataCoding}")
    static byte dataCoding;
    @Value("${smDefaultMsgId}")
    byte smDefaultMsgId;


    String addressRange;
    InterfaceVersion interfaceVersion =InterfaceVersion.IF_34;

    private SMPPSession session;
    private ScheduledExecutorService scheduler;
    private boolean isConnected = false;

    @Autowired
    SmsRequestRepository smsRequestRepository;

    @PostConstruct
    void init() {
        log.info("Post construct called: " );
        connect();
        startTransactionTimer(); // Start the transaction timer
        log.info("Post construct end: " );
    }

    @KafkaListener(topics = "#{@smsSenderService.topic}", groupId = "#{@smsSenderService.groupId}")
    public void listen(String message) {
        try {

            log.info("Received message: " + message);
            ObjectMapper objectMapper = new ObjectMapper();
            SmsRequestDto smsRequestDto = objectMapper.readValue(message,SmsRequestDto.class);
            this.sendSms(smsRequestDto);
            log.info("listener completed successfully");
        }
        catch (Exception e){
            log.error("exception occurred in listener",e);
        }
    }


    public Object sendSms( SmsRequestDto smsRequestDto){
        log.info("sendSms called with async " );
        sendToSMPPServer(smsRequestDto);
        log.info("sendSms async returned success " );
        return "SUCCESS";
    }

    @Async
    public void sendToSMPPServer(SmsRequestDto smsRequestDto) {
        log.info("sendSms called with message and phone number " + smsRequestDto);

        SmsRequest smsRequest = null;
        if( smsRequestDto.getId() == null ){
            smsRequest =  new SmsRequest();
            smsRequest.setCreatedDate(new Date());
            smsRequest.setRetryCount(0);
            smsRequest.setSenderId(senderid);
            smsRequest.setMessage(smsRequestDto.getMessage());
            smsRequest.setToNumber(smsRequestDto.getToNumber());
        }
        else {
            smsRequest = smsRequestRepository.findById(smsRequestDto.getId()).get();
            smsRequest.setRetryCount(smsRequest.getRetryCount() + 1);
            smsRequest.setModifiedDate(new Date());

        }
        smsRequest.setStatus(Status.INPROGRESS);
        smsRequest = smsRequestRepository.save(smsRequest);
        try {
            if( isConnected == false ){
                //To be persisted for retry
                smsRequest.setStatus(Status.NOCONNECTION);
                smsRequest.setModifiedDate(new Date());
                smsRequest = smsRequestRepository.save(smsRequest);
                return;
            }
            log.info("Connectivity available with smpp server : isConnected " + isConnected  );
            org. jsmpp.bean.TypeOfNumber sourceAddrTon = TypeOfNumber.NATIONAL ;
            org. jsmpp.bean.NumberingPlanIndicator sourceAddrNpi = NumberingPlanIndicator.NATIONAL;
            String sourceAddr = senderid;
            org. jsmpp.bean.TypeOfNumber destAddrTon = TypeOfNumber.NATIONAL;
            org. jsmpp.bean.NumberingPlanIndicator destAddrNpi = NumberingPlanIndicator.NATIONAL;
            String destinationAddr = smsRequestDto.getToNumber();
            org. jsmpp.bean.ESMClass esmClass = new ESMClass(this.esmClass);


            String scheduleDeliveryTime=null;
            //Need to set the date and time for validity
            String validityPeriod=null;
            if( this.validityPeriod == 0){
                validityPeriod=null;
            }else {
                //set date and time
                validityPeriod = addMinutesAndFormat(this.validityPeriod );
            }
            org.jsmpp.bean.RegisteredDelivery registeredDelivery = new RegisteredDelivery(this.registeredDelivery);
            org. jsmpp.bean.DataCoding dataCoding = new DataCoding() {
                @Override
                public byte toByte() {
                    return SmsSenderService.dataCoding;
                }
            };
            byte[] shortMessage = smsRequestDto.getMessage().getBytes();



            SubmitSmResult response = session.submitShortMessage(serviceType,sourceAddrTon,
                    sourceAddrNpi,sourceAddr,destAddrTon,destAddrNpi,destinationAddr,esmClass,protocolId,priorityFlag,
                    scheduleDeliveryTime,validityPeriod, registeredDelivery, replaceIfPresentFlag, dataCoding, smDefaultMsgId
                  ,shortMessage);
            log.info("Message sent. ID: " + response.getMessageId());
            smsRequest.setMessageId(response.getMessageId());
            smsRequest.setStatus(Status.SUCCESS);
            smsRequest.setModifiedDate(new Date());
            smsRequest = smsRequestRepository.save(smsRequest);
            log.info("SMS sent succssfully and saved ");
        }catch (Exception e) {
            log.error("Exception e", e);
            this.isConnected = false;
            String stackTrace = getStackTraceAsString(e);
            smsRequest.setErrorMessage(stackTrace);
            smsRequest.setStatus(Status.FAILURE);
            smsRequest.setModifiedDate(new Date());
            smsRequest = smsRequestRepository.save(smsRequest);
        }
    }

    public static String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString(); // Return the stack trace as a string
    }
    public  String addMinutesAndFormat(int minutes) {
        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Add the specified minutes
        LocalDateTime updatedDateTime = currentDateTime.plusMinutes(minutes);

        // Define the desired format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");

        // Format the updated date and time
        return updatedDateTime.format(formatter);
    }
    public boolean isConnectionLive() {
            // Send a no-operation command (bind or query) to check the connection
            return this.isConnected;

    }


    public void connect() {
        try {
            log.info("connect called....");
            session = new SMPPSession();
            session.setEnquireLinkTimer(30000);
            session.setTransactionTimer(2000);
            // Connect and bind to the SMPP server
            TypeOfNumber addrTon = TypeOfNumber.NATIONAL;
            NumberingPlanIndicator addrNpi = NumberingPlanIndicator.NATIONAL;
            BindParameter bindParameter = new BindParameter(BindType.BIND_TX, systemId,password,systemType,addrTon,addrNpi,addressRange,interfaceVersion);
            session.connectAndBind(host,port, bindParameter);
            isConnected = true;
            log.info("Connected and bound to the SMPP server.");
        }  catch (Exception e) {
            isConnected = false;
            log.error("Connection to the SMPP server failed.", e);
        }
    }

    public void disconnect() {
        if (scheduler != null) {
            log.info("shutting down the scheduler thread");
            scheduler.shutdown();
        }
        if (session != null) {
            log.info("Started disconnecting with smpp server");
            session.unbindAndClose();
            isConnected = false;
            log.info("Disconnected from the SMPP server.");
        }
    }


    private void startTransactionTimer() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (!isConnectionLive()) {
                log.info("Connection lost. Attempting to reconnect...");
                reconnect();
            }
        }, 0, 10, TimeUnit.SECONDS); // Adjust the interval as needed
    }
    private void reconnect() {
        disconnect(); // Ensure the old session is closed
        connect(); // Attempt to connect again
    }




}
