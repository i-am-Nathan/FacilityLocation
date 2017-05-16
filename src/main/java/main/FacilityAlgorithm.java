package main;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.List;

public class FacilityAlgorithm {
	/**
	 * This method solves the facility location problem, given a graph with facility and population nodes.
	 * It iterates through all the facility nodes, whic interates through all the population nodes to find the
	 * cumulative "score" of each facility node, which depends on the population and distance.
	 *
	 * Assumptions + Limitations: This algorithm only places one (the best) facility, the facility is assumed to
	 * 							  have infinite capacity, each facility node is assumed to be directly connected to
	 * 							  every population node. There are no competitive facilities.
	 *
	 * @author Juno Jin, Nathan Situ
	 * @param graph The graph to solve the facility location problem on. Consists of facility nodes (facility placement
	 *              candidates) and population nodes (Where distances from facilities will be calcuated)
	 * @return The facility node with the best score, solving the facility location problem.
	 */
	public Node Solve(SimpleWeightedGraph<Node, DefaultWeightedEdge> graph){
		List<PopNode> popNodeList = new ArrayList<>();
		List<FacNode> facNodeList = new ArrayList<>();
		FacNode bestNode;
		Double bestScore = 0.0;

		//loop through all Facilities F what are open
		// add
		for (Node n:graph.vertexSet()) {
			if(n instanceof PopNode)
				popNodeList.add((PopNode)n);
			else facNodeList.add((FacNode)n);
		}
		bestNode = facNodeList.get(0);

		for (FacNode facNode:facNodeList) {
			Double score = 0.0;
			for (PopNode popNode:popNodeList) {
				score+=calculateScore(popNode,facNode);
			}
			if(score > bestScore){
				bestNode = facNode;
				bestScore = score;
			}
		}
		
		return (Node)bestNode;
	}

	private Double calculateScore(PopNode popNode, FacNode facNode){
		int population = popNode.getPopulation();

		Double distance = Math.sqrt(Math.pow((facNode.getX()-popNode.getX()), 2) +
				Math.pow((facNode.getY()-popNode.getY()), 2));

		return population/distance;
	}
}
