Train of thought....
when a user is created, add their user name AND their socket to the Hashmap, with the socket as the key.
After a user is done registering, every other command is gonna need their username, which can be found inside
the hashmap. Simply use the current the socket channel as the key to their username.
The username can be set for any iteration, not just the registered ones, using containsKey(socket)....

When a user wants to send a message to all of the users, use the for each to set all of the other sockets ready to be written.
This involves registering the socket channel with SelectionKey.OP_Write.
Use a global variable to write the message, then use that variable inside the isWritable section of the while loop. 
Perhaps create a separate condition for private messages? so an isWritable only for general messages and 
if(isWritable && inside datastructure ???). This datastructure could be another hashmap that pairs the receiving socket with the 
message that is going to be sent????  

registering the socket into write mode creates a new key, so technically there will be two keys registered to the same socket...
it might be worth looking into just changing the keys using interestOps rather than creating a new key all together, we will see.
I could also simply cancel() [as opposed to modifying inrerestOps()] the key after I am done writing, since this will take the key out of the selector.
If I need write again then the isReadable() can set the key to writing again.
