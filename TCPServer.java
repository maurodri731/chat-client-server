//import java.io.*; 
import java.net.*;
import java.nio.*; 
//import java.nio.channels.*; 
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.util.Set;
import java.util.Iterator;
//import java.net.InetSocketAddress;
  
class TCPServer { 
  public static void main(String args[]) throws Exception 
  {
    int listenPort = 12346; 
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

          if (bytesRead == -1) {
            System.out.print("Client hung up \n");
            clientSockCh.close();
          }
          else {
            buffer.flip();                                     // switch from writing to reading
            clientSockCh.write(buffer);                        // read contents of buffer and write to client socket channel
          }
        }
      } 
    }
  } 
}  
