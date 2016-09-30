all: client server jar

client: UDPClient.class KVRequest.class KVResponse.class Logger.class

server: UDPServer.class KVRequest.class KVResponse.class KVStore.class Logger.class

jar: client server
	cd ../bin; jar cf UDPServer.jar UDPServer.class KVRequest.class KVResponse.class KVStore.class Logger.class
	cd ../bin; jar cf UDPClient.jar UDPClient.class KVRequest.class KVResponse.class Logger.class

%.class: %.java
	javac $< -d ../bin/

clean:
	rm -f ../bin/*.class ../bin/*.jar

## To run client
# java -cp ../bin/UDPClient.jar UDPClient localhost 1234

## To run server
# java -cp ../bin/UDPServer.jar UDPServer 1234