import java.io.*; 
import java.net.*; 
import java.lang.Thread;

public class Client{

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
    MyThread thread = new MyThread(inFromServer);
    thread.start();
    String sentence;
    //String modifiedSentence;
    while ((clientSocket.isConnected()) && (sentence = inFromUser.readLine()) != null) {
      outToServer.writeBytes(sentence + '\n');
      /*if ((modifiedSentence = inFromServer.readLine()) != null) {
        System.out.println("FROM SERVER: " + modifiedSentence);
      }
      else {
        System.out.println("Server hung up\n");
        clientSocket.close();
        System.exit(0);
      }*/
    }
    thread.join();
    clientSocket.close(); 
  } 
} 
class MyThread extends Thread{
  BufferedReader client;
  MyThread(BufferedReader c){
    client = c;
  }
  public void run(){
    String fromServer;
    try{
      while((fromServer = client.readLine()) != null){
        System.out.println(fromServer);
        if(fromServer.equals("MSG SERVER> Goodbye!!!"))
          System.exit(0);
      }
    }
    catch(Exception e){
      System.out.println("Server hung up");
      System.exit(0);
    }
  }
}
