package main;

import org.gephi.graph.api.*;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import java.io.File;
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

    public NodeListHolder Import(String fileName){
        NodeListHolder nlh = new NodeListHolder();
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
        DirectedGraph graph = graphModel.getDirectedGraph();
        System.out.println("Nodes: " + graph.getNodeCount());
        System.out.println("Edges: " + graph.getEdgeCount());

        List<PopNode> popNodeList = new ArrayList<>();
        List<FacNode> facNodeList = new ArrayList<>();

        List<String> zones = new ArrayList<>();
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

    public float CalculatePopulationScore(String zone, float area){
        int density;
        switch (zone) {
            case "1":
                density = 1200;
                break;
            case "2A1":
                density = 5000;
                break;
            case "2A":
                density = 800;
                break;
            case "2B":
            case "3C":
                density = 600;
                break;
            case "2C":
                density = 350;
                break;
            case "3A":
            case "4A":
            case "4B":
                density = 450;
                break;
            case "3B":
                density = 500;
                break;
            case "5":
                density = 350;
                break;
            case "6A":
            case "6A1":
            case "6B":
            case "6B1":
            case "6C":
            case "6C1":
                density = 150;
                break;
            case "7":
                density = 200;
                break;
            default:
                density = 0;
        }
        return (area/density);
    }
}
