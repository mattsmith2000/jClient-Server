# jClient-Server
Java Client and Server Project

- Program Instructions
	1. Open an instance of the Server.jar file.
	2. Open at least one instance of the Client.jar file.
	3. You will be prompted to enter what protocol you wish to use on the Client. The possible answers are “TCP/IP” and “UDP”.
	4. You will then be prompted for the IP address of the server you wish to connect to. If the IP address you entered doesn’t exist, you will be prompted to enter another one.
	5. You can now send messages to the server.
	6. Type “—quit” to exit the application.

- Required Programs
	- Oracle’s JRE or JDK. 

- Other Requirements
	- You may need to make firewall exception. The ports used are shown when the server app is opened.
	
How it works:
	
When the server application starts it creates two threads. One listens for clients on TPC port 9000 and the other listens on UDP port 9876. When a client connects with either protocol the server will make a new thread to communicate with the client. Once connected, the server will repeat back any message the client sends to the server. 
