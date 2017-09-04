package main;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;

public class Main {
	public static UndirectedGraph _importedGraph;
	public static List<Node> _facNodes;
	public static List<Node> _resNodes;


	public static void main(String[] args){


//		i.setXY((UndirectedGraph)graph);
//
//		Node[] nodeArray = graph.getNodes().toArray();
//		int size = nodeArray.length;
//		Random rand = new Random();
//
//		for(int index = 0; index < 100; index++){
//			Node node1 = nodeArray[rand.nextInt(size)];
//			Node node2 = nodeArray[rand.nextInt(size)];
//
//			System.out.println("Distances: "+Utility.euclidDistance(node1, node2));
//		}
//
//		HashMap<org.gephi.graph.api.Node, Double> foundLocations = ReverseGreedy.Search(100, graph);
//
//		Double weight = 0.0;
//		for(org.gephi.graph.api.Node node : foundLocations.keySet()){
//			System.out.println("THE CHOSEN NODES ARE : " + node.getLabel());
//			weight+=foundLocations.get(node);
//		}
//		System.out.println("ALGORITHM COMPLETE, TOTAL COST IS: " + weight);
//
//
//		for (List<org.gephi.graph.api.Node> nodeList :Utility.findInitialK(graph, 3,75,"Eigenvector Centrality")) {
//			output.export(nodeList);
//		}
//

		Input i = new Input();

		Graph graph = i.importGraph("300m.gml");

		SingleSwap ss = new SingleSwap();
//		boolean useEuclidean = false;
//		List<Node> result = ss.Search((UndirectedGraph)graph,1, useEuclidean, true);
//
//		System.out.printf("THE RESULTING SCORE FOR SINGLE SWAP IS: %f\n",Utility.calculateFinalScore(graph, result, useEuclidean));
		int facCount = 18;

		long startTime = System.currentTimeMillis();
//		List<Node> rgresult = ReverseGreedy.Search(facCount, graph, true);
		List<Node> rgresult = ss.Search((UndirectedGraph)graph,facCount, false, true);
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		long minutes = (totalTime / 1000) / 60;
		long seconds = (totalTime / 1000) % 60;

		System.out.println("Time taken for Single Swap (non-Clustering, non-Euclidean) and "+facCount+" facilities: " + totalTime +" ("+minutes+":"+seconds+")");

		System.out.printf("THE RESULTING SCORE FOR SINGLE SWAP IS: %f\n",Utility.calculateFinalScore(graph, rgresult, false));

		Output output = new Output();
		output.export(rgresult);
 	}
}
