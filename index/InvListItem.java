package index;

import java.io.Serializable;

import element.query_processing.HeapItem;



public abstract class InvListItem implements Comparable<InvListItem>, Serializable, HeapItem {
	private static final long serialVersionUID = -846752185329127471L;
	protected Float sortVal;
	protected int joinKey;
	
	
	public float getSortVal()
	{
		return sortVal;
	}
	
	@Override
	public float getScore()
	{
		return sortVal;
	}
	
	
	public void setScore(float score)
	{
		this.sortVal = score;
	}
	
	public int getJoinKey()
	{
		return joinKey;
	}
	
	@Override
	public int compareTo(InvListItem other)
	{
		return other.sortVal.compareTo(sortVal);
	}
}
