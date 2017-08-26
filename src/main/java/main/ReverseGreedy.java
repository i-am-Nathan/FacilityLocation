package main;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.openide.util.Lookup;

public class ReverseGreedy {
	@SuppressWarnings("unchecked")
	public static List<Node> Search(int facCount, Graph wholeGraph, boolean withEuclidDistance) {
		long startTime = System.currentTimeMillis();

		HashMap<Node, Double> facNodes = new HashMap<Node, Double>();
		List<Node> resNodes = new ArrayList<Node>();
	
		//Filter the wholeGraph into facNodes and resNodes
		for (Node node : wholeGraph.getNodes()) {
			String label = node.getLabel();
			String[] nodeLabels = label.split(";");
			if (nodeLabels[1].startsWith(Utility.FACILITY_NAME)) {
				facNodes.put(node, 0.0);
			} else if (nodeLabels[1].startsWith(Utility.RESIDENTIAL_NAME)) {
				resNodes.add(node);
			}
		}

		//Hashmap which represents <ResNode, HashMap<FacNode,DistanceFromFac>
		HashMap<Node, HashMap<Node, Double>> closestFacToResNodes = null;
		if(withEuclidDistance){
			closestFacToResNodes = computeEuclidDistance(resNodes,facNodes);
		}else {
			closestFacToResNodes = computeDijkstraDistances(wholeGraph,resNodes);
		}

		double lowestWeight;
		double tempWeight = 0.0;
		Node removeNode = null;
		HashMap<Node, HashMap<Node, Double>> currentBestSet = null;
		/**
		 * Loop through all the facNodes and remove one at a time and calculate weight
		 * until there is only facCount of facilities left
		 */
		while (facNodes.size() != facCount) {
			lowestWeight = Double.MAX_VALUE;
		
			for (Node facNode : facNodes.keySet()) {
				//Make a duplicate copy of closestFacToResNodes
				HashMap<Node, HashMap<Node, Double>> tempFacToRes = Utility.copyHashMap(closestFacToResNodes);

				for (Node resNode : tempFacToRes.keySet()) {
					tempFacToRes.get(resNode).remove(facNode);
				}

				// Calculate the weight of this set
				tempWeight = CalculateWeight(tempFacToRes, lowestWeight);
				if (tempWeight == -1.0) {
					continue;
				} else if (tempWeight < lowestWeight) {
					currentBestSet = new HashMap<Node, HashMap<Node, Double>>();
					currentBestSet.clear();
					for (Node node : tempFacToRes.keySet()) {
						currentBestSet.put(node, tempFacToRes.get(node));
					}
					lowestWeight = tempWeight;
					removeNode = facNode;
				}
			}

			closestFacToResNodes.clear();
			for (Node node : currentBestSet.keySet()) {
				closestFacToResNodes.put(node, currentBestSet.get(node));
			}
			facNodes.remove(removeNode);
			System.out.println("Nodes left: "+ facNodes.size());
		}
		List<Node> result = new ArrayList<Node>(facNodes.keySet());
		
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time taken for Reverse Greedy: " + totalTime/1000);

		return result;
	}

	private static HashMap<Node, HashMap<Node, Double>> computeEuclidDistance(List<Node> resNodes,
			HashMap<Node, Double> facNodes) {
		HashMap<Node, HashMap<Node,Double>> connectedFacToRes = new HashMap<Node,HashMap<Node,Double>>();
		for(Node resNode:resNodes){
			HashMap<Node,Double> facScores = new HashMap<Node,Double>();
			for(Node facNode:facNodes.keySet()){
				facScores.put(facNode, Utility.euclidDistance(facNode,resNode));
			}
			connectedFacToRes.put(resNode, facScores);
		}
		return connectedFacToRes;
	}

	private static HashMap<Node, HashMap<Node, Double>> computeDijkstraDistances(Graph wholeGraph,
			List<Node> resNodes) {
		HashMap<Node,HashMap<Node,Double>> closestFacToResNodes = new HashMap<Node,HashMap<Node,Double>>();

	
		for (Node node : resNodes) {
			//Using Dijkstra algorithm get the distances to all nodes relative the input node
			HashMap<Node, Double> distances = Utility.computeDistances(wholeGraph, node);
			HashMap<Node, Double> connectedFacs = new HashMap<Node, Double>();

			//Filter it so that the distances only get the faciliy distances
			for (Node connectedNode : distances.keySet()) {
				String label = connectedNode.getLabel();
				String[] nodeLabels = label.split(";");
				if (nodeLabels[1].startsWith(Utility.FACILITY_NAME)) {
					if (!distances.get(connectedNode).isInfinite()) {
						connectedFacs.put(connectedNode, distances.get(connectedNode));
					}
				}
			}

			// Sort the facility nodes by order. The first element is the
			// closest to the residential node.
			// Only add the residential nodes that are connected to the network.
			if (connectedFacs.size() != 0) {
				connectedFacs = Utility.sortByValues(connectedFacs);
				closestFacToResNodes.put(node, connectedFacs);
			}
		}
		return closestFacToResNodes;
	}

	private static double CalculateWeight(HashMap<Node, HashMap<Node, Double>> closestFacToResNodes,
			Double lowestWeight) {

		double facWeight = 0.0;

		if (closestFacToResNodes.keySet() != null) {
			//Loop through all the resNodes and find the facility that it is the closest to it
			for (Node resNode : closestFacToResNodes.keySet()) {
				HashMap<Node, Double> map = closestFacToResNodes.get(resNode);
				//Since the map is sorted the first element is the closest
				for (Node facNode : map.keySet()) {
					String[] nodeLabel = resNode.getLabel().split(";");
					double popScore = Utility.CalculatePopulationScore(nodeLabel[2], Float.valueOf(nodeLabel[5]));

					facWeight = facWeight + (map.get(facNode).doubleValue() * popScore);

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
