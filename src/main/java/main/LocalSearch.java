package main;

import org.gephi.graph.api.UndirectedGraph;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import main.Input.NodeListHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;

public class LocalSearch {
	@SuppressWarnings("unchecked")
	public static List<FacNode> Search(NodeListHolder graph, int facCount){

		
		List<FacNode> desiredFacLocations = new ArrayList<FacNode>();
		
		
		//=============GET INITIAL K========================
		if(facCount == 1){
			desiredFacLocations.add(Solve(graph.getFacNodeList(), graph.getPopNodeList()));
			return desiredFacLocations;
		}else if(facCount==2){
			graph.getFacNodeList().get(0).setVacancy(false);
			graph.getFacNodeList().get(graph.getFacNodeList().size()-1).setVacancy(false);
			
			desiredFacLocations.add(graph.getFacNodeList().get(0));
			desiredFacLocations.add(graph.getFacNodeList().get(graph.getFacNodeList().size()-1));

			facCount -=2;
		} else {
			//Sort the facilities in order by euclidean distance
			Collections.sort(graph.getFacNodeList(), new DistanceComparator());
			HashMap<Integer, FacNode> facNodeDistanceMap = new HashMap<Integer, FacNode>();
			
			facNodeDistanceMap.put(Integer.valueOf(0),graph.getFacNodeList().get(0));
			
			int distanceFromBefore = 0;
			for(int i = 1; i<graph.getFacNodeList().size();i++){
				distanceFromBefore += euclidDistance(graph.getFacNodeList().get(i),graph.getFacNodeList().get(i-1));
				facNodeDistanceMap.put(Integer.valueOf(distanceFromBefore), graph.getFacNodeList().get(i));
			}
			
			int dividedDistance = distanceFromBefore/facCount;
	
			int decreasingChecker = Integer.MAX_VALUE;
			int checker = Integer.MAX_VALUE;
			int distanceMultiplier = 1;
			boolean endLoop = false;
			Integer currentlySelectedKey = null;
			
			for(Integer distance: facNodeDistanceMap.keySet()){
				if(endLoop){
					if(distance == distanceFromBefore){
						facNodeDistanceMap.get(distance).setVacancy(false);
						desiredFacLocations.add(facNodeDistanceMap.get(distance));					
						break;
					}
					else {
						continue;
					}
				}
				
				//The checker makes sure it gets the facility node that is the closest to the divided value;
				decreasingChecker = distance - dividedDistance;
				if(decreasingChecker < checker){
					checker = decreasingChecker;
					currentlySelectedKey = distance;
				} else if(decreasingChecker > checker){
					facNodeDistanceMap.get(currentlySelectedKey).setVacancy(false);
					desiredFacLocations.add(facNodeDistanceMap.get(currentlySelectedKey));
					distanceMultiplier++;
					dividedDistance = distanceFromBefore*distanceMultiplier/facCount;
					
					if(dividedDistance == distanceFromBefore){
						endLoop = true;
					}
					//update it so it does not skip this distance when it is updated
					currentlySelectedKey = distance;
					decreasingChecker = distance - dividedDistance;
					checker = decreasingChecker;
				}
			}
		}
		

		//===================================================
		
		//Get initial score of the facilities
		Double currentScore = calculateAccumulateScore(desiredFacLocations, graph.getPopNodeList(),Double.MAX_VALUE);

		//find swaps for each of them, after this loop the best score should be given
		FacNode swappedOutNode;
		Double tempScore;
		
		for(int i = 0; i < desiredFacLocations.size(); i++){
			tempScore = 0.0;
			swappedOutNode = desiredFacLocations.get(i);
			for(FacNode tempFac: graph.getFacNodeList()){
				if(tempFac.getVacancy()==true){
					desiredFacLocations.set(i,tempFac);
					tempScore = calculateAccumulateScore(desiredFacLocations,graph.getPopNodeList(),currentScore);
					
					//If the accumulated score is smaller replace it else swap it back to the original one.
					if (tempScore<currentScore){
						currentScore = tempScore;
						swappedOutNode = tempFac;
					} 
				}
			}
			desiredFacLocations.set(i,swappedOutNode);
			System.out.printf("Current Lowest Score: %f\n", currentScore);
		}
		System.out.println("ALGORITHM COMPLETE");
		return desiredFacLocations;
	}
	
	public static FacNode Solve(List<FacNode> facNodeList, List<PopNode> popNodeList){
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
	
	private static int euclidDistance(FacNode fac1, FacNode fac2) {
		Double distance = Math.sqrt(Math.pow(fac2.xCoord-fac1.xCoord,2) + Math.pow(fac2.yCoord - fac1.yCoord, 2));
		return (int) Math.round(distance);
	}
	
	private static Double calculateAccumulateScore(List<FacNode> facLocations, List<PopNode> popNodeList, Double bound){
		Double currentScore = 0.0;
		for(FacNode facNode : facLocations){
			for (PopNode popNode:popNodeList) {
				currentScore+=calculateScore(popNode,facNode);
				if(currentScore > bound){
					return Double.MAX_VALUE;
				}
			}
		}
		return currentScore;
	}

	private static Double calculateScore(PopNode popNode, FacNode facNode){
		float populationScore = popNode.getPopulationScore();

		Double distance = Math.sqrt(Math.pow((facNode.getX()-popNode.getX()), 2) +
				Math.pow((facNode.getY()-popNode.getY()), 2));

		return populationScore * distance;
	}
}
