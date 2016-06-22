package element;

import java.util.ArrayList;
import java.util.HashMap;

import element.spatial.Point;

public class SpatialSocialDoc {
	int docID;
	ArrayList<Integer> users;
	HashMap<String, Float> terms;
	Point loc;
	public int vertexID;
	
	public SpatialSocialDoc(int docId, Point loc,int vertexID)
	{
		this.docID = docId;
		this.users = new ArrayList<Integer>();
		this.terms = new HashMap<String, Float>();
		this.loc = loc;
		this.vertexID=vertexID;
	}
	
	public SpatialSocialDoc(int docId, ArrayList<Integer> users, Point loc, HashMap<String, Float> terms,int vertexID)
	{
		this.docID = docId;
		this.users = users;
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
	
	public void addUser(int uid)
	{
		users.add(uid);
	}
	
	public ArrayList<Integer> getUsers()
	{
		return users;
	}
	
	
	@Override
	public String toString()
	{
		String str = "( "+docID+ " from users ";
		for(Integer user: users){
			str += user+" ";
		}
		str += "@"+loc+") : ";
		for(String term:terms.keySet()){
			str += term+" "+terms.get(term)+",";
		}
		return str;
	}
}
