all: client server jar

client: RPCClient.class KVStore.class KeyNotFoundException.class

server: RPCServer.class KVStore.class KVStoreImpl.class TwoPhaseCommit.class TwoPhaseCommitImpl.class TPCCoordinator.class KeyNotFoundException.class DataStore.class ResponseTPC.class RollbackException.class 

jar: client server
	cd ../bin; jar cf RPCServer.jar RPCServer.class KVStore.class KVStoreImpl.class TwoPhaseCommit.class TwoPhaseCommitImpl.class TPCCoordinator.class KeyNotFoundException.class DataStore.class ResponseTPC.class RollbackException.class DataStore\$$LockManager.class 
	cd ../bin; jar cf RPCClient.jar RPCClient.class KVStore.class KeyNotFoundException.class

%.class: %.java
	javac $< -d ../bin/

clean:
	rm -f ../bin/*.class ../bin/*.jar