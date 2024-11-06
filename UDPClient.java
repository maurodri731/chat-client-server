import java.io.*; 
import java.net.*; 
import java.lang.*;
  
class UDPClient { 
  public static void main(String args[]) throws Exception 
  {
    int servPort = 0;
    String hostName = null;
    int maxSeq = 0;
    
    try {//try catch for command line arguments
        hostName = args[0];
        servPort = Integer.valueOf(args[1]);
        maxSeq = Integer.valueOf(args[2]);
    } catch (ArrayIndexOutOfBoundsException e) {
        System.out.println("Usage: client <server-name> <port#> <MAXseq#>");
        System.exit(-1);
    }
      
    BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));//used to read in the input from the user 
    DatagramSocket clientSocket = new DatagramSocket();//create the socket for the client
    try{//set the timeout for the socket, this only works after each write to server
        clientSocket.setSoTimeout(5000);
    }
    catch(SocketException e){
        System.out.println("Error setting timeout");
        System.exit(-1);
    }

    InetAddress IPAddress = InetAddress.getByName(hostName);//locate the server, this comes from the command line arguments 
    byte[] sendData = new byte[1024];//ready the buffers for receiving and sending 
    byte[] receiveData = new byte[1024]; 
    String sentence = null;
    int sequence = 0;//set the sequence number, this always starts at 0

    while ((sentence = inFromUser.readLine()) != null) {//while the user sends
        char[] sentenceChars = sentence.toCharArray();//covert the sentence read in to char array, incase there is multiple characters input at a time
        for(char c : sentenceChars){//for each character read in
            String toServer = "DATA " + sequence + " " + c + "\n";//create the data to be sent
            sendData = toServer.getBytes();         
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, servPort); 
            clientSocket.send(sendPacket);//send data
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);//create the datagram packet that will be filled with the data received
        
            while(true){//this loop doesn't break unless the correct ACK has arrived 
                try{//receives the ack and checks if it is correct 
                    clientSocket.receive(receivePacket);//receive the data
                    String serverResponse = new String(receivePacket.getData());//turn it into a string
                    String tokens[] = serverResponse.split(" ");//pre-process the data
                    String sepSeq[] = tokens[1].split("\0");
				    sepSeq[0] = sepSeq[0].substring(0, sepSeq[0].indexOf('\n'));//get rid of the newline char to process the integer correcetly
                    if(Integer.valueOf(sepSeq[0]) != sequence){
                        continue;//if the ack is incorrect, go through the while loop again, this also restarts the timer automatically
                    }
                    break;//if the ack is correct, break the loop and process the response for the client to see in their console
                }//if the socket timesout, resend the packet and wait for a response once more
                catch(SocketTimeoutException e){
                    System.out.println("Timeout reached, resending last packet: DATA " + c);
                    clientSocket.send(sendPacket);
                }
            }
            if(sequence == maxSeq-1)
                sequence = 0;
            else
                sequence++;//the sequence number gets incremented only once the ACK has been verified
            String serverResponse = new String(receivePacket.getData()); 
            System.out.println("FROM SERVER:" + serverResponse);
        }
    }
    clientSocket.close(); 
  } 
}
