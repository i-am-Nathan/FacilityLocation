

import org.gephi.datalab.api.AttributeColumnsController;
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

import java.io.*;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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

//    public UndirectedGraph getZoneGraph(UndirectedGraph completeGraph, String zoneType){
//        Column col = completeGraph.getModel().getNodeTable().getColumn("Label");
//        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
//
//        AttributeEqualBuilder.EqualStringFilter equalStringFilter = new AttributeEqualBuilder.EqualStringFilter.Node(col);
//        equalStringFilter.init(completeGraph);
//        equalStringFilter.setUseRegex(true);
//        equalStringFilter.setPattern("^.*(" + zoneType + ").*$");
//        Query query = filterController.createQuery(equalStringFilter);
//        GraphView graphView = filterController.filter(query);
//
//        return completeGraph.getModel().getUndirectedGraph(graphView);
//    }

    public void setXY(UndirectedGraph graph){
        BufferedReader br = null;
        String line;
        String splitRegex = ",";
        boolean headerRecorded = false;

        DecimalFormat df = new DecimalFormat("###.#####");
        df.setRoundingMode(RoundingMode.DOWN);

        try {
            for (Node n:graph.getNodes()) {
                String[] nodeData = n.getLabel().split( ";");
                br = new BufferedReader(new FileReader("src\\main\\java\\main\\YX.csv"));
                while ((line = br.readLine()) != null) {
                    if(!headerRecorded){
                        headerRecorded = true;
                        continue;
                    }

                    String[] lineData = line.split(splitRegex);

                    float x = Float.parseFloat(lineData[1]);
                    float y = Float.parseFloat(lineData[0]);

                    if(nodeData[0].equals(lineData[3]) &&
                            nodeData[1].equals(lineData[4]) &&
                            nodeData[2].equals(lineData[5]) &&
                            nodeData[3].equals(lineData[6]) &&
                            Math.abs(Double.parseDouble(nodeData[4]) - Double.parseDouble(lineData[7])) < 0.001 &&
                            Math.abs(Double.parseDouble(nodeData[5]) - Double.parseDouble(lineData[8])) < 0.001){
                        n.setPosition(x,y);
                    }
                }
                headerRecorded = false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
//
//    public NodeListHolder  fileToNodeListHolder(String fileName){
//    	NodeListHolder nlh = new NodeListHolder();
//    	List<PopNode> resNodes = new ArrayList<PopNode>();
//    	List<FacNode> facNodes = new ArrayList<FacNode>();
//    	String line;
//    	String splitRegex = ",";
//    	boolean headerRecorded = false;
//
//    	try {
//			BufferedReader br = new BufferedReader(new FileReader(fileName));
//			while((line=br.readLine())!=null){
//				if(!headerRecorded){
//					headerRecorded = true;
//					continue;
//				}
//
//				String[] lineData = line.split(splitRegex);
//				if(lineData[4].startsWith("Business")){
//					facNodes.add(new FacNode(Float.valueOf(lineData[0]),Float.valueOf(lineData[1]),Integer.parseInt(lineData[2])));
//				}
//				else if(lineData[4].startsWith("Residential")){
//					resNodes.add(new PopNode(Float.valueOf(lineData[0]),Float.valueOf(lineData[1]),Integer.parseInt(lineData[2]),lineData[5], Float.valueOf(lineData[8])));
//				}
//			}
//
//
//    	} catch (IOException e) {
//			e.printStackTrace();
//		}
//    	nlh.setNodeLists(resNodes,facNodes);
//
//
//		return nlh;
//
//    }
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
                double populationScore = Utility.CalculatePopulationScore(nodeLabels[2], Float.valueOf(nodeLabels[5]));
                popNodeList.add(new PopNode(populationScore,n.x(),n.y()));
            } else if (nodeLabels[1].startsWith("Business")){
                facNodeList.add(new FacNode(true, n.x(),n.y()));
            }
        }
        nlh.setNodeLists(popNodeList, facNodeList);

        return nlh;
    }
*/
}
