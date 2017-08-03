package main;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import org.apache.xpath.operations.Mod;
import org.gephi.appearance.api.*;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.plugin.graph.GiantComponentBuilder;
import org.gephi.filters.plugin.partition.PartitionBuilder;
import org.gephi.graph.api.*;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.EigenvectorCentrality;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.plugin.Modularity;
import org.gephi.statistics.plugin.builder.ConnectedComponentsBuilder;
import org.gephi.statistics.plugin.builder.ModularityBuilder;
import org.gephi.statistics.spi.Statistics;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;

public class Utility {
	static int euclidDistance(FacNode fac1, FacNode fac2) {
		Double distance = Math.sqrt(Math.pow(fac2.xCoord-fac1.xCoord,2) + Math.pow(fac2.yCoord - fac1.yCoord, 2));
		return (int) Math.round(distance);
	}

	static List<Node> findInitialK(Graph graph, int k){
	    List<org.gephi.graph.api.Node> kNodeList = new ArrayList<>();

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
		int resolution = 1;
        Column classColumn = null;
        Object[] percentages = null;

		while(totalCoverage<75){
		    totalCoverage = 0;
			modularity.setResolution(resolution);
			modularity.execute(cleanGraph);

			classColumn = cleanGraph.getModel().getNodeTable().getColumn(Modularity.MODULARITY_CLASS);
			Function func2 = appearanceModel.getNodeFunction(graph, classColumn, PartitionElementColorTransformer.class);
			Partition partition2 = ((PartitionFunction) func2).getPartition();
			System.out.println(partition2.size() + " communities found");
			percentages = partition2.getSortedValues().toArray();

			for(int classIndex = 0; classIndex < k; classIndex++){
			    System.out.println("Community "+classIndex+": "+partition2.percentage(percentages[classIndex]));
			    totalCoverage += partition2.percentage(percentages[classIndex]);
            }
			resolution++;
            System.out.println("Total coverage over " + k + " communities: " + totalCoverage + "%\n");
		}

		System.out.println("####Total coverage now over 60%!\n\n");

        PartitionBuilder.PartitionFilter partitionFilter = new PartitionBuilder.NodePartitionFilter(classColumn, appearanceModel);
        for(int classIndex = 0; classIndex < k; classIndex++) {
            partitionFilter.addPart(percentages[classIndex]);
            Query query1 = filterController.createQuery(partitionFilter);
            filterController.setSubQuery(query1, query);
            GraphView communityView = filterController.filter(query1);
            UndirectedGraph communityGraph = graph.getModel().getUndirectedGraph(communityView);

            EigenvectorCentrality eigenvectorCentrality = new EigenvectorCentrality();
            eigenvectorCentrality.execute(communityGraph);

            GraphDistance graphDistance = new GraphDistance();
            graphDistance.execute(communityGraph);
            double betweenness = 0;
            double maxBetweenness = 0;
            org.gephi.graph.api.Node maxBetweennessNode = null;

            double closeness = 0;
            double maxCloseness = 0;
            org.gephi.graph.api.Node maxClosenessNode = null;

            double eigenvector = 0;
            double maxEigenvector = 0;
            org.gephi.graph.api.Node maxEigenvectorNode = null;

            System.out.println("Nodes in community " + classIndex + ": " + communityGraph.getNodeCount());
            System.out.println("Edges in community " + classIndex + ": " + communityGraph.getEdgeCount());

            Column betweennessColumn = null;
            Column closenessColumn = null;
            Column eigenvectorColumn = null;

            for(org.gephi.graph.api.Node node:communityGraph.getNodes()){
                for(Column col: node.getAttributeColumns()){
                    switch(col.getTitle()){
                        case "Betweenness Centrality":
                            betweennessColumn = col;
                            break;
                        case "Closeness Centrality":
                            closenessColumn = col;
                            break;
                        case "Eigenvector Centrality":
                            eigenvectorColumn = col;
                            break;
                        default:
                            break;
                    }
                }
                betweenness = (double)node.getAttribute(betweennessColumn); //9
                closeness = (double)node.getAttribute(closenessColumn); //7
                eigenvector = (double)node.getAttribute(eigenvectorColumn); //5

                if(betweenness > maxBetweenness) maxBetweennessNode = node;
                if(closeness > maxCloseness) maxClosenessNode = node;
                if(eigenvector > maxEigenvector) maxEigenvectorNode = node;
            }
            maxBetweenness = 0;
            maxCloseness = 0;
            maxEigenvector = 0;
            kNodeList.add(maxBetweennessNode);
            partitionFilter.removePart(percentages[classIndex]);
        }

		return kNodeList;

	}

}
