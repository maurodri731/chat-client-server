import java.io.*; 
import java.net.*; 
import java.lang.*;
  
class UDPClient { 
  public static void main(String args[]) throws Exception 
  {
    int servPort = 12346;
    String hostName = null;
    
    try {
        hostName = args[0];
    } catch (ArrayIndexOutOfBoundsException e) {
        System.out.println("Need argument: remoteHost");
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
        String toServer = "DATA " + sentence + " " + sequence;
        sendData = toServer.getBytes();         
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, servPort); 
        clientSocket.send(sendPacket);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        while(true){ 
            try{ 
                clientSocket.receive(receivePacket);
                break;
            }
            catch(SocketTimeoutException e){
                System.out.println("Timeout reached, resending last packet");
                clientSocket.send(sendPacket);
            }
        }
        sequence++;
        String serverResponse = new String(receivePacket.getData()); 
        System.out.println("FROM SERVER:" + serverResponse);
    }
    clientSocket.close(); 
  } 
}