package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gephi.algorithms.shortestpath.AbstractShortestPathAlgorithm;
import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

import main.Input.NodeListHolder;

public class ReverseGreedy {
	public static HashMap<Node,Double> Search(int facCount,Graph wholeGraph){

		List<AbstractShortestPathAlgorithm> distanceGraph = new ArrayList<AbstractShortestPathAlgorithm>();

		List<Node> facNodes = new ArrayList<Node>();
		
		for(Node node : wholeGraph.getNodes()){
			String label = node.getLabel();
			String[] nodeLabels = label.split(";");
			if(nodeLabels[1].startsWith("Business")){
				facNodes.add(node);
			}
		}
		HashMap<Node,Double> facWeights = new HashMap<Node,Double>();
		
		for(int i = 0; i<facNodes.size();i++){
			DijkstraShortestPathAlgorithm dijkstraGraph = new DijkstraShortestPathAlgorithm(wholeGraph, facNodes.get(i));
			dijkstraGraph.compute();
			
			HashMap<Node,Double> distances = dijkstraGraph.getDistances();
			Double facilityWeight = 0.0;
			
			for(Node node : distances.keySet()){
				String[] nodeLabels = node.getLabel().split(";");
			
				if (nodeLabels[1].startsWith("Residential")){
	                double populationScore = CalculatePopulationScore(nodeLabels[2], Float.valueOf(nodeLabels[5]));
	                if(!distances.get(node).isInfinite()){
		                facilityWeight+=populationScore * distances.get(node);
	                }
				}
				
			}
			facWeights.put(facNodes.get(i), facilityWeight);
		}
		
		while(facWeights.keySet().size()!= facCount){
			Node removeFacNodeID = RGreed(facWeights);
			System.out.println("REMOVED: " + removeFacNodeID.getLabel());
			facWeights.remove(removeFacNodeID);
		}
		
		return facWeights;
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
