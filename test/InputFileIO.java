package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import element.SpatialDoc;
import element.SpatialSocialDoc;
import element.spatial.Point;



/**
 * The customized InputFileIO reads input from a specified format. In this format,
 * the first line is four float numbers representing the spatial space. In the
 * following, each line represents one spatial document or a spatial keyword query.
 * 
 * 
 * @author dongxiang
 *
 */
public class InputFileIO {
	BufferedReader inputStream;
	int docID;
	
	
	public InputFileIO(String filename) throws FileNotFoundException
	{
		docID = 1;
		inputStream = new BufferedReader(new FileReader(filename));
	}

	
	
	public SpatialDoc nextSpatialDocument() throws IOException
	{
		String line = inputStream.readLine();
//		System.out.println(line);
		if(line == null){
			return null;
		}
		
		int vertexID=-1;
		// It is possible that some keywords appear multiple times in a input line if the data is not cleaned.
		String parts[] = line.split(",");
		HashMap<String, Integer> kwdCount = new HashMap<String, Integer>();
		for (int i = 3; i < parts.length; i += 2) {
			String kwd = parts[i];
			
			int freq = Integer.parseInt(parts[i + 1]);
			if (kwdCount.containsKey(kwd)) {
				kwdCount.put(kwd, kwdCount.get(kwd) + freq);
			} else {
				kwdCount.put(kwd, freq);
			}
		}
		
		// Get the location information
		Point loc = new Point(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]));
		vertexID=Integer.parseInt(parts[0]);
		SpatialDoc doc = new SpatialDoc(docID++, loc, vertexID);
		
		
		// Convert the frequency of a keyword into a relevance score.
		double sumeOfWeightSquare = 0;
		double weight;
		for (String kwd : kwdCount.keySet()) {
			int freq = kwdCount.get(kwd);
			weight = 1 + Math.log(freq);
			sumeOfWeightSquare += weight * weight;
		}
		sumeOfWeightSquare = Math.sqrt(sumeOfWeightSquare);		
		for (String kwd : kwdCount.keySet()) {
			int freq = kwdCount.get(kwd);
			doc.addTerm(kwd, (float)((1 + Math.log(freq))/ sumeOfWeightSquare));
		}
		return doc;
		
	}
	
	
	/*
	 * The input file format for SpatialSocialDoc is:
	 * id lat lng |uid1|uid2|... term1 freq1 term2 freq2...
	 * 
	 */
	public SpatialSocialDoc nextSpatialSocialDocument() throws IOException
	{
		String line = inputStream.readLine();
		
		if(line == null){
			return null;
		}

		String parts[] = line.split(" ");
		Point loc = new Point(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]));
		SpatialSocialDoc doc = new SpatialSocialDoc(docID++, loc,Integer.parseInt(parts[0]));
		
		// Parse the users associated with a spatial doc 
		String[] uids = parts[3].split("\\|");
		for(int i=1;i < uids.length;i++){
			doc.addUser(Integer.parseInt(uids[i]));
		}
		
		
		
		// It is possible that some keywords appear multiple times in a input line if the data is not cleaned.
		HashMap<String, Integer> kwdCount = new HashMap<String, Integer>();
		for (int i = 4; i < parts.length; i += 2) {
			String kwd = parts[i];
			int freq = Integer.parseInt(parts[i + 1]);
			if (kwdCount.containsKey(kwd)) {
				kwdCount.put(kwd, kwdCount.get(kwd) + freq);
			} else {
				kwdCount.put(kwd, freq);
			}
		}
		
		// Convert the frequency of a keyword into a relevance score.
		double sumeOfWeightSquare = 0;
		double weight;
		for (String kwd : kwdCount.keySet()) {
			int freq = kwdCount.get(kwd);
			weight = 1 + Math.log(freq);
			sumeOfWeightSquare += weight * weight;
		}
		sumeOfWeightSquare = Math.sqrt(sumeOfWeightSquare);		
		for (String kwd : kwdCount.keySet()) {
			int freq = kwdCount.get(kwd);
			doc.addTerm(kwd, (float)((1 + Math.log(freq))/ sumeOfWeightSquare));
		}
		return doc;
	}
	
	public HashMap<Integer, ArrayList<Integer>> getSocialGraph()
	{
		HashMap<Integer, ArrayList<Integer>> graph = new HashMap<Integer, ArrayList<Integer>>();
		String line;
		try{
			while( (line=inputStream.readLine()) != null){
				String parts[] = line.split(" ");
				if(parts.length >= 2){
					int uid = Integer.parseInt(parts[0]);
					ArrayList<Integer> friends = new ArrayList<Integer>();
					for(int i=1;i < parts.length;i++){
						friends.add(Integer.parseInt(parts[i]));
					}
					graph.put(uid,  friends);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return graph;
	}
}
