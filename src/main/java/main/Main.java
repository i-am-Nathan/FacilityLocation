package main;

import jgrapht.graph.DefaultWeightedEdge;
import jgrapht.graph.SimpleWeightedGraph;

public class Main {
	public static void main(String[] args){
		
		SimpleWeightedGraph<Node, DefaultWeightedEdge> testGraph = new SimpleWeightedGraph<Node, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		//Nodes
		FacNode n1 = new FacNode(true,2,2);
		FacNode n2 = new FacNode(true,3,5);
		PopNode n3 = new PopNode(2,1,1);
		PopNode n4 = new PopNode(10,4,4);
		
		
		//Adding Nodes
		testGraph.addVertex(n1);
		testGraph.addVertex(n2);
		testGraph.addVertex(n3);
		testGraph.addVertex(n4);

		FacilityAlgorithm fa = new FacilityAlgorithm();
		Node bestNode = fa.Solve(testGraph);
		
		System.out.println(bestNode.getX() + " " + bestNode.getY());
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
	private int population;
	
	public PopNode(int population, float xCoord, float yCoord){
		this.population = population;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
	}
	
	public int getPopulation(){
		return population;
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
