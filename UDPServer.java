import java.io.*; 
import java.net.*; 
  
class UDPServer { 
  public static void main(String args[]) throws Exception 
  { 
    int servPort = 0;
    int maxSeq = 0;
    try{
      servPort = Integer.valueOf(args[0]);
      maxSeq = Integer.valueOf(args[1]);
    }
    catch(ArrayIndexOutOfBoundsException e){
      System.out.println("Usage <port#> <maxseq#>");
      System.exit(-1);
    }
    DatagramSocket serverSocket = new DatagramSocket(servPort);
    int seqNum = 0; 
    while(true) 
    { 
      byte[] receiveData = new byte[1024];//buffers for receiving and sending data 
      byte[] sendData  = new byte[1024]; 
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);//create a datagram to fit the receiving data 

      serverSocket.receive(receivePacket);//receive the data 
      String response;
      String sentence = new String(receivePacket.getData());//turn it into a string
      String tokens[] = sentence.split(" ");//split DATA, the character, and the sequence number from each other

      InetAddress IPAddress = receivePacket.getAddress();//prepare for the sending of the Datagram back to the user 
      int port = receivePacket.getPort();

      if(sentence.contains("DATA") && seqNum == Integer.valueOf(tokens[1])){ //check if client sent a DATA message with the expected sequence number
        System.out.println("FROM CLIENT:" + sentence);//output the data if it is correct
        response = "ACK " + seqNum + "\n";//prepare the ACK
        if(seqNum == maxSeq-1)
          seqNum = 0;
        else
          seqNum++;
      }
      else{
        int temp = seqNum-1;
        response = "ACK " + temp + "\n";//duplicate ACK
      }
      //sending data, the processing was already done on the if-else above
      sendData = response.getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port); 
      serverSocket.send(sendPacket);
    } 
  } 
}  
