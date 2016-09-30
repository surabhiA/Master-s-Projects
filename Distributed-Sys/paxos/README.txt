Compile  ##Run all commands from the directory paxos
> make --directory=src

Upload bin/RPCServer.jar file to server machines and bin/RPCClient.jar file to Client machines

To run client
> java -cp <PathTO.jar>/RPCClient.jar RPCClient <ServerIPaddresses>

To run server
> java -cp <PathTO.jar>/RPCServer.jar -Djava.rmi.server.hostname=<ServersOwnIPaddress> RPCServer <PortNumber> <UniqueIntegerServerIDLessThan10> <AllServerIPaddresses>
