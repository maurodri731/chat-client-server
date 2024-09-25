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
import java.util.HashMap;
  
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
          System.out.print("Data from " + clientInfo.getInetAddress().getHostName() + " Port " + clientInfo.getPort() + "\n");
          int BUFFER_SIZE = 1024;
          int bytesRead;
          ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

          bytesRead = clientSockCh.read(buffer);

          if(buffer.hasArray()){
            String input = new String(buffer.array(), "UTF-8");//turn bytes into parsable string
            input = input.replace('\n', ' ');        //get rid of newlines for better processing
            String output = processReq(input, userList);            //process string
            //System.out.print(output);
            String tokens[] = output.split(" ");
            userMap.put(clientSockCh, tokens[tokens.length-1]);     //put the socket channel in the map with the associated name as the key

            output = output + "\n";
            byte b[] = output.getBytes();
            buffer = ByteBuffer.allocate(b.length);         //prepare buffer to send to client
            buffer = ByteBuffer.wrap(b);
          }

          if (bytesRead == -1) {
            System.out.print("Client hung up \n");
            clientSockCh.close();
          }
          else {
            //buffer.flip();                                      // switch from writing to reading
            clientSockCh.write(buffer);                        // read contents of buffer and write to client socket channel
          }
        }
      } 
    }
  }
  
  public static String processReq(String request, ArrayList<String> userList){
    String result = "";
    if(request.contains("REG")){
      request = request.substring(4, 37);
      String []token = request.split("\0");
      String []tokens = token[0].split(" ");
      if(tokens.length > 1)
        result = "ERR 2";
      else{
        String username = tokens[0];
        if(userList.contains(username))
          result = "ERR 0";
        else if(username.length() > 32)
          result = "ERR 1";
        else{
          userList.add(username);
          result = "ACK " + userList.size();
          for(int i = 0; i < userList.size(); i++)
            result = result + " " + userList.get(i);
        }
      } 
    }
    else if(request.contains("MESG")){
      result = "MESG";
    }
    else if(request.contains("PMSG")){
      result = "PMSG";
    }
    else if(request.contains("EXIT")){
      result = "EXIT";
    }
    else{
      result = "Please enter a valid command";
    }
    return result;
  }
}  
