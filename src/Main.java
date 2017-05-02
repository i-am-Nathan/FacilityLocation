import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

public class Main {
	public static void main(String[] args){
		
		SimpleWeightedGraph<Node, DefaultWeightedEdge> testGraph = new SimpleWeightedGraph<Node, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		//Nodes
		FacNode n1 = new FacNode(true,2,2);
		PopNode n2 = new PopNode(3,4,4);
		FacNode n3 = new FacNode(true,3,5);
		
		//Adding Nodes
		testGraph.addVertex(n1);
		testGraph.addVertex(n2);
		testGraph.addVertex(n3);
		
		//Adding Edges
		DefaultWeightedEdge e1 = testGraph.addEdge(n1,n2);
		DefaultWeightedEdge e2 = testGraph.addEdge(n2, n3);
		
		//Adding Edge Weights (Road Distance)
		testGraph.setEdgeWeight(e1, 10);
		testGraph.setEdgeWeight(e2,5);
		
		
		System.out.println(FacilityAlgorithm.Solve(testGraph));
		
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
