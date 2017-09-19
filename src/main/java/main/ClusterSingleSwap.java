package main;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClusterSingleSwap {
    public List<List<Node>> Search(Graph graph, int facCount, float coverageThreshold, boolean useEuclidean){

        List<List<Node>> nodeLists = new ArrayList<>();
        List<Subgraph> communitySubgraphs = Utility.findModularityClasses(graph, facCount, coverageThreshold);
        HashMap<Graph, Node> currentBestNodes = new HashMap<>();

        for(Graph communitySubgraph: communitySubgraphs){
            boolean nodeSelected = false;
            List<Node> communityNodes = new ArrayList<>();
            for(Node n: communitySubgraph.getNodes()){
                communityNodes.add(n);
                if(n.getLabel().contains(Utility.FACILITY_NAME)){
                    if(!nodeSelected) {
                        currentBestNodes.put(communitySubgraph, n);
                        nodeSelected = true;
                    }
                }
            }
            nodeLists.add(communityNodes);
        }

        HashMap<Node, HashMap<Node, Double>> distancesToFacs = new HashMap<>();

        for(Node currentBestNode: currentBestNodes.values()){
            HashMap<Node, Double> distances = Utility.createDistanceMap(graph, currentBestNode, useEuclidean);
            distancesToFacs.put(currentBestNode, distances);
        }

        HashMap<Node, Double> bestDistances = createBestDistancesMap(graph, distancesToFacs);

        double bestCost = calculateCost(bestDistances, Double.MAX_VALUE);

        double oldCost = 0;

        while(oldCost != bestCost){
            oldCost = bestCost;
            for (Graph communitySubgraph: communitySubgraphs){
                for(Node n: communitySubgraph.getNodes()){
                    if(n.getLabel().contains(Utility.FACILITY_NAME) && !currentBestNodes.get(communitySubgraph).equals(n)){
                        HashMap<Node, Double> tempHashMap = distancesToFacs.remove(currentBestNodes.get(communitySubgraph));
                        distancesToFacs.put(n, Utility.createDistanceMap(graph, n, useEuclidean));
                        bestDistances = createBestDistancesMap(graph, distancesToFacs);
                        double tempCost = calculateCost(bestDistances, bestCost);
                        if(tempCost < bestCost) {
                            bestCost = tempCost;
                            currentBestNodes.replace(communitySubgraph, n);
                        }else {
                            distancesToFacs.remove(n);
                            distancesToFacs.put(currentBestNodes.get(communitySubgraph), tempHashMap);
                        }
                    }
                }
            }
        }
        List<Node> bestNodeList = new ArrayList<>();
        for(Node bestNode : currentBestNodes.values()){
            bestNodeList.add(bestNode);
        }
        nodeLists.add(bestNodeList);
        return nodeLists;

    }

    public HashMap<Node, Double> createBestDistancesMap(Graph graph, HashMap<Node, HashMap<Node, Double>> distancesToFacs){

        HashMap<Node, Double> bestDistances = new HashMap<>();

        for(Node n: graph.getNodes()){
            String[] nodeLabels = n.getLabel().split(";");
            if (nodeLabels[1].startsWith(Utility.RESIDENTIAL_NAME)) {
                double bestDistance = Double.MAX_VALUE;
                for (HashMap<Node, Double> distances : distancesToFacs.values()) {
                    double distance = distances.get(n);
                    if (bestDistance > distance) bestDistance = distance;
                }
                if(bestDistance == Double.MAX_VALUE) continue;

                bestDistances.put(n, bestDistance);
            }
        }

        return bestDistances;
    }

    public double calculateCost(HashMap<Node, Double> bestDistances, double currentBestCost){
        double cost = 0;
        for (Node n: bestDistances.keySet()){
            String[] nodeLabels = n.getLabel().split(";");
            double distance = bestDistances.get(n);
            double popScore = Utility.calculatePopulationScore(nodeLabels[2], Float.valueOf(nodeLabels[5]));
            if(Double.isFinite(distance)) {
                cost += popScore * distance;
            }
            if(cost > currentBestCost) break;
        }

        return cost;
    }
}
