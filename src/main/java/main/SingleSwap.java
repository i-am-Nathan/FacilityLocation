package main;

import java.util.*;

import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.plugin.graph.GiantComponentBuilder;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.openide.util.Lookup;

public class SingleSwap {
	public List<Node> Search(Graph graph, int facCount, boolean useEuclidean, boolean useClustering){

		if(!useClustering){
			FilterController filterController = Lookup.getDefault().lookup(FilterController.class);

			GiantComponentBuilder.GiantComponentFilter giantComponentFilter = new GiantComponentBuilder.GiantComponentFilter();
			giantComponentFilter.init(graph);
			Query query = filterController.createQuery(giantComponentFilter);
			GraphView graphView = filterController.filter(query);
			graph = graph.getModel().getUndirectedGraph(graphView);
		}

		List<Node> swapNodes = new ArrayList<>();

		//Swap
		List<Node> facNodes = new ArrayList<>();
		HashMap<Node, HashMap<Node, Double>> resNodes = new HashMap<>();

		for(Node node : graph.getNodes()){
			if(node.getLabel().contains(Utility.FACILITY_NAME)){
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
			HashMap<Node, Double> distances = Utility.createDistanceMap(graph, swapNode, useEuclidean);

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

		while (oldScore != bestScore){
			oldScore = bestScore;
			for(Node swapInNode: facNodes){
				if(swapNodes.contains(swapInNode)) continue;

				HashMap<Node, Double> distances = Utility.createDistanceMap(graph, swapInNode, useEuclidean);

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
				}
			}
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

	public double calculateSetScore(HashMap<Node, HashMap<Node, Double>> distancesToFacs, double currentBestScore){
		double minimumDistance;
		double score = 0;

		for(Node resNode: distancesToFacs.keySet()){
			String[] nodeLabels = resNode.getLabel().split(";");
			minimumDistance = Collections.min(distancesToFacs.get(resNode).values());
			if(Double.isFinite(minimumDistance))
				score += Utility.calculatePopulationScore(nodeLabels[2], Float.valueOf(nodeLabels[5])) * minimumDistance;
			if(score > currentBestScore) break;
		}

		return score;
	}
}
