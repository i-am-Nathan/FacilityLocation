import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.openide.util.Lookup;
import org.openide.windows.IOSelect.AdditionalOperation;

import main.FacNode;
import main.Input.NodeListHolder;
import main.PopNode;
import main.Utility;

public class ReverseGreedyDistance {
	
	public static List<FacNode> Search(int facCount, NodeListHolder nodeListHolder){	

		// List of all the facility nodes
		List<FacNode>facNodes = nodeListHolder.getFacNodeList();
		List<PopNode> resNodes = nodeListHolder.getPopNodeList();
		
		HashMap<PopNode,HashMap<FacNode,Double>> popNodeDistances = computeDistances(facNodes,resNodes);
		
		double lowestWeight;
		double tempWeight = 0.0;
		FacNode removeNode = null;
		HashMap<PopNode,HashMap<FacNode,Double>> currentBestSet = null;
		while(facNodes.size()!=facCount){
			lowestWeight = Double.MAX_VALUE;
			for(FacNode facNode:facNodes){
				HashMap<PopNode,HashMap<FacNode,Double>> tempFacToRes = copyHashMap(popNodeDistances);
				
				for(PopNode resNode : tempFacToRes.keySet()){
					tempFacToRes.get(resNode).remove(facNode);
				}
				
				tempWeight = CalculateWeight(tempFacToRes,lowestWeight);
				if(tempWeight == -1.0){
					continue;
				} else if(tempWeight < lowestWeight){
					currentBestSet = new HashMap<PopNode,HashMap<FacNode,Double>>();
					currentBestSet.clear();
					for(PopNode node:tempFacToRes.keySet()){
						currentBestSet.put(node, tempFacToRes.get(node));
					}
					lowestWeight = tempWeight;
					removeNode = facNode;
				}
			}
			popNodeDistances.clear();
			for(PopNode resNode:currentBestSet.keySet()){
				popNodeDistances.put(resNode, currentBestSet.get(resNode));
			}
			facNodes.remove(removeNode);
			System.out.println("NEW WEIGHT : "+ lowestWeight);
		}
		
		return facNodes;
		
	}
	
	
	
	private static HashMap<PopNode, HashMap<FacNode, Double>> copyHashMap(
			HashMap<PopNode, HashMap<FacNode, Double>> popNodeDistances) {
		HashMap<PopNode, HashMap<FacNode, Double>> tempFacToRes = new HashMap<PopNode, HashMap<FacNode, Double>>();

		new HashMap<Node, HashMap<Node, Double>>();
		tempFacToRes.clear();
		for (PopNode node : popNodeDistances.keySet()) {
			HashMap<FacNode, Double> tempFacNodes = new HashMap<FacNode, Double>();
			for (FacNode tempNode : popNodeDistances.get(node).keySet()) {
				tempFacNodes.put(tempNode, popNodeDistances.get(node).get(tempNode));
			}
			tempFacToRes.put(node, tempFacNodes);
		}
		return tempFacToRes;
	}



	private static HashMap<PopNode, HashMap<FacNode,Double>> computeDistances(List<FacNode> facNodes, List<PopNode> resNodes) {
		HashMap<PopNode,HashMap<FacNode,Double>> popNodeDistances = new HashMap<PopNode,HashMap<FacNode,Double>>();

		for(PopNode resNode:resNodes){
			HashMap<FacNode,Double> facDistances = new HashMap<FacNode,Double>();
			for(FacNode facNode:facNodes){
				facDistances.put(facNode, Utility.euclidDistance(resNode, facNode));
			}

			popNodeDistances.put(resNode, facDistances);
		}
		return popNodeDistances;
	}
	
	private static double CalculateWeight(HashMap<PopNode, HashMap<FacNode, Double>> closestFacToResNodes,
			Double lowestWeight) {

		double facWeight = 0.0;
		// Get the weight of all facility nodes
		// Loops through ll residential nodes
		if (closestFacToResNodes.keySet() != null) {
			for (PopNode resNode : closestFacToResNodes.keySet()) {
				// gets a facility closest to res

				HashMap<FacNode, Double> map = closestFacToResNodes.get(resNode);
				for (FacNode facNode : map.keySet()) {
					facWeight = facWeight + (map.get(facNode).doubleValue() * resNode.getPopulationScore());

					if (facWeight > lowestWeight) {
						return -1.0;
					} else {
						break;
					}
				}
			}
		}

		if (facWeight == 0.0) {
			return -1.0;
		}

		return facWeight;
	}
}
