package index;

import java.util.Comparator;

public class DocIdComparator implements Comparator<InvListItem>{
	
	@Override
	public int compare(InvListItem o1, InvListItem o2) {
		return new Integer(o1.getJoinKey()).compareTo(o2.getJoinKey());
	}
 
}
