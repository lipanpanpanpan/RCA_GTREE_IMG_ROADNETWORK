package element;

import java.io.Serializable;

import element.spatial.Point;

public class ItemSpatialCurveForCombined implements Comparable<ItemSpatialCurveForCombined>, Serializable{
	
	private static final long serialVersionUID = -278697927816802083L;
	public int docID;
	public int zorder;
	public Point point;
	
	
	public ItemSpatialCurveForCombined(int docID, int zorder,  Point point)
	{
		this.docID = docID;
		this.zorder = zorder;
		this.point = point;
	}
	
	
	@Override
	public String toString()
	{
		return "("+docID+","+zorder+","+point+")";
	}

	/**
	 * ∞¥docId≈≈–Ú
	 */
	@Override
	public int compareTo(ItemSpatialCurveForCombined arg0) {
		return new Integer(zorder).compareTo(arg0.zorder);
	}
	
}

