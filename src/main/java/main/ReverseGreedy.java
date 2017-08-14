package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.gephi.algorithms.shortestpath.AbstractShortestPathAlgorithm;
import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

import main.Input.NodeListHolder;

public class ReverseGreedy {
	@SuppressWarnings("unchecked")
	public static HashMap<Node,Double> Search(int facCount,Graph wholeGraph){

		//List of all the facility nodes
		HashMap<Node, Double> facNodes = new HashMap<Node,Double>();
		List<Node> resNodes = new ArrayList<Node>();
		
		
		//Insert nodes into facNodes and resNodes list, sort them out
		for(Node node : wholeGraph.getNodes()){
			String label = node.getLabel();
			String[] nodeLabels = label.split(";");
			if(nodeLabels[1].startsWith("Business")){
				facNodes.put(node,0.0);
			} else if(nodeLabels[1].startsWith("Residential")){
				resNodes.add(node);
			}
		}
		
		//Find closest facility that the residential nodes can get to
		HashMap<Node,HashMap<Node,Double>> closestFacToResNodes = new HashMap<Node,HashMap<Node,Double>>();
		
		//Loops through all the residential nodes and creates a list of business nodes that it is closest to.
		for(Node node : resNodes){
			DijkstraShortestPathAlgorithm dijkstraGraph = new DijkstraShortestPathAlgorithm(wholeGraph,node);
			dijkstraGraph.compute();
			
			//distances of residential node with respect to all the other nodes INCLUDING itself?
			HashMap<Node, Double> distances = dijkstraGraph.getDistances();
			
			//Get all the facilities that the residential nodes are connected to, the double is the distance away from the facility from the node
			HashMap<Node, Double> connectedFacs = new HashMap<Node,Double>();
			
			//Filtering the distances hashmap to only contain facilities
			for(Node connectedNode: distances.keySet()){
				String label = connectedNode.getLabel();
				String[] nodeLabels = label.split(";");
				if(nodeLabels[1].startsWith("Business")){
					if(!distances.get(connectedNode).isInfinite()){
						connectedFacs.put(connectedNode,distances.get(connectedNode));
					}
				}
			}
			
			//Sort the facility nodes by order. The first element is the closest to the residential node.
			//Only add the residential nodes that are connected to the network.
			if(connectedFacs.size()!=0){
				connectedFacs = Utility.sortByValues(connectedFacs);
				closestFacToResNodes.put(node,connectedFacs);
			} 
		}

		
		
		double lowestWeight = Double.MAX_VALUE;
		double tempWeight = 0.0 ;
		while(facNodes.size()!=facCount){
			Node removeNode = null;
			lowestWeight = Double.MAX_VALUE;
			tempWeight = 0.0;
			HashMap<Node,HashMap<Node,Double>> currentBestSet = null;
			
			//Loop through all the facNodes and remove one at a time and check the weight of that one
			for(Node facNode:facNodes.keySet()){
				
				//For some reason these 2 does not reset
				HashMap<Node,HashMap<Node,Double>> tempFacToRes =  new HashMap<Node,HashMap<Node,Double>>();
				tempFacToRes.clear();
				for(Node node:closestFacToResNodes.keySet()){
					HashMap<Node,Double> tempFacNodes = new HashMap<Node,Double>();
					for(Node tempNode : closestFacToResNodes.get(node).keySet()){
						tempFacNodes.put(tempNode, closestFacToResNodes.get(node).get(tempNode));
					}
					tempFacToRes.put(node,tempFacNodes);
				}

				
				//Remove the currently selected facilities on all residential nodes.
				for(Node resNode:tempFacToRes.keySet()){
					tempFacToRes.get(resNode).remove(facNode);
				}
				
				//Calculate the weight of this set
				tempWeight = CalculateWeight(tempFacToRes,lowestWeight);
				if(tempWeight == -1.0){
					continue;
				}
				else if(tempWeight < lowestWeight){
					currentBestSet = new HashMap<Node,HashMap<Node,Double>>();
					currentBestSet.clear();
					for(Node node:tempFacToRes.keySet()){
						currentBestSet.put(node, tempFacToRes.get(node));
					}
					lowestWeight = tempWeight;
					removeNode = facNode;
				}
			}
			
			closestFacToResNodes.clear();
			for(Node node:currentBestSet.keySet()){
				closestFacToResNodes.put(node, currentBestSet.get(node));
			}
			facNodes.remove(removeNode);
			System.out.println(facNodes.size());
			if(facNodes.size()==4){
				System.out.println("WAIT PLS");
			}

			System.out.println("NEW WEIGHT: " + lowestWeight);
		
		}
		
		return facNodes; 
	}

	
	//TODO this algorithm for adding facweight needs to be fixed, reverse the for loops
	private static double CalculateWeight(HashMap<Node, HashMap<Node, Double>> closestFacToResNodes, Double lowestWeight) {
		
		double facWeight = 0.0;
		//Get the weight of all facility nodes
		//Loops through ll residential nodes
		if(closestFacToResNodes.keySet()!=null){
			for(Node resNode:closestFacToResNodes.keySet()){
				//gets a facility closest to res
				
				HashMap<Node,Double> map = closestFacToResNodes.get(resNode);
				for(Node facNode: map.keySet()){
					String[] nodeLabel = resNode.getLabel().split(";");
					double popScore = CalculatePopulationScore(nodeLabel[2],Float.valueOf(nodeLabel[5]));
					
					facWeight = facWeight + (map.get(facNode).doubleValue() * popScore);
					break;
				/*	if(facWeight > lowestWeight){
						return -1.0;
					} else {
						break;
					}*/
				}			
			}
		}

		if(facWeight==0.0){
			return -1.0;
		}
		
		return facWeight;
	}



	private static Node RGreed(HashMap<Node, Double> facWeights) {
		double lowestWeight = Double.MAX_VALUE;
		double currentWeight;
		Node removeNode = null;
		for(Node ignoredNode : facWeights.keySet()){
			currentWeight = 0.0;
			for(Node node : facWeights.keySet()){
				if(node != ignoredNode){
					currentWeight+=facWeights.get(node);
				}
				if(currentWeight < lowestWeight){
					lowestWeight = currentWeight;
					removeNode = ignoredNode;
				}
			}
		}
		return removeNode;
	}


	private static float CalculatePopulationScore(String zone, Float area) {
		int density;
	    switch (zone) {
	        case "1":
	            density = 1200;
	            break;
	        case "2A1":
	            density = 5000;
	            break;
	        case "2A":
	            density = 800;
	            break;
	        case "2B":
	        case "3C":
	            density = 600;
	            break;
	        case "2C":
	            density = 350;
	            break;
	        case "3A":
	        case "4A":
	        case "4B":
	            density = 450;
	            break;
	        case "3B":
	            density = 500;
	            break;
	        case "5":
	            density = 350;
	            break;
	        case "6A":
	        case "6A1":
	        case "6B":
	        case "6B1":
	        case "6C":
	        case "6C1":
	            density = 150;
	            break;
	        case "7":
	            density = 200;
	            break;
	        default:
	          	return 0;
	    }
	    return (area/density);

	}


}
