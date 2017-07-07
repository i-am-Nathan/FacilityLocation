package main;

import org.gephi.graph.api.UndirectedGraph;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.List;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;

public class LocalSearch {
	public List<Node> Search(UndirectedGraph graph, int businessNum){
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();
		
		List<Node> facNodeList = new ArrayList<Node>();
		List<Node> popNodeList = new ArrayList<Node>();
		for(Node node : graph.getNodes()){
			if(isBusiness(node,businessNum)){
				facNodeList.add(node);
			} 
			else if(isResidential(node)){
				popNodeList.add(node);
			}
		}
		
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
		UndirectedGraph newGraph = graphModel.getUndirectedGraph();
		newGraph.addAllNodes(facNodeList);
		newGraph.addAllNodes(popNodeList);
		
		
		return null;
		
	}
	
	private boolean isResidential(Node node){
		return node.getLabel().toLowerCase().contains(("Residential").toLowerCase());
	}

	private boolean isBusiness(Node node, int businessNum) {
		return node.getLabel().toLowerCase().contains(("Business " + businessNum).toLowerCase());
	}
}
