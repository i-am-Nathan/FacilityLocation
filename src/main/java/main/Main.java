package main;

import java.util.List;

import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;

public class Main {
	public static void main(String[] args){

		Input i = new Input();
//		org.gephi.graph.api.UndirectedGraph graph = i.importGraph("300m.gml");

//		for(int j = 0; j< 5; j++){
//			List<FacNode> desiredFacLocations = LocalSearch.Search(nlh, 3);
//		}
//		Input.NodeListHolder nlh = i.Import("300m.gml");

//		List<FacNode> desiredFacLocations = LocalSearch.Search(nlh, 3);

		UndirectedGraph graph = i.importGraph("300m.gml");
//
//		HashMap<org.gephi.graph.api.Node, Double> foundLocations = ReverseGreedy.Search(3, graph);
//		Double weight = 0.0;
//		for(org.gephi.graph.api.Node node : foundLocations.keySet()){
//			System.out.println("THE CHOSEN NODES ARE : " + node.getLabel());
//			weight+=foundLocations.get(node);
//		}
//		System.out.println("ALGORITHM COMPLETE, TOTAL COST IS: " + weight);

		Output output = new Output();
		SingleSwap ss = new SingleSwap();

//		List<Node> chosenFacilities = ss.Search(graph, 3);
		List<List<Node>> chosenFacilities = Utility.findInitialK(graph, 3, 75, "Eigenvector Centrality");
		for(List<Node> nodeList: chosenFacilities){
			output.export(nodeList);
		}
	}
}

class MainNode {
	protected float xCoord;
	protected float yCoord;
	
	public float getX(){
		return xCoord;
	}
	
	public float getY(){
		return yCoord;
	}
}

class PopNode extends MainNode {
	private double populationScore;
	
	public PopNode(double populationScore, float xCoord, float yCoord){
		this.populationScore = populationScore;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
	}
	
	public double getPopulationScore(){
		return populationScore;
	}
}

class FacNode extends MainNode {
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
