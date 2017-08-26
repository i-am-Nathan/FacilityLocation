package main;

import java.util.*;

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
	public List<Node> Search(UndirectedGraph graph, int facCount, boolean useEuclidean, boolean useClustering){
		List<Node> swapNodes = new ArrayList<>();

		//Swap
		List<Node> facNodes = new ArrayList<>();
		HashMap<Node, HashMap<Node, Double>> resNodes = new HashMap<>();

		for(Node node : graph.getNodes()){
			if(node.getLabel().contains("Business")){
				facNodes.add(node);
			}
		}
		//Initial K
		if(useClustering){
			List<List<Node>> nodeLists = Utility.findInitialK(graph, facCount, 75, "Eigenvector Centrality");
			swapNodes = nodeLists.get(nodeLists.size()-1);
		} else {
			Random rand = new Random();
			for(int index = 0; index < facCount; index++){
				swapNodes.add(facNodes.get(rand.nextInt(facNodes.size()-1)));
			}
		}

		for (Node swapNode: swapNodes){
			HashMap<Node, Double> distances;
			if(useEuclidean) distances = Utility.createEuclideanSet(graph, swapNode);
			else distances = Utility.computeDistances(graph, swapNode);

			for(Node targetNode: distances.keySet()){
				if(targetNode.getLabel().contains(Utility.RESIDENTIAL_NAME)){
					HashMap<Node, Double> currentFacDist = resNodes.getOrDefault(targetNode, new HashMap<>());
					currentFacDist.put(swapNode, distances.get(targetNode));
					resNodes.put(targetNode, currentFacDist);
				}
			}
		}

		double bestScore = calculateSetScore(resNodes, Double.MAX_VALUE);
		double oldScore = 0;

		long startTime = System.currentTimeMillis();
		while (oldScore != bestScore){
			oldScore = bestScore;
			for(Node swapInNode: facNodes){
				if(swapNodes.contains(swapInNode)) continue;

				HashMap<Node, Double> distances;
				if(useEuclidean) distances = Utility.createEuclideanSet(graph, swapInNode);
				else distances = Utility.computeDistances(graph, swapInNode);

				SetScore bestScoreSet = null;

				for(Node swapOutNode: swapNodes){
					HashMap<Node, HashMap<Node, Double>> tempDistances = Utility.copyHashMap(resNodes);
					for(Node targetNode: tempDistances.keySet()){
						HashMap<Node, Double> facilityDistance = tempDistances.getOrDefault(targetNode, new HashMap<>());
						facilityDistance.put(swapInNode, distances.get(targetNode));
						facilityDistance.remove(swapOutNode);
					}
					double tempScore = calculateSetScore(tempDistances, bestScore);

					if(tempScore < bestScore){
						bestScore = tempScore;
						bestScoreSet = new SetScore(swapInNode, swapOutNode, bestScore, tempDistances);
					}
				}
				if(bestScoreSet != null){
					swapNodes.remove(bestScoreSet.swapOutNode);
					swapNodes.add(bestScoreSet.swapInNode);
					resNodes = bestScoreSet.resultingSet;
//					System.out.printf("Best Score: %f\n",bestScore);
//					System.out.println("\n####");
				}
			}
//			System.out.println("\n### The best score for this round is: "+bestScore+"\n");
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Time: "+(endTime-startTime));


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

	public double calculateSetScore(HashMap<Node, HashMap<Node, Double>> distancesToFacs, double currentBestScore){
		double minimumDistance;
		double score = 0;

		for(Node resNode: distancesToFacs.keySet()){
			String[] nodeLabels = resNode.getLabel().split(";");
			minimumDistance = Collections.min(distancesToFacs.get(resNode).values());
			if(Double.isFinite(minimumDistance))
				score += Utility.CalculatePopulationScore(nodeLabels[2], Float.valueOf(nodeLabels[5])) * minimumDistance;
			if(score > currentBestScore) return Double.MAX_VALUE;
		}



		return score;
	}
}

