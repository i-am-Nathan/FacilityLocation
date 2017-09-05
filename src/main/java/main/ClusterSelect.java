package main;

import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClusterSelect {

    public List<List<Node>> Search(Graph graph, int facCount, float coverageThreshold, boolean useEuclidean){
        List<Subgraph> communitySubgraphs = Utility.findModularityClasses(graph, facCount, coverageThreshold);
        List<List<Node>> nodeLists = new ArrayList<>();

        List<Node> bestNodes = new ArrayList<>();

        for(Subgraph communitySubgraph: communitySubgraphs){
            List<Node> communityNodes = new ArrayList<>();
            double bestCost = Double.MAX_VALUE;
            Node currentBestNode = null;

            for(Node n: communitySubgraph.getNodes()){
                communityNodes.add(n);
                if(n.getLabel().contains(Utility.FACILITY_NAME)){
                    double tempCost = 0;
                    HashMap<Node, Double> distances;
                    if(useEuclidean) distances = Utility.createEuclideanSet(graph, n);
                    else distances = Utility.computeDistances(graph, n);

                    for(Node targetNode: communitySubgraph.getNodes()){
                        String[] nodeLabels = targetNode.getLabel().split(";");
                        if(nodeLabels[3].contains(Utility.RESIDENTIAL_NAME)){
                            double popScore = Utility.CalculatePopulationScore(nodeLabels[2], Float.valueOf(nodeLabels[5]));
                            double distance = distances.get(targetNode);
                            if(Double.isFinite(distance) && Double.isFinite(popScore))
                                tempCost += popScore * distance;

                        }
                    }

                    if(tempCost < bestCost ){
                        bestCost = tempCost;
                        System.out.printf("New Best cost: %f\n", bestCost);
                        currentBestNode = n;
                    }
                }
            }
            bestNodes.add(currentBestNode);
            nodeLists.add(communityNodes);

            System.out.println("#######");
        }

        nodeLists.add(bestNodes);

        return nodeLists;
    }
}
