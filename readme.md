# Chat application

This project is a basic client-server chat application written in Java. The goal of this project was to experiment and explore Java concepts, particularly:
- Using sockets for client-server communication.
- Implementing threads.
- Writing sample tests with Mockito to understand mocking in unit tests.

There is no UI implemented; only the command line can be used.

## Disclaimer
This project is for learning purposes only. It is not suitable for production use.

## Prerequisites
- Java 17 (ensure `JAVA_HOME` is set to point to your Java installation)
- Maven for building the project

## Usage
1. Clone this repository.
2. Navigate to the project directory.
3. Compile the project:
    ```java
    mvn clean package
    ```
4. Start the server:
   - Navigate to the folder containing the server jar (path\to\the\project\server\target) and run the server:
    ```bash
    cd server/target
    java -jar server-1.0-SNAPSHOT-jar-with-dependencies.jar
    ```
    - You should see a message indicating the server has started.
5. Connect a client:
    - Navigate to the folder containing the client jar (path\to\the\project\client\target) and connect a new client:
    ```bash
    cd client/target
    java -jar client-1.0-SNAPSHOT-jar-with-dependencies.jar
    ```
    - You should see a message asking for a username.
6. Connect additional clients:
    - You can repeat step 5 in a new terminal window to connect another client.
    - You can adjust the connection limits in the ServerApp class:
        - `MAX_CLIENTS_CONNECTED` is the maximum number of simultaneous connected clients.
        - `MAX_CLIENTS_QUEUED` is the maximum number of clients in the waiting queue.
        - These settings are located in `com.lnkhdl.chat.server.ServerApp`.
7. Messaging:
    - Messages are broadcast to all connected clients by default.
    - Use `/w username message` to send a private message to a specific client.
    - Type `/exit` or press `Ctrl+C` to exit the client application.
