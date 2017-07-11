package main;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

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
	

	
	public FacNode Solve(List<FacNode> facNodeList, List<PopNode> popNodeList){
		FacNode bestNode;
		Double bestScore = Double.MAX_VALUE;

		//Set current node
		bestNode = facNodeList.get(0);

		for (FacNode facNode:facNodeList) {
			Double score = 0.0;
			for (PopNode popNode:popNodeList) {
				score+=calculateScore(popNode,facNode);
			}
			if(score < bestScore){
				bestNode = facNode;
				bestScore = score;
			}
		}
		
		return bestNode;
	}

	public List<FacNode> LocalSearch(List<FacNode> facNodeList, List<PopNode> popNodeList, int facNum){
		List<FacNode> desiredFacLocations = new ArrayList<FacNode>();
		
		
		if(facNum == 1){
			desiredFacLocations.add(Solve(facNodeList, popNodeList));
			return desiredFacLocations;
		}

		//=============GET INITIAL K========================
		//Sort the facilities in order by euclidean distance
		Collections.sort(facNodeList, new DistanceComparator());
		
		//get initial k facility nodes
		desiredFacLocations.add(facNodeList.get(0));
		desiredFacLocations.add(facNodeList.get(facNodeList.size()));
		facNum -=2;
		

		//===================================================
	
		//Get initial score of the facilities
		Double currentScore = calculateAccumulateScore(desiredFacLocations, popNodeList);
		
		//find swaps for each of them, after this loop the best score should be given
		for(int i = 0; i < desiredFacLocations.size(); i++){
			Double tempScore = 0.0;
			FacNode swappedOutNode;
			for(FacNode tempFac: facNodeList){
				if(tempFac.getVacancy()==false){
					swappedOutNode = desiredFacLocations.get(i);
					desiredFacLocations.add(i,tempFac);
					tempScore = calculateAccumulateScore(desiredFacLocations,popNodeList);
					if (tempScore<currentScore){
						currentScore = tempScore;
					} else {
						desiredFacLocations.add(i,swappedOutNode);
					}
				}
			}
		}

		return desiredFacLocations;
		
	}
	
	private Double calculateAccumulateScore(List<FacNode> facLocations, List<PopNode> popNodeList){
		Double currentScore = 0.0;
		for(FacNode facNode : facLocations){
			for (PopNode popNode:popNodeList) {
				currentScore+=calculateScore(popNode,facNode);
			}
		}
		return currentScore;
	}
	
	
	private Double calculateScore(PopNode popNode, FacNode facNode){
		int population = popNode.getPopulation();

		Double distance = Math.sqrt(Math.pow((facNode.getX()-popNode.getX()), 2) +
				Math.pow((facNode.getY()-popNode.getY()), 2));

		return population * distance;
	}
	
}

class DistanceComparator implements Comparator<FacNode>{

	@Override
	public int compare(FacNode fac1, FacNode fac2) {
		return Utility.euclidDistance(fac1,fac2);
	}


	
}

class Utility{
	static int euclidDistance(FacNode fac1, FacNode fac2) {
		Double distance = Math.sqrt(Math.pow(fac2.xCoord-fac1.xCoord,2) + Math.pow(fac2.yCoord - fac1.yCoord, 2));
		return (int) Math.round(distance);
	}
}
