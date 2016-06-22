package element;

import java.io.Serializable;

import element.spatial.Point;

public class ItemSpatialCurve implements Comparable<ItemSpatialCurve>, Serializable{
	
	private static final long serialVersionUID = -278697927816802083L;
	public int docID;
	public int zorder;
	public Point point;
	public int VertexID;
	
	
	public ItemSpatialCurve(int docID, int zorder,  Point point,int vertexID)
	{
		this.docID = docID;
		this.zorder = zorder;
		this.point = point;
		this.VertexID=vertexID;
	}
	
	
	@Override
	public String toString()
	{
		return "("+docID+","+zorder+","+point+")";
	}

	/**
	 * ��docId����
	 */
	@Override
	public int compareTo(ItemSpatialCurve arg0) {
		return new Integer(zorder).compareTo(arg0.zorder);
	}
	
}

