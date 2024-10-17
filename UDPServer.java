import java.io.*; 
import java.net.*; 
  
class UDPServer { 
  public static void main(String args[]) throws Exception 
  { 
    DatagramSocket serverSocket = new DatagramSocket(12346);
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
      String []sepSeq = tokens[2].split("\0");//separate the sequence number from all of the nulls at the end of the string

      InetAddress IPAddress = receivePacket.getAddress();//prepare for the sending of the Datagram back to the user 
      int port = receivePacket.getPort();

      if(sentence.contains("DATA") && seqNum == Integer.valueOf(sepSeq[0])){ //check if client sent a DATA message with the expected sequence number
        System.out.println("FROM CLIENT:" + sentence);//output the data if it is correct
        sentence = "ACK " + (seqNum+1);//prepare the ACK 
        seqNum++;
      }
      else{
        int temp = seqNum-1;
        sentence = "ACK " + temp;//duplicate ACK
      }
      //sending data, the processing was already done on the if-else above
      response = sentence; 
      sendData = response.getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port); 
      serverSocket.send(sendPacket);
    } 
  } 
}  