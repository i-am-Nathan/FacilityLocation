package main;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JApplet;

import org.jgraph.JGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public class GraphDisplay extends JApplet{
	private final JGraph jgraph;
	private static final Dimension DEFAULT_SIZE = new Dimension(530,320);
	private static final Color DEFAULT_BG_COLOR = Color.decode( "#FAFBFF" );
	public GraphDisplay(SimpleWeightedGraph<Node, DefaultWeightedEdge> graph) {
		jgraph = new JGraph(new JGraphModelAdapter(graph));
	}
	
	public void display(){
		adjustDisplaySettings(jgraph);
		getContentPane().add(jgraph);
		resize(DEFAULT_SIZE);
	}

	private void adjustDisplaySettings(JGraph jg) {
        jg.setPreferredSize(DEFAULT_SIZE);

        Color c = DEFAULT_BG_COLOR;
        String colorStr = null;

        try {
            colorStr = getParameter("bgcolor");
        }
        catch(Exception e) {}

        if(colorStr != null) {
            c = Color.decode(colorStr);
        }

        jg.setBackground(c);
        
		
	}
}
