package main;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
* Implementation of Cluster Select algorithm, the algorithm works by first clustering the network into K communities.
* It than finds the facility location that has the minimum cost for opening within its community ignoring the rest of the opened facilities.
*/
public class ClusterSelect {

    public List<List<Node>> Search(Graph graph, int facCount, float coverageThreshold, boolean useEuclidean){
        List<Subgraph> communitySubgraphs = Utility.findModularityClasses(graph, facCount, coverageThreshold);
        List<List<Node>> nodeLists = new ArrayList<>();

        List<Node> bestNodes = new ArrayList<>();

        //Loop through all the communities to find the best facility location in each of them
        for(Subgraph communitySubgraph: communitySubgraphs){
            List<Node> communityNodes = new ArrayList<>();
            double bestCost = Double.MAX_VALUE;
            Node currentBestNode = null;
            
            //Within each community find the facility that produces the lowest cost within its community
            for(Node n: communitySubgraph.getNodes()){
                communityNodes.add(n);
                if(n.getLabel().contains(Utility.FACILITY_NAME)){
                    double tempCost = 0;
                    HashMap<Node, Double> distances = Utility.createDistanceMap(graph, n, useEuclidean);

                    //Had to manually calculate score as using the utility version calculates score based on entire graph
                    for(Node targetNode: communitySubgraph.getNodes()){
                        String[] nodeLabels = targetNode.getLabel().split(";");
                        if(nodeLabels[3].contains(Utility.RESIDENTIAL_NAME)){
                            double popScore = Utility.calculatePopulationScore(nodeLabels[2], Float.valueOf(nodeLabels[5]));
                            double distance = distances.get(targetNode);
                            if(Double.isFinite(distance) && Double.isFinite(popScore))
                                tempCost += popScore * distance;
                            if(tempCost > bestCost){
                                break;
                            }
                        }
                    }
                    
                    //If the current chosen facility produces a lower cost than replace the currently best one
                    if(tempCost < bestCost ){
                        bestCost = tempCost;
                        currentBestNode = n;
                    }
                }
            }
            bestNodes.add(currentBestNode);
            nodeLists.add(communityNodes);
        }

        nodeLists.add(bestNodes);

        return nodeLists;
    }
}
