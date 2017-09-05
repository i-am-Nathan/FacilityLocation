package main;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClusterSelect {

    public List<Node> Search(Graph graph, int facCount, float coverageThreshold, boolean useEuclidean){
        List<Subgraph> communitySubgraphs = Utility.findModularityClasses(graph, facCount, coverageThreshold);

        List<Node> bestNodes = new ArrayList<>();

        for(Subgraph communitySubgraph: communitySubgraphs){
            double bestCost = Double.MAX_VALUE;
            Node currentBestNode = null;
            for(Node n: communitySubgraph.getNodes()){
                if(n.getLabel().contains(Utility.FACILITY_NAME)){
                    List<Node> tempList = Arrays.asList(n);
                    double tempCost = Utility.calculateFinalScore(communitySubgraph, tempList, useEuclidean);
                    if(tempCost < bestCost ){
                        bestCost = tempCost;
                        currentBestNode = n;
                    }
                }
            }
            bestNodes.add(currentBestNode);
        }

        return bestNodes;
    }
}
