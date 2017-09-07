package main;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.appearance.api.*;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.plugin.graph.GiantComponentBuilder;
import org.gephi.filters.plugin.partition.PartitionBuilder;
import org.gephi.graph.api.*;
import org.gephi.statistics.plugin.EigenvectorCentrality;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.plugin.Modularity;
import org.openide.util.Lookup;
import java.util.ArrayList;

/**
 * Utility class for calling common methods
 */
public class Utility {
	
	public static String FACILITY_NAME = "Business";
	public static String RESIDENTIAL_NAME = "Residential";
	public static String XY_FILE = "src\\main\\java\\main\\YX.csv";
	
	//Euclid distance between 2 nodes
	public static double euclidDistance(Node facNode, Node resNode) {
		double distance = Math.sqrt(Math.pow(facNode.x()- resNode.x(), 2) + Math.pow(facNode.y() - resNode.y(), 2));
		return distance;
	}

	//Sort hashmap by the values
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static HashMap sortByValues(HashMap map) {
		List list = new LinkedList(map.entrySet());

		Collections.sort(list, new Comparator() {

			@Override
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}

		});

		HashMap sortedHashMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}

		return sortedHashMap;

	}

	static List<Subgraph> findModularityClasses(Graph graph, int k, float coverageThreshold){
		List<Subgraph> communitySubgraphs = new ArrayList<>();

		FilterController filterController = Lookup.getDefault().lookup(FilterController.class);

		GiantComponentBuilder.GiantComponentFilter giantComponentFilter = new GiantComponentBuilder.GiantComponentFilter();
		giantComponentFilter.init(graph);
		Query query = filterController.createQuery(giantComponentFilter);
		GraphView graphView = filterController.filter(query);

		AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
		AppearanceModel appearanceModel = appearanceController.getModel();

		UndirectedGraph cleanGraph = graph.getModel().getUndirectedGraph(graphView);

		Modularity modularity = new Modularity();
		modularity.setUseWeight(true);
		modularity.setRandom(true);

		float totalCoverage = 0;
		double resolution = 1;
		Column classColumn = null;
		Object[] percentages = null;

		while (totalCoverage < coverageThreshold) {
			totalCoverage = 0;
			modularity.setResolution(resolution);
			modularity.execute(cleanGraph);

			classColumn = cleanGraph.getModel().getNodeTable().getColumn(Modularity.MODULARITY_CLASS);

			Function func2 = appearanceModel.getNodeFunction(graph, classColumn,
					PartitionElementColorTransformer.class);
			Partition partition2 = ((PartitionFunction) func2).getPartition();

			while (k > partition2.size()){
				resolution /= 2;
				modularity.setResolution(resolution);
				modularity.execute(cleanGraph);

				classColumn = cleanGraph.getModel().getNodeTable().getColumn(Modularity.MODULARITY_CLASS);

				func2 = appearanceModel.getNodeFunction(graph, classColumn,
						PartitionElementColorTransformer.class);
				partition2 = ((PartitionFunction) func2).getPartition();
				System.out.println(partition2.size());

			}

			System.out.println(partition2.size() + " communities found");
			percentages = partition2.getSortedValues().toArray();

			for (int classIndex = 0; classIndex < k; classIndex++) {
				System.out.println("Community " + classIndex + ": " + partition2.percentage(percentages[classIndex]));
				totalCoverage += partition2.percentage(percentages[classIndex]);
			}
			resolution++;
			System.out.println("Total coverage over " + k + " communities: " + totalCoverage + "%\n");
		}

		PartitionBuilder.PartitionFilter partitionFilter = new PartitionBuilder.NodePartitionFilter(classColumn,
				appearanceModel);
		for (int classIndex = 0; classIndex < k; classIndex++) {
			partitionFilter.addPart(percentages[classIndex]);
			Query query1 = filterController.createQuery(partitionFilter);
			filterController.setSubQuery(query1, query);
			GraphView communityView = filterController.filter(query1);
			communitySubgraphs.add(graph.getModel().getGraph(communityView));
			partitionFilter.removePart(percentages[classIndex]);
		}


		return communitySubgraphs;
	}

	//Find k facilities based on the clustering and centrality of the node population
	static List<List<Node>> findInitialK(Graph graph, int k, float coverageThreshold, String centralityType) {
		List<List<Node>> nodeLists = new ArrayList<>();
		List<Node> kNodeList = new ArrayList<>();
		List<Subgraph> communitySubgraphs = findModularityClasses(graph, k, coverageThreshold);

		for(Subgraph sg: communitySubgraphs){
		    System.out.println(sg.getNodeCount());
        }

		for(Subgraph sg: communitySubgraphs){
			EigenvectorCentrality eigenvectorCentrality = new EigenvectorCentrality();
			eigenvectorCentrality.execute(sg);

			GraphDistance graphDistance = new GraphDistance();
			graphDistance.execute(sg);
			double maxCentrality = 0;
			org.gephi.graph.api.Node maxCentralityNode = null;

//			System.out.println("Nodes in community " + classIndex + ": " + communityGraph.getNodeCount());
//			System.out.println("Edges in community " + classIndex + ": " + communityGraph.getEdgeCount());

			Column centralityColumn = null;

			for (org.gephi.graph.api.Node node : sg.getNodes()) {
				if (node.getLabel().contains(FACILITY_NAME)) {
					for (Column col : node.getAttributeColumns()) {
						if (col.getTitle().equals(centralityType)) {
							centralityColumn = col;
							break;
						}
					}
					double nodeCentrality = (double) node.getAttribute(centralityColumn);
					if (nodeCentrality > maxCentrality) {
						maxCentralityNode = node;
						maxCentrality = nodeCentrality;
					}
				}
			}
			kNodeList.add(maxCentralityNode);
			Iterable<Node> communityIterable = sg.getNodes();
			List<Node> communityNodes = new ArrayList<>();
			communityIterable.forEach(communityNodes::add);
			nodeLists.add(communityNodes);
		}

		nodeLists.add(kNodeList);

		return nodeLists;
	}
	
	//Uses dijkstra algorithm and finds the distance of the whole graph relative to the node
	public static HashMap<Node, Double> createNetworkSet(Graph graph, Node node){
		DijkstraShortestPathAlgorithm dspa = new DijkstraShortestPathAlgorithm(graph, node);
		dspa.compute();
		return dspa.getDistances();
	}

	private static HashMap<Node, Double> createEuclideanSet(Graph graph, Node node){
		HashMap<Node, Double> euclideanSet = new HashMap<>();
		for(Node n: graph.getNodes()){
			euclideanSet.put(n, Utility.euclidDistance(node, n));
		}
		return euclideanSet;
	}

	public static HashMap<Node, Double> createDistanceMap(Graph graph, Node node, boolean useEuclidean){
		if(useEuclidean) return createEuclideanSet(graph, node);
		else return createNetworkSet(graph, node);

	}

	public static HashMap<Node, HashMap<Node, Double>> copyHashMap(
			HashMap<Node, HashMap<Node, Double>> closestFacToResNodes) {
		HashMap<Node, HashMap<Node, Double>> tempFacToRes = new HashMap<Node, HashMap<Node, Double>>();

		new HashMap<Node, HashMap<Node, Double>>();
		tempFacToRes.clear();
		for (Node node : closestFacToResNodes.keySet()) {
			HashMap<Node, Double> tempFacNodes = new HashMap<Node, Double>();
			for (Node tempNode : closestFacToResNodes.get(node).keySet()) {
				tempFacNodes.put(tempNode, closestFacToResNodes.get(node).get(tempNode));
			}
			tempFacToRes.put(node, tempFacNodes);
		}
		return tempFacToRes;
	}

	public static double calculatePopulationScore(String zone, float area){
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

	public static double calculateFinalScore(Graph wholeGraph, List<Node> nodeList, boolean useEuclidean){
		double score = 0;
		HashMap<Node, HashMap<Node, Double>> distancesToResNodes = new HashMap<>();
		for(Node n: nodeList){
			HashMap<Node, Double> distances = createDistanceMap(wholeGraph, n, useEuclidean);
			for(Node targetNode: distances.keySet()){
				String[] targetNodeLabels = targetNode.getLabel().split(";");
				if(targetNodeLabels[1].startsWith(RESIDENTIAL_NAME)){
					HashMap<Node, Double> currentFacDist = distancesToResNodes.get(targetNode);
					if(currentFacDist == null) currentFacDist = new HashMap<>();
					currentFacDist.put(n, distances.get(targetNode));
					distancesToResNodes.put(targetNode, currentFacDist);
				}
			}
		}
		double minimumDistance;
		for(Node resNode: distancesToResNodes.keySet()){
			String[] nodeLabels = resNode.getLabel().split(";");
			minimumDistance = Collections.min(distancesToResNodes.get(resNode).values());
			if(Double.isFinite(minimumDistance))
				score += Utility.calculatePopulationScore(nodeLabels[2], Float.valueOf(nodeLabels[5])) * minimumDistance;
		}
		return score;
	}
}
