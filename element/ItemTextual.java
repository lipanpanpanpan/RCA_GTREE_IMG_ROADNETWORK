package element;

import java.io.Serializable;

public class ItemTextual implements Comparable<ItemTextual>, Serializable{
	
	private static final long serialVersionUID = -278697927816802083L;
	public int docID;
	public float textRelevance;
	public int vertexID;

	
	public ItemTextual(int docID, float textRelevance,int vertexID)
	{
		this.docID = docID;
		this.textRelevance = textRelevance;
		this.vertexID=vertexID;
	}
	
	
	@Override
	public String toString()
	{
		return "("+docID+","+textRelevance+")";
	}

	
	@Override
	public int compareTo(ItemTextual arg0) {
		return new Float(arg0.textRelevance).compareTo(textRelevance);
	}
}

