package main;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_ADDPeer;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.openide.util.datatransfer.ExTransferable;

public class Main {
	public static UndirectedGraph _importedGraph;
	public static List<Node> _facNodes;
	public static List<Node> _resNodes;


	public static void main(String[] args){
//	    Input i = new Input();
//
//	    i.convertGMLFile("GML_ZONE.gml");

		boolean useEuclidean = false;
		int facCount = 15;

//		for(int j = 0; j < 2; j++) {
//			if(useEuclidean)System.out.println("CLUSTER SWAP FOR EUCLIDEAN");
//			else System.out.println("CLUSTER SWAP FOR NETWORK");
//
//			for (facCount = 1; facCount <= 11; facCount += 2) {
//				System.out.println("\n\n#####################\n"+facCount+" FACILITY NODE(S)");
//				for (int index = 1; index <= 5; index++) {
//					System.out.println("ITERATION " + index);

					Input i = new Input();
					Graph graph = i.importGraph("FIXED_GML_ZONE.gml");
					ClusterSingleSwap css = new ClusterSingleSwap();

					long startTime = System.currentTimeMillis();
					List<List<Node>> csr = css.Search(graph, facCount, 75, useEuclidean);

					long endTime = System.currentTimeMillis();
					long totalTime = endTime - startTime;
					long minutes = (totalTime / 1000) / 60;
					long seconds = (totalTime / 1000) % 60;

					List<Node> result = csr.get(csr.size()-1);

					System.out.println("Time taken for cluster swap and " + facCount + " facilities: " + totalTime + " (" + minutes + ":" + seconds + ")");
					System.out.printf("THE RESULTING SCORE FOR CLUSTER SWAP IS: %f\n", Utility.calculateFinalScore(graph, result, useEuclidean));
//				}
//			}
//			useEuclidean = true;
//		}
//
//		Output o = new Output();
//		for(List<Node> nl : result){
//			o.export(nl, "Countdown");
//		}


	}
}
