# smpp-service
Client smpp version 3.4 protocol version using jsmpp library 

The smpp client application uses the following tech stack

1. Spring boot
2. Java 20
3. JSMPP library
4. MySQL 8.0.39
5. Apache kafka
6. Gradle build tool

This application listens on a kafaka queue which is configured in the application.yml file. All the SMPP parameters are configured with comments in the yml file. It has a retry logic as well , in case if a connection is not available with the SMPP server, the sms is stored in the database with a retry threshold.





