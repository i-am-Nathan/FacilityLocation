package main;

import org.gephi.graph.api.UndirectedGraph;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

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
	public List<FacNode> Search(UndirectedGraph graph, int businessNum, int facCount){
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		
		List<FacNode> facNodeList = new ArrayList<FacNode>();
		List<PopNode> popNodeList = new ArrayList<PopNode>();
		for(Node node : graph.getNodes()){
			if(isBusiness(node,businessNum)){
				facNodeList.add((FacNode) node);
			} 
			else if(isResidential(node)){
				popNodeList.add((PopNode) node);
			}
		}
		
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
		UndirectedGraph newGraph = graphModel.getUndirectedGraph();
		newGraph.addAllNodes((Collection<? extends Node>) facNodeList);
		newGraph.addAllNodes((Collection<? extends Node>) popNodeList);
		
		List<FacNode> desiredFacLocations = new ArrayList<FacNode>();
		
		if(facCount == 1){
			desiredFacLocations.add(Solve(facNodeList, popNodeList));
			return desiredFacLocations;
		}
		

		//=============GET INITIAL K========================
		//Sort the facilities in order by euclidean distance
		Collections.sort(facNodeList, new DistanceComparator());
		HashMap<Integer, FacNode> facNodeDistanceMap = new HashMap<Integer, FacNode>();
		
		facNodeDistanceMap.put(Integer.valueOf(0),facNodeList.get(0));
		
		int distanceFromBefore = 0;
		for(int i = 1; i<facNodeList.size();i++){
			distanceFromBefore += euclidDistance(facNodeList.get(i),facNodeList.get(i-1));
			facNodeDistanceMap.put(Integer.valueOf(distanceFromBefore), facNodeList.get(i));
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
		
		
		//get initial k facility nodes
		desiredFacLocations.add(facNodeList.get(0));
		desiredFacLocations.add(facNodeList.get(facNodeList.size()));
		facCount -=2;
		

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
	
	private int euclidDistance(FacNode fac1, FacNode fac2) {
		Double distance = Math.sqrt(Math.pow(fac2.xCoord-fac1.xCoord,2) + Math.pow(fac2.yCoord - fac1.yCoord, 2));
		return (int) Math.round(distance);
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
	
	private boolean isResidential(Node node){
		return node.getLabel().toLowerCase().contains(("Residential").toLowerCase());
	}

	private boolean isBusiness(Node node, int businessNum) {
		return node.getLabel().toLowerCase().contains(("Business " + businessNum).toLowerCase());
	}
}
