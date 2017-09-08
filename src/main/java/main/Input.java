package main;



import org.gephi.datalab.api.AttributeColumnsController;
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
import java.text.DecimalFormat;

/**
 * Created by Juno on 4/07/2017.
 */
public class Input {
    private Workspace workspace;

    public Input(){
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        workspace = pc.getCurrentWorkspace();
    }

    //Imports a graph from the supplied file name
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
        
		AttributeColumnsController attributeColumnsController = Lookup.getDefault().lookup(AttributeColumnsController.class);
		Table edgeTable = graph.getModel().getEdgeTable();
		attributeColumnsController.copyColumnDataToOtherColumn(edgeTable, edgeTable.getColumn("Label"), edgeTable.getColumn("Weight"));


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

    //Sets the x and y coordinates for the graph
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
                br = new BufferedReader(new FileReader(Utility.XY_FILE));
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
}
