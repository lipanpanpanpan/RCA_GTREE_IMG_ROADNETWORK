package element;

import java.util.HashMap;

import element.spatial.Point;

public class SpatialDoc {
	int docID;
	HashMap<String, Float> terms;
	Point loc;
	public int vertexID;
	
	public SpatialDoc(int id, Point loc,int vertexID)
	{
		this.docID = id;
		this.terms = new HashMap<String, Float>();
		this.loc = loc;
		this.vertexID=vertexID;
	}
	
	public SpatialDoc(int id, Point loc, HashMap<String, Float> terms,int vertexID)
	{
		this.docID = id;
		this.terms = terms;
		this.loc = loc;
		this.vertexID=vertexID;
	}
	
	public void addTerm(String kwd, float tf)
	{
		terms.put(kwd, tf);
	}
	
	public HashMap<String, Float> getTerms()
	{
		return terms;
	}
	
	public int getID()
	{
		return docID;
	}
	
	public Point getLoc()
	{
		return loc;
	}
	
	
	@Override
	public String toString()
	{
		String str = "( "+docID+" @"+loc+") : ";
		for(String term:terms.keySet()){
			str += term+" "+terms.get(term)+",";
		}
		return str;
	}
}
