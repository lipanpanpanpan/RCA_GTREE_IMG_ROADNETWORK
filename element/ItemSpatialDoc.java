package element;

import java.io.Serializable;
import element.spatial.Point;

public class ItemSpatialDoc  implements Comparable<ItemSpatialDoc>, Serializable{
	
	private static final long serialVersionUID = -278697927816802083L;
	public int docID;
	public float textRelevance;
	public Point point;
	public int VertexID;
	
	public ItemSpatialDoc(int docID, float textRelevance, Point point,int VertexID)
	{
		this.docID = docID;
		this.textRelevance = textRelevance;
		this.point = point;
		this.VertexID=VertexID;
	}
	
	
	@Override
	public String toString()
	{
		return "("+docID+","+textRelevance+","+point+")";
	}

	
	@Override
	public int compareTo(ItemSpatialDoc arg0) {
		return new Integer(docID).compareTo(arg0.docID);
	}
	
}
