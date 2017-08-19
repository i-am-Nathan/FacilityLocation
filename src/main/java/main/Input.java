package main;

import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.plugin.attribute.AttributeEqualBuilder;
import org.gephi.graph.api.*;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Juno on 4/07/2017.
 */
public class Input {
    private ProjectController pc;
    private Workspace workspace;

    public Input(){
        pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        workspace = pc.getCurrentWorkspace();
    }

    public UndirectedGraph importGraph(String fileName){
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        Container container;

        try{
            File file = new File(getClass().getResource(fileName).toURI());
            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);
            container.getLoader().setAllowAutoNode(false);
        } catch(Exception ex){
            ex.printStackTrace();
            return null;
        }

        importController.process(container,new DefaultProcessor(), workspace);

        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph graph = graphModel.getUndirectedGraph();

        return graph;
    }

    public UndirectedGraph getZoneGraph(UndirectedGraph completeGraph, String zoneType){
        Column col = completeGraph.getModel().getNodeTable().getColumn("Label");
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);

        AttributeEqualBuilder.EqualStringFilter equalStringFilter = new AttributeEqualBuilder.EqualStringFilter.Node(col);
        equalStringFilter.init(completeGraph);
        equalStringFilter.setUseRegex(true);
        equalStringFilter.setPattern("^.*(" + zoneType + ").*$");
        Query query = filterController.createQuery(equalStringFilter);
        GraphView graphView = filterController.filter(query);

        return completeGraph.getModel().getUndirectedGraph(graphView);
    }
    
    public NodeListHolder  fileToNodeListHolder(String fileName){
    	NodeListHolder nlh = new NodeListHolder();
    	List<PopNode> resNodes = new ArrayList<PopNode>();
    	List<FacNode> facNodes = new ArrayList<FacNode>();
    	String line;
    	String splitRegex = ",";
    	boolean headerRecorded = false;
    	
    	try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			while((line=br.readLine())!=null){
				if(!headerRecorded){
					headerRecorded = true;
					continue;
				}
				
				String[] lineData = line.split(splitRegex);
				if(lineData[4].startsWith("Business")){
					facNodes.add(new FacNode(Float.valueOf(lineData[0]),Float.valueOf(lineData[1]),Integer.parseInt(lineData[2])));
				}
				else if(lineData[4].startsWith("Residential")){
					resNodes.add(new PopNode(Float.valueOf(lineData[0]),Float.valueOf(lineData[1]),Integer.parseInt(lineData[2]),lineData[5], Float.valueOf(lineData[8])));
				}
			}
    	
    	
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    
    	nlh.setNodeLists(resNodes,facNodes);
    	
    	
		return nlh;
    	
    }
/*
 * 
    public NodeListHolder graphToNodeListHolder(UndirectedGraph graph){
        NodeListHolder nlh = new NodeListHolder();

        List<PopNode> popNodeList = new ArrayList<>();
        List<FacNode> facNodeList = new ArrayList<>();

//        List<String> zones = new ArrayList<>();
        for (org.gephi.graph.api.Node n: graph.getNodes()) {
            String label = n.getLabel();
            String[] nodeLabels = label.split(";");
            if (nodeLabels[1].startsWith("Residential")){
                float populationScore = CalculatePopulationScore(nodeLabels[2], Float.valueOf(nodeLabels[5]));
                popNodeList.add(new PopNode(populationScore,n.x(),n.y()));
            } else if (nodeLabels[1].startsWith("Business")){
                facNodeList.add(new FacNode(true, n.x(),n.y()));
            }
        }
        nlh.setNodeLists(popNodeList, facNodeList);

        return nlh;
    }
*/
    class NodeListHolder{
        private List<PopNode> popNodeList = new ArrayList<>();
        private List<FacNode> facNodeList = new ArrayList<>();

        private void setNodeLists(List<PopNode> popNodeList, List<FacNode> facNodeList){
            this.popNodeList = popNodeList;
            this.facNodeList = facNodeList;
        }

        public List<PopNode> getPopNodeList() {
            return popNodeList;
        }

        public List<FacNode> getFacNodeList() {
            return facNodeList;
        }
    }

}
