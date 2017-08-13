package main;

import java.util.List;

import org.gephi.graph.api.Graph;
import org.gephi.statistics.plugin.EigenvectorCentrality;

public class LocalSearchWithSwap {
	public static List<Node> Search(Graph graph, int facCount){
		
		//Initial K
		List<List<org.gephi.graph.api.Node>> nodeLists = Utility.findInitialK(graph, facCount, 75, "Eigenvector Centrality");
		List<org.gephi.graph.api.Node> initialKList = nodeLists.get(nodeLists.size()-1);

		//Swap
		//Assuming it has the intitial facility nodes
		
		
		
		return null;
		
	}
}
