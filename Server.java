import java.net.*;
import java.nio.*; 
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.util.Set;
import java.util.Iterator;
//import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.*;
  
public class Server {
  public static void main(String args[]) throws Exception {
    int listenPort = 12346; 
    ArrayList<String> userList = new ArrayList<String>(); 
    HashMap<SocketChannel, String> userMap = new HashMap<>();
    //String clientSentence;
    //String capitalizedSentence;


 
    ServerSocketChannel welcomeSockCh = ServerSocketChannel.open();
    welcomeSockCh.socket().bind(new InetSocketAddress(listenPort));
    welcomeSockCh.configureBlocking(false);
    Selector sel = Selector.open();
    
    welcomeSockCh.register(sel, SelectionKey.OP_ACCEPT);  // register the listening socket channel with the selector
                                                   
                                                        
    while(true) {
      sel.select();                                // wait until one or more key is ready
      Set selKeys = sel.selectedKeys();            // set of keys that are ready 
      Iterator keyIter = selKeys.iterator();

      while (keyIter.hasNext()) {                  // go through each of the keys that are ready
        SelectionKey key = (SelectionKey) keyIter.next();
        keyIter.remove();
        
        if (key.isAcceptable()) {                                   // listing socket received connect from client
          SocketChannel clientSockCh = welcomeSockCh.accept();      // create a new data socket 
          clientSockCh.configureBlocking(false);
          clientSockCh.register(sel, SelectionKey.OP_READ);         // register new socket with the selector
          continue;
        } 
        if (key.isReadable()) {                                    // socket has data ready to be read
          SocketChannel clientSockCh = (SocketChannel) key.channel();
          Socket clientInfo = clientSockCh.socket();
          System.out.print("Data from " + clientInfo.getInetAddress().getHostName() + " Port " + clientInfo.getPort() +  " " + userMap.get(clientSockCh) + "\n");
          int BUFFER_SIZE = 1024;
          int bytesRead = 0;
          ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
          String input = "";
          String output = "";

          while(true){
            try{
              bytesRead += clientSockCh.read(buffer);
              input = new String(buffer.array());
              if(input.contains("\n"))
                break;
            }
            catch(SocketException e){
              key.cancel();
              if(userMap.get(clientSockCh) != null){
                String username = userMap.get(clientSockCh);
                String message = "MESG SERVER> " + username + " has quit the chat\n";
                for(Map.Entry<SocketChannel, String> entry : userMap.entrySet()){
                  if(entry.getKey() != clientSockCh){
                    try{
                      byte b[] = message.getBytes();
                      ByteBuffer buffer2 = ByteBuffer.allocate(1024);
                      buffer2 = ByteBuffer.wrap(b);
                      entry.getKey().write(buffer2);
                    }
                    catch(Exception e2){
                      e2.getStackTrace();
                    }
                  }
                } 
                userMap.remove(clientSockCh);
                userList.remove(username);
              }
              clientSockCh.close();
              break;
            }
          }
            

          if(buffer.hasArray()){
            //String input = new String(buffer.array(), "UTF-8");//turn bytes into parsable string
            input = input.replace('\n', ' ');        //get rid of newlines for better processing
            //System.out.println("User input: " + input + " " + bytesRead);
            output = processReq(input, userList, clientSockCh, userMap);            //process string

            output = output + "\n";
            byte b[] = output.getBytes();
            buffer = ByteBuffer.wrap(b);
          }

          if (bytesRead == -1) {
            System.out.print("Client hung up \n");
            clientSockCh.close();
          }
          else {
            //buffer.flip();                                      // switch from writing to reading
            if(clientSockCh.isOpen())
              clientSockCh.write(buffer);                        // read contents of buffer and write to client socket channel
          }
        }
      } 
    }
  }
  
  public static String processReq(String request, ArrayList<String> userList, SocketChannel clientSockCh, HashMap<SocketChannel, String> userMap) throws Exception{
    String result = "";
    if(request.contains("REG") && !userMap.containsKey(clientSockCh)){//registering
      request = request.substring(4, 37);
      String []token = request.split("\0");
      String []tokens = token[0].split(" ");
      if(tokens.length > 1)
        result = "ERR username contains spaces, try REG again";
      else{
        String username = tokens[0];
        if(userList.contains(username))
          result = "ERR username is taken, try REG again";
        else if(username.length() > 32)
          result = "ERR usernames is greater than 32 characters, try REG again";
        else if(userMap.containsKey(clientSockCh))
          result = "Client already has a username associated with this connection, try another command";
        else{
          userList.add(username);
          userMap.put(clientSockCh, username); //put the socket channel in the map with the associated name as the key
          result = "ACK " + userList.size();
          for(int i = 0; i < userList.size(); i++)
            result = result + " " + userList.get(i);
          String message = "MSG SERVER> user " + username + " just joined\n";
          for(Map.Entry<SocketChannel, String> entry : userMap.entrySet()){
            if(entry.getKey() != clientSockCh){
              try{
                byte b[] = message.getBytes();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                buffer = ByteBuffer.wrap(b);
                entry.getKey().write(buffer);
              }
              catch(Exception e){
                e.getStackTrace();
              }
            }
          }  
        }
      } 
    }
    else if(request.contains("REG") && userMap.containsKey(clientSockCh))//user tries registering but has already registered
      return "Channel has already been registerd with username " + userMap.get(clientSockCh);
    else if((request.contains("MESG") || request.contains("PMSG") || request.contains("EXIT")) && userMap.containsKey(clientSockCh)){//other valid commands
      if(request.contains("MESG")){
        String message = "MSG " + userMap.get(clientSockCh) + "> " + request.substring(5) + "\n";

        for(Map.Entry<SocketChannel, String> entry : userMap.entrySet()){
          if(entry.getKey() != clientSockCh){
            try{
              byte b[] = message.getBytes();
              ByteBuffer buffer = ByteBuffer.allocate(1024);
              buffer = ByteBuffer.wrap(b);
              entry.getKey().write(buffer);
            }
            catch(Exception e){
              e.getStackTrace();
            }
          }
        }
        result = "Message sent to everyone";
      }
      else if(request.contains("PMSG")){
        String[] temp = request.split(" ");
        String receiver = temp[1];
        String message = "MSG " + userMap.get(clientSockCh) + "> " + request.substring(request.indexOf(receiver)+receiver.length()) + "\n";
        for(Map.Entry<SocketChannel, String> entry : userMap.entrySet()){
          if(entry.getValue().equals(receiver)){
            try{
              byte b[] = message.getBytes();
              ByteBuffer buffer = ByteBuffer.allocate(1024);
              buffer = ByteBuffer.wrap(b);
              entry.getKey().write(buffer);
              return "Message sent to " + receiver;
            }
            catch(Exception e){
              e.getStackTrace();
            }
          }
        }
        result = "ERR receiving user is not registered in the chat";
      }
      else if(request.contains("EXIT")){
        String username = userMap.get(clientSockCh);
        userList.remove(username);
        userMap.remove(clientSockCh);
        String userMessage = "MSG SERVER> Goodbye!!!\n";
        try{
          byte b[] = userMessage.getBytes();
          ByteBuffer buffer = ByteBuffer.allocate(1024);
          buffer = ByteBuffer.wrap(b);
          clientSockCh.write(buffer);
        }
        catch(Exception e){
          e.getStackTrace();
        }
        String message = "MSG SERVER> " + username + " has left the chat\n";
        for(Map.Entry<SocketChannel, String> entry : userMap.entrySet()){
          if(entry.getKey() != clientSockCh){
            try{
              byte b[] = message.getBytes();
              ByteBuffer buffer = ByteBuffer.allocate(1024);
              buffer = ByteBuffer.wrap(b);
              entry.getKey().write(buffer);
            }
            catch(Exception e){
              e.getStackTrace();
            }
          }
        }
        clientSockCh.close();
        result = null;
      }
    }
    else{//any other command is not supported
      if(!userMap.containsKey(clientSockCh))
        result = "Please register for the chat using REG before entering any command";
      else
        result = "Please enter a valid command";
    }
    return result;
  }
}  
