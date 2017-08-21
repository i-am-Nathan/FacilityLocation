

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import org.apache.xpath.operations.Mod;
import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.appearance.api.*;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.datalab.api.AttributeColumnsController;
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
import java.util.List;

public class Utility {
	
	public static String FACILITY_NAME = "Business";
	public static String RESIDENTIAL_NAME = "Residential";
	
	public static double euclidDistance(Node facNode, Node resNode) {
		double distance = Math.sqrt(Math.pow(facNode.x()- resNode.x(), 2) + Math.pow(facNode.y() - resNode.y(), 2));
		return distance;
	}

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

	static List<List<Node>> findInitialK(Graph graph, int k, float coverageThreshold, String centralityType) {
		List<List<Node>> nodeLists = new ArrayList<>();
		List<org.gephi.graph.api.Node> kNodeList = new ArrayList<>();

		FilterController filterController = Lookup.getDefault().lookup(FilterController.class);

		GiantComponentBuilder.GiantComponentFilter giantComponentFilter = new GiantComponentBuilder.GiantComponentFilter();
		giantComponentFilter.init(graph);
		Query query = filterController.createQuery(giantComponentFilter);
		GraphView graphView = filterController.filter(query);

		AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
		AppearanceModel appearanceModel = appearanceController.getModel();

		UndirectedGraph cleanGraph = graph.getModel().getUndirectedGraph(graphView);

		AttributeColumnsController attributeColumnsController = Lookup.getDefault()
				.lookup(AttributeColumnsController.class);
		Table edgeTable = cleanGraph.getModel().getEdgeTable();
		attributeColumnsController.copyColumnDataToOtherColumn(edgeTable, edgeTable.getColumn("Label"),
				edgeTable.getColumn("Weight"));

		Modularity modularity = new Modularity();
		modularity.setUseWeight(true);
		modularity.setRandom(true);

		float totalCoverage = 0;
		int resolution = 1;
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
			System.out.println(partition2.size() + " communities found");
			percentages = partition2.getSortedValues().toArray();

			for (int classIndex = 0; classIndex < k; classIndex++) {
				System.out.println("Community " + classIndex + ": " + partition2.percentage(percentages[classIndex]));
				totalCoverage += partition2.percentage(percentages[classIndex]);
			}
			resolution++;
			System.out.println("Total coverage over " + k + " communities: " + totalCoverage + "%\n");
		}

		System.out.println("#### Total coverage now over " + coverageThreshold + "%!\n\n");

		PartitionBuilder.PartitionFilter partitionFilter = new PartitionBuilder.NodePartitionFilter(classColumn,
				appearanceModel);
		for (int classIndex = 0; classIndex < k; classIndex++) {
			List<Node> communityNodeList = new ArrayList<>();
			partitionFilter.addPart(percentages[classIndex]);
			Query query1 = filterController.createQuery(partitionFilter);
			filterController.setSubQuery(query1, query);
			GraphView communityView = filterController.filter(query1);
			UndirectedGraph communityGraph = graph.getModel().getUndirectedGraph(communityView);

			EigenvectorCentrality eigenvectorCentrality = new EigenvectorCentrality();
			eigenvectorCentrality.execute(communityGraph);

			GraphDistance graphDistance = new GraphDistance();
			graphDistance.execute(communityGraph);
			double maxCentrality = 0;
			org.gephi.graph.api.Node maxCentralityNode = null;

			System.out.println("Nodes in community " + classIndex + ": " + communityGraph.getNodeCount());
			System.out.println("Edges in community " + classIndex + ": " + communityGraph.getEdgeCount());

			Column centralityColumn = null;

			for (org.gephi.graph.api.Node node : communityGraph.getNodes()) {
				communityNodeList.add(node);
				if (!node.getLabel().contains("Business"))
					continue;
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
			kNodeList.add(maxCentralityNode);
			nodeLists.add(communityNodeList);
			partitionFilter.removePart(percentages[classIndex]);
		}

		nodeLists.add(kNodeList);

		return nodeLists;

	}
	
	public static HashMap<Node, Double> computeDistances(Graph wholeGraph, Node node){
		DijkstraShortestPathAlgorithm dspa = new DijkstraShortestPathAlgorithm(wholeGraph, node);
		dspa.compute();
		return dspa.getDistances();
	}

	public static double CalculatePopulationScore(String zone, float area){
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
