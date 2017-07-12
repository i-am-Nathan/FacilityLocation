package main;

import java.util.ArrayList;
import java.util.List;

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

		FacilityAlgorithm fa = new FacilityAlgorithm();
		Node bestNode = fa.Solve(facNodeList,popNodeList);

		System.out.println(bestNode.getX() + " " + bestNode.getY());

		Input i = new Input();
		Input.NodeListHolder nlh = i.Import("300m.gml");
		List<FacNode> desiredFacLocations = LocalSearch.Search(nlh, 3);
		System.out.println(desiredFacLocations.size());
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
}
