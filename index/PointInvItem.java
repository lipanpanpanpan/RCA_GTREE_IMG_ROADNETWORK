package index;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import element.spatial.Point;

public class PointInvItem extends InvListItem {
	private static final long serialVersionUID = -1952425106184620238L;
	Point loc;
	
	public PointInvItem(int docID, Point loc, float hashValue)
	{
		this.joinKey = docID;
		this.loc = loc;
		this.sortVal = hashValue;
	}
	
	public Point getLoc()
	{
		return loc;
	}
	
	
	@Override
	public String toString()
	{
		return "("+loc+", "+joinKey+", "+sortVal+")";
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(joinKey);
		out.writeFloat(sortVal);
		out.writeDouble(loc.getX());
		out.writeDouble(loc.getY());
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		joinKey = in.readInt();
		sortVal = in.readFloat();
		loc = new Point(in.readDouble(), in.readDouble());
	}

}
