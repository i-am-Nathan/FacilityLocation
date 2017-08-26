package main;

import java.util.*;

import org.apache.xalan.templates.OutputProperties;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.omg.CORBA.UNKNOWN;
import sun.text.resources.et.CollationData_et;

public class Main {
	public static UndirectedGraph _importedGraph;
	public static List<Node> _facNodes;
	public static List<Node> _resNodes;


	public static void main(String[] args){


		Input i = new Input();

		Graph graph = i.importGraph("200m.gml");

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
//		Output output = new Output();
//
//		for (List<org.gephi.graph.api.Node> nodeList :Utility.findInitialK(graph, 3,75,"Eigenvector Centrality")) {
//			output.export(nodeList);
//		}

		SingleSwap ss = new SingleSwap();
		List<Node> result = ss.Search((UndirectedGraph)graph,3, true);

		System.out.printf("THE RESULTING SCORE FOR SINGLE SWAP IS: %f\n",Utility.calculateFinalScore(graph, result));


//		List<Node> rgresult = ReverseGreedy.Search(3, graph, false);
//
//		System.out.printf("THE RESULTING SCORE FOR REVERSE GREEDY IS: %f\n",Utility.calculateFinalScore(graph, rgresult));

//		Output o = new Output();
//		o.export(rgresult);
	}
}
