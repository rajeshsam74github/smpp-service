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

Installation instructions

1. Install java 20 or higher version
2. Install MySQL database
3. Clone this repository under a folder
4. Edit the application.yml file under the src/resources folder for all configuration parameter
5. Open the command terminal in windows or a terminal in unix
6. Run the following command
   gradlew clean build
7. This will build a runnable jar file under build/libs directory.
8. Run  the application as follows
   java -jar <name of the jar>





