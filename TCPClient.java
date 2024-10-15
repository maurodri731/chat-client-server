import java.io.*; 
import java.net.*; 
  
class TCPClient { 
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

    Socket clientSocket = new Socket(hostName,servPort); 
    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

    String sentence;
    String modifiedSentence;
    while ((sentence = inFromUser.readLine()) != null) {
      outToServer.writeBytes(sentence + '\n');
      if ((modifiedSentence = inFromServer.readLine()) != null) {
        System.out.println("FROM SERVER: " + modifiedSentence);
      }
      else {
        System.out.println("Server hung up\n");
        clientSocket.close();
        System.exit(0);
      }
    }
    clientSocket.close(); 
  } 
} 
