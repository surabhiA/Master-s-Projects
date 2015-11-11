This is a single server-multiple client KV Store program which uses Remote Method Invocation for client-server communication. It is multithreaded and handles client and server side logging.

Compile the Server , Client and KVStore  ##Run all commands from the directory RPCProject
> make --directory=src

To run client
> java -cp bin/RPCClient.jar RPCClient <ServerIPaddress>

To run server
> java -cp bin/RPCServer.jar -Djava.rmi.server.hostname=<ServerIPaddress> RPCServer 9999