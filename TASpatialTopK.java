

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Vector;

import search.Scorer;
import element.ItemSpatialCurve;
import element.ItemSpatialDoc;
import element.ItemTextual;
import element.query_processing.Candidate;
import element.spatial.Point;

public class TASpatialTopK {
	private int kwdNum;
	private Point qLoc;

	
	private float upperBoundScore = Float.MAX_VALUE;
	private PriorityQueue<Candidate> topkResults;
	private float[] high;
	private int pos=0;
	private HashSet<Integer> accessDoc = new HashSet<Integer>();
	
	private Vector<Vector<ItemSpatialDoc>> docLists; 
	private Vector<Vector<ItemTextual>> textLists;
	private Vector<Vector<ItemSpatialCurve>> spatialLists;
	HashMap<Integer, Integer> vertexDistanceMap;
	
	private static final int distScale = 100000;
	long debugTime = 0;
	long itemAccessed = 0;
	
	
	
	
	
	
	public TASpatialTopK(Vector<Vector<ItemSpatialDoc>> docLists, Vector<Vector<ItemTextual>> textLists, Vector<Vector<ItemSpatialCurve>> spatialLists,  Point qLoc, int K, HashMap<Integer, Integer> vertexDistanceMap)
	{
		this.kwdNum = docLists.size();
		this.docLists = docLists;
		this.textLists = textLists;
		this.spatialLists = spatialLists;
		this.vertexDistanceMap=vertexDistanceMap;
		this.qLoc = qLoc;

		

		topkResults = new PriorityQueue<Candidate>();
		high = new float[kwdNum+1];
		high[kwdNum] = 0;
		for(int i=0;i < kwdNum;i++){
			high[i] = Scorer.getTextScore(textLists.get(i).firstElement().textRelevance);
			int firstDistanc=0;
			if(spatialLists.size()>0)
			{
				int firstElementVertex=spatialLists.get(i).get(0).VertexID;
				if (vertexDistanceMap.containsKey(firstElementVertex)) {
					firstDistanc=vertexDistanceMap.get(firstElementVertex);
				}
			}
			
			high[kwdNum] = Math.max(high[kwdNum], Scorer.getGtreeSpatialSocre(firstDistanc));
//			high[kwdNum] = Math.max(high[kwdNum], Scorer.getDistScore(qLoc.getDist(spatialLists.get(i).firstElement().point)));
		}
		
		// put dummy objects to topkResults and upperBounds
		for(int i=0;i < K;i++){
			topkResults.add(new Candidate(-1,-1,-1));
		}


		// sort the spatial lists by the distance
		for(int i=0;i < kwdNum;i++){
			for(int j=0;j < spatialLists.get(i).size();j++){
				spatialLists.get(i).get(j).zorder = (int)(distScale * qLoc.getDist(spatialLists.get(i).get(j).point)); 
			}
			Collections.sort(spatialLists.get(i));
		}
	}
	
	
	
	
	public void run()
	{
		do{
//			System.out.println(topkResults.peek().score);
			exploreLists();
			updateUpperBound();
		}while(!finish());
	}
	
	
	
	
	
	/*
	 * The algorithm terminates when the value of topk is larger than the object with minimum upper bound.
	 * 
	 */
	private boolean finish()
	{
		return topkResults.peek().score >= upperBoundScore;
	}
	
	
	
	/*
	 * For each textual inverted list, move forward until reach the minTextRelevance
	 * 
	 */
	private void exploreLists()
	{
		for(int i=0;i < kwdNum;i++){
			if(pos < textLists.get(i).size()){
				ItemTextual doc = textLists.get(i).get(pos);
				if(doc.docID == 22126){
					System.out.println("debug");
				}
				float score = Scorer.getTextScore(doc.textRelevance);
				high[i] = score;
				if(!accessDoc.contains(doc.docID)){
					Candidate cand = new Candidate(doc.docID,kwdNum,doc.vertexID);
					cand.addTextScore(i, score);
					fillCandidateScore(cand);
					if(cand.score > topkResults.peek().score){
						updateTopk(cand);
					}
					accessDoc.add(doc.docID);
				}
			}
		}
			
		float maxScore = 1;
		for(int i=0;i < kwdNum;i++){
			if(pos < spatialLists.get(i).size()){
				ItemSpatialCurve doc = spatialLists.get(i).get(pos);
				if(doc.docID == 22126){
					System.out.println("debug");
				}
				int distance=-1;
				if(vertexDistanceMap.containsKey(doc.VertexID))
				{
					distance=vertexDistanceMap.get(doc.VertexID);
					
				}
				
				float score=Scorer.getGtreeSpatialSocre(distance);
//				float score = Scorer.getDistScore(qLoc.getDist(doc.point));
				
				
				maxScore = Math.min(maxScore, score);
				if(!accessDoc.contains(doc.docID)){
					Candidate cand = new Candidate(doc.docID,kwdNum,doc.VertexID);
					cand.addSpatialScore(score);
					fillCandidateScore(cand);
					if(cand.score > topkResults.peek().score){
						updateTopk(cand);
					}
					accessDoc.add(doc.docID);
				}
				itemAccessed++;
			}
		}
		high[kwdNum] = maxScore;
			
		pos++;
	}
	
	
	
	/*
	 * When the worst score of a candidate is larger than top-k, we need to update the topkResults
	 * 
	 */
	private void updateTopk(Candidate cand)
	{
		topkResults.poll();
		topkResults.add(cand);
	}
	
	
	/*
	 * When we access an item and its upper bound is larger than topkScore, we need to monitor its upper bound score.
	 * 
	 */
	private void updateUpperBound()
	{
		upperBoundScore = 0f;
		for(int i=0;i < kwdNum+1;i++){
			upperBoundScore += high[i];
		}
	}
	
	
	
	/*
	 * A candidate may only contain partial results. Fill it to be a confirmed top-k
	 * score.
	 */
	private void fillCandidateScore(Candidate cand)
	{
		if(cand.docId == 22126){
			System.out.println("debug");
		}
		// First check if the spatial attribute is accessed
		if( (cand.flag.andAtIndex(0)) == 0){
			
			// There must be at least one of the keyword is set 1. Otherwise, the score 
			// would be zero and will not appear in top-k 
			for(int j=0;j < kwdNum;j++){
				if( (cand.flag.andAtIndex(1+j)) != 0){
					int index = Collections.binarySearch(docLists.get(j), new ItemSpatialDoc(cand.docId,0,null,cand.vertexID));
					if(index > 0){
						int distance=-1;
						int vertexID=docLists.get(j).get(index).VertexID;
						if(vertexDistanceMap.containsKey(vertexID))
						{
							distance=vertexDistanceMap.get(vertexID);
							
						}
						
						float spatialScore=Scorer.getGtreeSpatialSocre(distance);
//						float spatialScore = Scorer.getSpatialScore(qLoc, docLists.get(j).get(index).point);
						
						//docLocs.put(cand.docId,  docLists.get(j).get(index).point);
						cand.addSpatialScore(spatialScore);
					}
					break;
				}
			}
			
			// Next we fill the score for textual relevance
			for(int j=0;j < kwdNum;j++){
				if( (cand.flag.andAtIndex(1+j)) == 0){
					int index = Collections.binarySearch(docLists.get(j), new ItemSpatialDoc(cand.docId,0,null,cand.vertexID));
					if(index > 0){
						float score = Scorer.getTextScore(docLists.get(j).get(index).textRelevance);
						cand.addTextScore(j, score);
					}
				}
			}
		}else{
		
			// Next we fill the score for textual relevance
			for(int j=0;j < kwdNum;j++){
				if( (cand.flag .andAtIndex(1+j)) == 0 ){
					int index = Collections.binarySearch(docLists.get(j), new ItemSpatialDoc(cand.docId,0,null,cand.vertexID));
					if(index > 0){
						float score = Scorer.getTextScore(docLists.get(j).get(index).textRelevance);
						cand.addTextScore(j, score);
					}
				}
			}
		}
		cand.seeAll();
		//System.out.println(cand);
	}
	
	
	
	
	
	//==========================  The following are output functions  ============================//

	public long getAccessedItem()
	{
		return itemAccessed;
	}
	
	
	
	/*
	 * Print the top-k results when the algorithm terminates.
	 */
	public void printResult()
	{
		//printDebug();
		System.out.println("results : ");
		while(topkResults.size() > 0){
			System.out.println(topkResults.poll());
		}
	}
	
	
	
	public void printDebug()
	{
		System.out.println("=============== "+pos+"======================");
		System.out.print("Terminate condition : " + topkResults.peek().score + " >= "+ upperBoundScore);
		for(int i=0;i <= kwdNum;i++){
			System.out.println(high[i]);
		}
	}
}
