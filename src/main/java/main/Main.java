package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.xalan.templates.OutputProperties;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.UndirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public class Main {
	public static void main(String[] args){
		
		
		SimpleWeightedGraph<Node, DefaultWeightedEdge> testGraph = new SimpleWeightedGraph<Node, DefaultWeightedEdge>(DefaultWeightedEdge.class);
	/*	List<PopNode> popNodeList = new ArrayList<>();
		List<FacNode> facNodeList = new ArrayList<>();
		
		//Nodes
		FacNode n1 = new FacNode(true,2,2);
		FacNode n2 = new FacNode(true,3,5);
		PopNode n3 = new PopNode(2,1,1);
		PopNode n4 = new PopNode(10,4,4);
		
		facNodeList.add(n1);
		facNodeList.add(n2);
		popNodeList.add(n3);
		popNodeList.add(n4);
		
		//Adding Nodes
		testGraph.addVertex(n1);
		testGraph.addVertex(n2);
		testGraph.addVertex(n3);
		testGraph.addVertex(n4);*/

		Input i = new Input();

		Graph graph = i.importGraph("300m.gml");

		HashMap<org.gephi.graph.api.Node, Double> foundLocations = ReverseGreedy.Search(100, graph);
		Double weight = 0.0;
		for(org.gephi.graph.api.Node node : foundLocations.keySet()){
			System.out.println("THE CHOSEN NODES ARE : " + node.getLabel());
			weight+=foundLocations.get(node);
		}
		System.out.println("ALGORITHM COMPLETE, TOTAL COST IS: " + weight);
		
		Output output = new Output();

		for (List<org.gephi.graph.api.Node> nodeList :Utility.findInitialK(graph, 3,75,"Eigenvector Centrality")) {
			output.export(nodeList);
		}
	}
}

class Node {
	protected int xCoord;
	protected int yCoord;
	protected int nodeID;
	
	
	public int getX(){
		return xCoord;
	}
	
	public int getY(){
		return yCoord;
	}
	
}

class PopNode extends Node {
	String zone;
	
	public PopNode(int xCoord, int yCoord, int nodeID, String zone){
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.nodeID = nodeID;
		this.zone = zone;
	}
	public String getZone(){
		return zone;
	}

}

class FacNode extends Node {
	
	public FacNode(int xCoord, int yCoord, int nodeID){
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.nodeID = nodeID;
	}

}
