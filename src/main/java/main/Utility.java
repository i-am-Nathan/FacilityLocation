package main;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

public class Utility {
	static int euclidDistance(FacNode fac1, FacNode fac2) {
		Double distance = Math.sqrt(Math.pow(fac2.xCoord-fac1.xCoord,2) + Math.pow(fac2.yCoord - fac1.yCoord, 2));
		return (int) Math.round(distance);
	}
	
	
	@SuppressWarnings("unchecked")
	public static HashMap sortByValues(HashMap map){
		List list = new LinkedList(map.entrySet());
		
		Collections.sort(list, new Comparator(){

			@Override
			public int compare(Object o1, Object o2) {
				return((Comparable)((Map.Entry)(o1)).getValue()).compareTo(((Map.Entry)(o2)).getValue());
			}
			
		});
		
		HashMap sortedHashMap = new LinkedHashMap();
		for(Iterator it = list.iterator(); it.hasNext();){
			Map.Entry entry = (Map.Entry)it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		
		
		return sortedHashMap;
		
	}

}
