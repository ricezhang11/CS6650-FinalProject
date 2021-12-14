# CS6650-FinalProject --- building a dropdox-like file sharing system 
For a detailed introduction and walk-through of the entire system, please refer to our final report and video demo submitted together with the code repo. 

## How to run the application? 

1. clone the repo and cd into the src/main/java directory 
2. Open 5 terminal window for 5 replication servers and run 
```java -jar DataStoreServer 5000``` 
```java -jar DataStoreServer 5010```  
```java -jar DataStoreServer 5020``` 
```java -jar DataStoreServer 5030``` 
```java -jar DataStoreServer 5040``` 
You should start seeing logs that show the servers are reading previous stored highest sequence number and accepted proposals. Please refer to our video demo for more information
3. Open another terminal window and run
```java -jar ElectionServer``` You should see that the election server picks one of the servers to be the leader of the Paxos algorithm
4. Open another terminal window and run 
```java -jar DataStoreClient``` You should see shared folder created for the client in the project directory and client saying that they're ready to take your inputs
5. Now you can start give input command from the client window, please refer to our video demo for the correct formatting. Also, you must contact our team member to run the program because your IP address need to be whitelisted in our cloud Mongo Atlas cluster. Otherwise, you'll run into errors. 
