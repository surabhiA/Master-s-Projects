all: client server jar

client: RPCClient.class KVStore.class KeyNotFoundException.class

server: RPCServer.class KVStore.class KVStoreImpl.class KeyNotFoundException.class

jar: client server
	cd ../bin; jar cf RPCServer.jar RPCServer.class KVStore.class KVStoreImpl.class KeyNotFoundException.class
	cd ../bin; jar cf RPCClient.jar RPCClient.class KVStore.class KeyNotFoundException.class

%.class: %.java
	javac $< -d ../bin/

clean:
	rm -f ../bin/*.class ../bin/*.jar