package main;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_ADDPeer;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.openide.util.datatransfer.ExTransferable;

/**
* The main class is used to call all the created algorithms and setting the parameters
* It will display the results of each of the algorithms and the time
*/
public class Main {
	public static UndirectedGraph _importedGraph;
	public static List<Node> _facNodes;
	public static List<Node> _resNodes;
	public boolean useEuclidean = false;
	public int facCount = 3;
	public int coverageThreshold = 75;

	public static void main(String[] args){

		Input i = new Input();
		Graph graph = i.importGraph("300.gml");

		ClusterSingleSwap css = new ClusterSingleSwap();
		ClusterSelect cSelect = new ClusterSelect();
		SingleSwap ss = new SingleSwap();
		ReverseGreedy rGreedy = new ReverseGreedy();

		// Peforming Single Swap with Community Detection===========================================
		long startTime = System.currentTimeMillis();
		List<List<Node>> csr = css.Search(graph, facCount, coverageThreshold, useEuclidean);
		long endTime = System.currentTimeMillis();

		long totalTime = endTime - startTime;
		long minutes = (totalTime / 1000) / 60;
		long seconds = (totalTime / 1000) % 60;

		List<Node> result = csr.get(csr.size()-1);

		System.out.println("Time taken for Single Swap with Community Detection and " + facCount + " facilities: " + totalTime + " (" + minutes + ":" + seconds + ")");
		System.out.printf("Resulting Score Single Swap with Community Detection: %f\n", Utility.calculateFinalScore(graph, result, useEuclidean));

		//Performing Cluster Select================================================================
		startTime = System.currentTimeMillis();
		List<List<Node>> cSelectr = cSelect.Search(graph, facCount, coverageThreshold, useEuclidean);
		endTime = System.currentTimeMillis();

		totalTime = endTime - startTime;
		minutes = (totalTime / 1000) / 60;
		seconds = (totalTime / 1000) % 60;

		result = cSelectr.get(cSelectr.size()-1);

		System.out.println("Time taken for Cluster Select and " + facCount + " facilities: " + totalTime + " (" + minutes + ":" + seconds + ")");
		System.out.printf("Resulting Score for Cluster Select: %f\n", Utility.calculateFinalScore(graph, result, useEuclidean));

		//Performing Local Search with Single Swap=================================================
		startTime = System.currentTimeMillis();
		result = ss.Search(graph, facCount, useEuclidean, false);
		endTime = System.currentTimeMillis();

		totalTime = endTime - startTime;
		minutes = (totalTime / 1000) / 60;
		seconds = (totalTime / 1000) % 60;

		System.out.println("Time taken for Local Search with Single Swap and " + facCount + " facilities: " + totalTime + " (" + minutes + ":" + seconds + ")");
		System.out.printf("Result Score for Local Search with Single Swap: %f\n", Utility.calculateFinalScore(graph, result, useEuclidean));

		//Performing Reverse Greedy===================================Note this algorithm can take approximately 30min
		startTime = System.currentTimeMillis();
		result = rGreedy.Search(graph, facCount, useEuclidean);
		endTime = System.currentTimeMillis();

		totalTime = endTime - startTime;
		minutes = (totalTime / 1000) / 60;
		seconds = (totalTime / 1000) % 60;

		System.out.println("Time taken for Reverse Greedy and " + facCount + " facilities: " + totalTime + " (" + minutes + ":" + seconds + ")");
		System.out.printf("Resulting Score for Reverse Greedy: %f\n", Utility.calculateFinalScore(graph, result, useEuclidean));

	}
}
