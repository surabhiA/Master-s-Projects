all: project jar

project: tcss543.class FordFulkerson.class FordFulkersonAlt.class PreFlowPushAlgo.class ScalingMaxFlow.class Vertex.class Edge.class GraphInput.class SimpleGraph.class InputLib.class KeyboardReader.class

jar: project
	jar cf tcss543Project.jar tcss543.class FordFulkerson.class FordFulkersonAlt.class PreFlowPushAlgo.class ScalingMaxFlow.class Vertex.class Edge.class GraphInput.class SimpleGraph.class InputLib.class KeyboardReader.class FordFulkersonAlt\$$Edge.class FordFulkersonAlt\$$Vertex.class InputLib\$$InputError.class PreFlowPushAlgo\$$Edge.class PreFlowPushAlgo\$$Node.class 

%.class: %.java
	javac $<