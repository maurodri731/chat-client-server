Usage- 
Use the Client with the Server and the UDPClient with the UDPServer
Command Line calls-
java Server
java Client <server-name>
<server-name> will probably be localhost, unless Server was called on another IPaddress
java UDPServer <port#> <max-seq-num>
choose the port number and the max seq number refers to the amount of number that will be used in the ack
java UDPClient <server-name> <port#> <max-seq-num>
choose the server name, probaby localhost, port# of the server, and the max-seq-num should match the one in the server
