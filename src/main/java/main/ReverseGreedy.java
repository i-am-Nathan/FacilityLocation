package main;

import java.util.HashMap;
import java.util.List;

import main.Input.NodeListHolder;

public class ReverseGreedy {
	public static HashMap<Integer,FacNode> Search(NodeListHolder graph, int facCount){
		HashMap<Integer,FacNode> currentlySelected = new HashMap<Integer,FacNode>();
		for(int i = 0; i<graph.getFacNodeList().size();i++){
			currentlySelected.put(i,graph.getFacNodeList().get(i));
		}
		
		while(currentlySelected.size()!= facCount){
			Integer removeFacNodeID = RGreed(currentlySelected);
			currentlySelected.remove(removeFacNodeID);
		}
		
		return currentlySelected;
		
	}

	// Finds a facility node which reduces the cost of the network the most and returns the hash value of that.
	private static Integer RGreed(HashMap<Integer, FacNode> currentlySelected) {
		// TODO Auto-generated method stub
		
		return null;
	}
}
