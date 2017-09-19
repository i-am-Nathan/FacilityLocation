package main;



import com.mysql.jdbc.Buffer;
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
import java.net.URISyntaxException;
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

    public void convertGMLFile(String fileName){


        try {
            File inFile = new File(getClass().getResource(fileName).toURI());

            try(BufferedReader bufferedReader = new BufferedReader(new FileReader(inFile))) {

                try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("FIXED_" + fileName))) {

                    String line, nodeLine, edgeLine;

                    while ((line = bufferedReader.readLine()) != null) {
                        if (line.contains("node")) {
                            bufferedWriter.write(line);
                            bufferedWriter.write(System.getProperty("line.separator"));

                            boolean firstLabelWritten = false;

                            while (!(nodeLine = bufferedReader.readLine()).contains("]")) {
                                if (nodeLine.contains("label")) {
                                    String label = nodeLine.split("\"")[1];
                                    String content;
                                    if(label.contains("Zone_label")){
                                        content = label.split("  ")[1];
                                    } else {
                                        content = label.substring(label.indexOf(' ') + 1);
                                    }
                                    if (!firstLabelWritten) {
                                        String indent = nodeLine.split("label")[0];
                                        bufferedWriter.write(indent+"label \"");
                                        firstLabelWritten = true;
                                    }
                                    bufferedWriter.write(content);
                                    if (!label.contains("SHAPE_Area")) {
                                        bufferedWriter.write(";");
                                    }
                                } else {
                                    bufferedWriter.write(nodeLine);
                                    bufferedWriter.write(System.getProperty("line.separator"));
                                }
                            }
                            bufferedWriter.write("\""+System.getProperty("line.separator") + nodeLine);
                            bufferedWriter.write(System.getProperty("line.separator"));

                        } else if (line.contains("edge")) {
                            bufferedWriter.write(line);
                            bufferedWriter.write(System.getProperty("line.separator"));

                            while (!(edgeLine = bufferedReader.readLine()).contains("]")) {
                                if (edgeLine.contains("label")) {
                                    String weightValue = edgeLine.split("\"")[1];
                                    String indent = edgeLine.split("label")[0];
                                    bufferedWriter.write(indent+"weight " + weightValue);
                                } else {
                                    bufferedWriter.write(edgeLine);
                                }
                                bufferedWriter.write(System.getProperty("line.separator"));
                            }
                            bufferedWriter.write(edgeLine);
                            bufferedWriter.write(System.getProperty("line.separator"));

                        } else {
                            bufferedWriter.write(line);
                            bufferedWriter.write(System.getProperty("line.separator"));
                        }
                    }
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
