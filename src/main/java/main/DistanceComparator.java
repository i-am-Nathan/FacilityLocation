package main;

import java.util.Comparator;

public class DistanceComparator implements Comparator<FacNode> {
	@Override
	public int compare(FacNode fac1, FacNode fac2) {
		return Utility.euclidDistance(fac1,fac2);
	}
}
