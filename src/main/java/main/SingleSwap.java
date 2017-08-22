package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.mysql.jdbc.Util;
import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.UndirectedGraph;
import org.openide.util.Lookup;

public class SingleSwap {
	public List<Node> Search(UndirectedGraph graph, int facCount){
		
		//Initial K
		List<List<Node>> nodeLists = Utility.findInitialK(graph, facCount, 75, "Eigenvector Centrality");
		List<Node> swapNodes = nodeLists.get(nodeLists.size()-1);

		//Swap
		List<Node> facNodes = new ArrayList<>();
		HashMap<Node, HashMap<Node, Double>> resNodes = new HashMap<>();

		for(Node node : graph.getNodes()){
			if(node.getLabel().contains("Business")){
				facNodes.add(node);
			}
		}

		AttributeColumnsController attributeColumnsController = Lookup.getDefault().lookup(AttributeColumnsController.class);
		Table edgeTable = graph.getModel().getEdgeTable();
		attributeColumnsController.copyColumnDataToOtherColumn(edgeTable, edgeTable.getColumn("Label"), edgeTable.getColumn("Weight"));

		for (Node n: swapNodes){
			HashMap<Node, Double> distances = Utility.computeDistances(graph, n);

			for(Node targetNode: distances.keySet()){
				if(targetNode.getLabel().contains("Residential")){
					HashMap<Node, Double> currentFacDist = resNodes.get(targetNode);
					if(currentFacDist == null) currentFacDist = new HashMap<>();
					currentFacDist.put(n, distances.get(targetNode));
					resNodes.put(targetNode, currentFacDist);
				}
			}
		}

		double bestScore = calculateSetScore(resNodes);
		double oldScore = 0;

		while (oldScore != bestScore){
			oldScore = bestScore;
			for(Node swapInNode: facNodes){
				if(swapNodes.contains(swapInNode)) {
					continue;
				}

				HashMap<Node, Double> distances = Utility.computeDistances(graph, swapInNode);
				HashMap<Node, Double> replacedSetScore = new HashMap<>();
				List<SetScore> betterScores = new ArrayList<>();
				for(Node swapOutNode: swapNodes){
					HashMap<Node, HashMap<Node, Double>> tempDistances = Utility.copyHashMap(resNodes);
					for(Node targetNode: tempDistances.keySet()){
						HashMap<Node, Double> facilityDistance = tempDistances.getOrDefault(targetNode, new HashMap<>());
						facilityDistance.put(swapInNode, distances.get(targetNode));
						facilityDistance.remove(swapOutNode);
					}
					double tempScore = calculateSetScore(tempDistances);

					if(tempScore < bestScore){
						SetScore betterScore = new SetScore(swapInNode, swapOutNode, tempScore, tempDistances);
						betterScores.add(betterScore);
					}
				}
				if(betterScores.size() > 0){
					SetScore bestSet = null;
					for(SetScore set: betterScores){
						if(set.score < bestScore) {
							bestScore = set.score;
							bestSet = set;
						}
					}
					swapNodes.remove(bestSet.swapOutNode);
					swapNodes.add(bestSet.swapInNode);
					resNodes = bestSet.resultingSet;
				}
			}
			System.out.println("\n### The best score for this round is: "+bestScore+"\n");
		}

		List<Node> selectedNodes = new ArrayList<>(swapNodes);
		for(Node n: selectedNodes){
			System.out.println("Node id "+n.getId().toString()+" "+n.getLabel());
		}
		return selectedNodes;

	}

	class SetScore{
		Node swapInNode;
		Node swapOutNode;
		double score;
		HashMap<Node, HashMap<Node, Double>> resultingSet = new HashMap<>();

		public SetScore(Node swapInNode, Node swapOutNode, double score, HashMap<Node, HashMap<Node, Double>> resultingSet) {
			this.swapInNode = swapInNode;
			this.swapOutNode = swapOutNode;
			this.score = score;
			this.resultingSet = resultingSet;
		}
	}

	public double calculateSetScore(HashMap<Node, HashMap<Node, Double>> distancesToFacs){
		double minimumDistance;
		double score = 0;

		for(Node resNode: distancesToFacs.keySet()){
			String[] nodeLabels = resNode.getLabel().split(";");
			minimumDistance = Collections.min(distancesToFacs.get(resNode).values());
			if(Double.isFinite(minimumDistance))
				score += Utility.CalculatePopulationScore(nodeLabels[2], Float.valueOf(nodeLabels[5])) * minimumDistance;
		}

		return score;
	}
}

