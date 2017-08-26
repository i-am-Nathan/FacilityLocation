package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.mysql.jdbc.Util;
import it.unimi.dsi.fastutil.Hash;
import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.UndirectedGraph;
import org.openide.util.Lookup;

public class SingleSwap {
	public List<Node> Search(UndirectedGraph graph, int facCount, boolean useEuclidean){

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


		for (Node swapNode: swapNodes){
			HashMap<Node, Double> distances;
			if(useEuclidean) distances = createEuclideanSet(graph, swapNode);
			else distances = Utility.computeDistances(graph, swapNode);

			for(Node targetNode: distances.keySet()){
				if(targetNode.getLabel().contains(Utility.RESIDENTIAL_NAME)){
					HashMap<Node, Double> currentFacDist = resNodes.getOrDefault(targetNode, new HashMap<>());
					currentFacDist.put(swapNode, distances.get(targetNode));
					resNodes.put(targetNode, currentFacDist);
				}
			}
		}

		double bestScore = calculateSetScore(resNodes);
		double oldScore = 0;

		while (oldScore != bestScore){
			oldScore = bestScore;
			for(Node swapInNode: facNodes){
				if(swapNodes.contains(swapInNode)) continue;

				HashMap<Node, Double> distances;
				if(useEuclidean) distances = createEuclideanSet(graph, swapInNode);
				else distances = Utility.computeDistances(graph, swapInNode);

				SetScore bestScoreSet = null;

				for(Node swapOutNode: swapNodes){
					HashMap<Node, HashMap<Node, Double>> tempDistances = Utility.copyHashMap(resNodes);
					for(Node targetNode: tempDistances.keySet()){
						HashMap<Node, Double> facilityDistance = tempDistances.getOrDefault(targetNode, new HashMap<>());
						facilityDistance.put(swapInNode, distances.get(targetNode));
						facilityDistance.remove(swapOutNode);
					}
					double tempScore = calculateSetScore(tempDistances);

					if(tempScore < bestScore){
						bestScore = tempScore;
						bestScoreSet = new SetScore(swapInNode, swapOutNode, bestScore, tempDistances);
					}
				}
				if(bestScoreSet != null){
					swapNodes.remove(bestScoreSet.swapOutNode);
					swapNodes.add(bestScoreSet.swapInNode);
					resNodes = bestScoreSet.resultingSet;
				}
				System.out.printf("Best Score: %f\n",bestScore);
				System.out.println("\n####");
			}
//			System.out.println("\n### The best score for this round is: "+bestScore+"\n");
		}

		List<Node> selectedNodes = new ArrayList<>(swapNodes);


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

	private HashMap<Node, Double> createEuclideanSet(Graph graph, Node swapNode){
		HashMap<Node, Double> euclideanSet = new HashMap<>();
		for(Node n: graph.getNodes()){
			euclideanSet.put(n, Utility.euclidDistance(swapNode, n));
		}
		return euclideanSet;
	}
}

