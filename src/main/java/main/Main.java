package main;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_ADDPeer;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;

public class Main {
	public static UndirectedGraph _importedGraph;
	public static List<Node> _facNodes;
	public static List<Node> _resNodes;


	public static void main(String[] args){
		boolean useEuclidean = false;
		int facCount;

		for(int j = 0; j < 1; j++) {
			for (facCount = 1; facCount <= 10; facCount++) {
				if(!useEuclidean) System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@\nREVERSE GREEDY - " + facCount + " FACILITY NODES - NON EUCLIDEAN\n");
				else System.out.println("REVERSE GREEDY - " + facCount + " FACILITY NODES - EUCLIDEAN\n");
				for (int index = 1; index <= 5; index++) {
					System.out.println("#####################\n");
					System.out.println("ITERATION " + index);

					Input i = new Input();
					Graph graph = i.importGraph("300m.gml");
					ReverseGreedy algorithm = new ReverseGreedy();

					long startTime = System.currentTimeMillis();
					List<Node> result = algorithm.Search(facCount, graph, useEuclidean);
					long endTime = System.currentTimeMillis();
					long totalTime = endTime - startTime;
					long minutes = (totalTime / 1000) / 60;
					long seconds = (totalTime / 1000) % 60;

					System.out.println("Time taken for Reverse Greedy and " + facCount + " facilities: " + totalTime + " (" + minutes + ":" + seconds + ")");
					System.out.printf("THE RESULTING SCORE FOR REVERSE GREEDY IS IS: %f\n", Utility.calculateFinalScore(graph, result, useEuclidean));
				}
			}
			useEuclidean = true;
		}

		Output output = new Output();
////		output.export(result, "singleswap1node");
//		for(List<Node> nl: result){
//			output.export(nl, "ClusterSwap");
//		}

	}
}
