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

/**
* The single swap algorithm works by first randomly selecting K facility locations to open, than for each opened facility
* temporarily swap it to another facility location to open. Swap the one that reduces the cost the most. Do this for each unvisited 
* facility node or when no swaps that reduces the cost are left. We also have an alternate version which uses clustering for finding 
* the initial K facility nodes, however it is somewhat slower and less optimal than the existing one.
*/
public class SingleSwap {
	public List<Node> Search(Graph graph, int facCount, boolean useEuclidean, boolean useClustering){
		
		//If this run does not use clustering than use the largest connected network, meaning small unconnected nodes are ignored.
		if(!useClustering){
			FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
			GiantComponentBuilder.GiantComponentFilter giantComponentFilter = new GiantComponentBuilder.GiantComponentFilter();
			giantComponentFilter.init(graph);
			Query query = filterController.createQuery(giantComponentFilter);
			GraphView graphView = filterController.filter(query);
			graph = graph.getModel().getUndirectedGraph(graphView);
		}

		List<Node> swapNodes = new ArrayList<>();

		List<Node> facNodes = new ArrayList<>();
		HashMap<Node, HashMap<Node, Double>> resNodes = new HashMap<>();
		
		//Add all facility nodes into facNodes
		for(Node node : graph.getNodes()){
			if(node.getLabel().contains(Utility.FACILITY_NAME)){
				facNodes.add(node);
			}
		}
		//If this run uses clustering than cluster the network into K communities else randomly select K facilities.
		if(useClustering){
			List<List<Node>> nodeLists = Utility.findInitialK(graph, facCount, 75, "Eigenvector Centrality");
			swapNodes = nodeLists.get(nodeLists.size()-1);
		} else {
			Random rand = new Random();
			for(int index = 0; index < facCount; index++){
				swapNodes.add(facNodes.get(rand.nextInt(facNodes.size()-1)));
			}
		}
		
		//Add all the residential nodes and its distances to the opened facilities to resNodes
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
			
			//Loop through each unselected node to swap into the intial K
			for(Node swapInNode: facNodes){
				if(swapNodes.contains(swapInNode)) continue;

				HashMap<Node, Double> distances = Utility.createDistanceMap(graph, swapInNode, useEuclidean);

				SetScore bestScoreSet = null;
				
				//Swap our one the initial K facility nodes once at a time and calculate the new cost for the set.
				for(Node swapOutNode: swapNodes){
					HashMap<Node, HashMap<Node, Double>> tempDistances = Utility.copyHashMap(resNodes);
					for(Node targetNode: tempDistances.keySet()){
						HashMap<Node, Double> facilityDistance = tempDistances.getOrDefault(targetNode, new HashMap<>());
						facilityDistance.put(swapInNode, distances.get(targetNode));
						facilityDistance.remove(swapOutNode);
					}
					double tempScore = calculateSetScore(tempDistances, bestScore);

					//If the new set is better than update the current best set
					if(tempScore < bestScore){
						bestScore = tempScore;
						bestScoreSet = new SetScore(swapInNode, swapOutNode, bestScore, tempDistances);
					}
				}
				//update the bestScore set to the best set
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
	
	// A class which holds the node that is swapped out and in, the current score and the resulting set of facility locations
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

	//Calculate the score of the current set of chosen facilities
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
