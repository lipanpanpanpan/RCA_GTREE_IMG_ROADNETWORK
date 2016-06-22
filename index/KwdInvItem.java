package index;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class KwdInvItem extends InvListItem {
	private static final long serialVersionUID = -2786947927816802083L;
	
	public KwdInvItem(int docID, float score)
	{
		this.joinKey = docID;
		this.sortVal = score;
	}
	
	public void setScore(float score)
	{
		this.sortVal = score;
	}
	
	
	
	@Override
	public String toString()
	{
		return "("+joinKey+","+sortVal+")";
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(joinKey);
		out.writeFloat(sortVal);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		joinKey = in.readInt();
		sortVal = in.readFloat();
	}
	
}
