package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gephi.graph.api.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public class Main {
	public static void main(String[] args){
		

		SimpleWeightedGraph<Node, DefaultWeightedEdge> testGraph = new SimpleWeightedGraph<Node, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		List<PopNode> popNodeList = new ArrayList<>();
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
		testGraph.addVertex(n4);

		Input i = new Input();
		org.gephi.graph.api.UndirectedGraph graph = i.importGraph("300m.gml");

//		for(int j = 0; j< 5; j++){
//			List<FacNode> desiredFacLocations = LocalSearch.Search(nlh, 3);
//		}
		Input.NodeListHolder nlh = i.Import("300m.gml");

		List<FacNode> desiredFacLocations = LocalSearch.Search(nlh, 3);

		Graph graph = i.ImportGraph("300m.gml");
		HashMap<org.gephi.graph.api.Node, Double> foundLocations = ReverseGreedy.Search(3, graph);
		Double weight = 0.0;
		for(org.gephi.graph.api.Node node : foundLocations.keySet()){
			System.out.println("THE CHOSEN NODES ARE : " + node.getLabel());
			weight+=foundLocations.get(node);
		}
		System.out.println("ALGORITHM COMPLETE, TOTAL COST IS: " + weight);



	}
}

class Node {
	protected float xCoord;
	protected float yCoord;
	
	public float getX(){
		return xCoord;
	}
	
	public float getY(){
		return yCoord;
	}
}

class PopNode extends Node {
	private float populationScore;
	
	public PopNode(float populationScore, float xCoord, float yCoord){
		this.populationScore = populationScore;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
	}
	
	public float getPopulationScore(){
		return populationScore;
	}
}

class FacNode extends Node {
	private boolean isVacant;
	
	public FacNode(boolean isVacant, float xCoord, float yCoord){
		this.isVacant = isVacant;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
	}
	
	public boolean getVacancy(){
		return isVacant;
	}
	
	public void setVacancy(boolean bool){
		isVacant = bool;
	}
}
