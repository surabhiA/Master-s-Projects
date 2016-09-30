Compile  ##Run all commands from the directory TwoPhaseCommit
> make --directory=src

Upload RPCServer.jar file to server machines and RPCClient.jar file to Client machines

To run client
> java -cp <PathTO.jar>/RPCClient.jar RPCClient <ServerIPaddresses>

To run server
> java -cp <PathTO.jar>/RPCServer.jar -Djava.rmi.server.hostname=<Server'sOwnIPaddress> RPCServer <PortNumber> <AllServerIPaddresses>