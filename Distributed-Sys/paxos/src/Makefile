all: client server jar

client-classes = RPCClient.class KVStore.class KeyNotFoundException.class TimedOutException.class
server-classes = RPCServer.class KVStore.class KVStoreImpl.class \
		KeyNotFoundException.class DataStore.class Acceptor.class \
		AcceptorInterface.class Gremlin.class Learner.class LearnerInterface.class \
		Operation.class OperationVerbs.class Proposer.class ProposerInterface.class \
		RejectedException.class TimedOutException.class
internal-classes = Gremlin\$$Lookout.class RejectedException\$$RejectedReason.class \
		Learner\$$1.class KVStoreImpl\$$1.class KVStoreImpl\$$Counter.class

client: $(client-classes) 

server: $(server-classes)

jar: client server
	cd ../bin; jar cf RPCServer.jar $(server-classes) $(internal-classes)
	cd ../bin; jar cf RPCClient.jar $(client-classes) 

%.class: %.java
	javac $< -d ../bin/

clean:
	rm -f ../bin/*.class ../bin/*.jar
