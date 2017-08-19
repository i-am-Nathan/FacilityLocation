package main;

import org.gephi.graph.api.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Juno on 1/08/2017.
 */
public class Output {

    public void export(List<Node> nodeList){
        FileWriter writer = null;
        BufferedReader br = null;
        String line;
        String splitRegex = ",";
        boolean headerRecorded = false;
        DecimalFormat df = new DecimalFormat("###.#####");
        df.setRoundingMode(RoundingMode.DOWN);

        try {
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Timestamp(System.currentTimeMillis()));
            writer = new FileWriter(timeStamp+".csv");

            for (Node n:nodeList) {
                String[] nodeData = n.getLabel().split( ";");
                br = new BufferedReader(new FileReader("src\\main\\java\\main\\YX.csv"));
                while ((line = br.readLine()) != null) {
                    if(!headerRecorded){
                        writer.append(line);
                        headerRecorded = true;
                        continue;
                    }

                    String[] lineData = line.split(splitRegex);

                    if(nodeData[0].equals(lineData[3])){
                        if(nodeData[1].equals(lineData[4]))
                            if(nodeData[2].equals(lineData[5]))
                                if(nodeData[3].equals(lineData[6])) {
                                    if (Math.abs(Double.parseDouble(nodeData[4]) - Double.parseDouble(lineData[7])) < 0.001) {
                                        if (Math.abs(Double.parseDouble(nodeData[5]) - Double.parseDouble(lineData[8])) < 0.001) {
                                            writer.append("\n");
                                            writer.append(line);
                                        }
                                    }
                                }

                    }
//
//                    String[] lineData = line.split(splitRegex);
//
//                    if(nodeData[0].equals(lineData[3]) &&
//                            nodeData[1].equals(lineData[4]) &&
//                            nodeData[2].equals(lineData[5]) &&
//                            nodeData[3].equals(lineData[6]) &&
//                            Math.abs(Double.parseDouble(nodeData[4]) - Double.parseDouble(lineData[7])) < 0.001 &&
//                            Math.abs(Double.parseDouble(nodeData[5]) - Double.parseDouble(lineData[8])) < 0.001){
//                        System.out.println("MATCH!");
//                        writer.append("\n");
//                        writer.append(line);
//                    }

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public void exportFacNode(List<FacNode> nodeList){
        FileWriter writer = null;
        BufferedReader br = null;
        String line;
        String splitRegex = ",";
        boolean headerRecorded = false;
        DecimalFormat df = new DecimalFormat("###.#####");
        df.setRoundingMode(RoundingMode.DOWN);

        try {
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Timestamp(System.currentTimeMillis()));
            writer = new FileWriter(timeStamp+".csv");

            for (FacNode n:nodeList) {
                br = new BufferedReader(new FileReader("src\\main\\java\\main\\YX.csv"));
                while ((line = br.readLine()) != null) {
                    if(!headerRecorded){
                        writer.append(line);
                        headerRecorded = true;
                        continue;
                    }

                    String[] lineData = line.split(splitRegex);
                    if(n.getID()== Integer.parseInt(lineData[2])){
                    	writer.append("\n");
                    	writer.append(line);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
