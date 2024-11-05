import java.io.*; 
import java.net.*; 
import java.lang.*;
  
class UDPClient { 
  public static void main(String args[]) throws Exception 
  {
    int servPort = 0;
    String hostName = null;
    
    try {
        hostName = args[0];
        servPort = Integer.valueOf(args[1]);
    } catch (ArrayIndexOutOfBoundsException e) {
        System.out.println("Usage: client <server-name> <port#>");
        System.exit(-1);
    }
      
    BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in)); 
    DatagramSocket clientSocket = new DatagramSocket(); 
    try{
        clientSocket.setSoTimeout(5000);
    }
    catch(SocketException e){
        System.out.println("Error setting timeout");
        System.exit(-1);
    }
    InetAddress IPAddress = InetAddress.getByName(hostName); 
    byte[] sendData = new byte[1024]; 
    byte[] receiveData = new byte[1024]; 
    String sentence = null;
    int sequence = 0;
    while ((sentence = inFromUser.readLine()) != null) {
        
        String toServer = "DATA " + sequence + " " + sentence + "\n";
        sendData = toServer.getBytes();         
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, servPort); 
        clientSocket.send(sendPacket);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        
        while(true){//this loop doesn't break unless the correct ACK has arrived 
            try{//receives the ack and checks if it is correct 
                clientSocket.receive(receivePacket);
                String serverResponse = new String(receivePacket.getData());
                String tokens[] = serverResponse.split(" ");
                String sepSeq[] = tokens[1].split("\0");
				sepSeq[0] = sepSeq[0].substring(0, sepSeq[0].indexOf('\n'));
                if(Integer.valueOf(sepSeq[0]) != sequence){//if the ack is incorrect, go through the while loop again, this also restarts the timer automatically
                    continue;
                }
                break;
            }//if the socket timesout, resend the packet and wait for a response once more
            catch(SocketTimeoutException e){
                System.out.println("Timeout reached, resending last packet: DATA " + sentence);
                clientSocket.send(sendPacket);
            }
        }
        sequence++;//the sequence number gets incremented only once the ACK has been verified
        String serverResponse = new String(receivePacket.getData()); 
        System.out.println("FROM SERVER:" + serverResponse);
    }
    clientSocket.close(); 
  } 
}
