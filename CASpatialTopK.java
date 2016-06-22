

import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Vector;

import search.Scorer;
import element.ItemSpatialCurve;
import element.ItemSpatialDoc;
import element.ItemTextual;
import element.query_processing.Candidate;
import element.spatial.Point;

public class CASpatialTopK {
	private int kwdNum;
	private Point qLoc;
	public static int H = 20;
	
	

	
	

	private HashMap<Integer, Candidate> candidates;
	private PriorityQueue<Candidate> topkResults;
	private PriorityQueue<UpperScore>  upperBounds;
	private float[] high;
	private int pos=0;
	
	private Vector<Vector<ItemSpatialDoc>> docLists; 
	private Vector<Vector<ItemTextual>> textLists;
	private Vector<Vector<ItemSpatialCurve>> spatialLists;
	private Vector<ItemSpatialCurve> gtreeSpatiallist;
	HashMap<Integer, Integer> vertexDistanceMap;

	
	private static final int distScale = 100000;
	long debugTime = 0;
	long itemAccessed = 0;
	
	
	
	// The class UpperScore is to record the upper bound score of each candidate 
	private class UpperScore implements Comparable<UpperScore>{
		public int docID;
		public float score;
		public UpperScore(int docID, float score)
		{
			this.docID = docID;
			this.score = score;
		}
		
		@Override
		public int compareTo(UpperScore arg0) {
			return new Float(arg0.score).compareTo(score);
		}
		
		@Override
		public boolean equals(Object o) 
		{
		    if (o instanceof UpperScore) 
		    {
		    	UpperScore c = (UpperScore) o;
		    	if ( this.docID == c.docID){ 
		    		return true;
		    	}
		    }
		    return false;
		}
		
		@Override
		public String toString()
		{
			return "("+docID+","+score+")";
		}
	}
	
	
	
	
	
	
	
	public CASpatialTopK(Vector<Vector<ItemSpatialDoc>> docLists, Vector<Vector<ItemTextual>> textLists, Vector<Vector<ItemSpatialCurve>> spatialLists,  Point qLoc, int K, HashMap<Integer, Integer> vertexDistanceMap, Vector<ItemSpatialCurve> gtreeSpatiallist)
	{
		this.kwdNum = docLists.size();
		this.docLists = docLists;
		this.textLists = textLists;
		this.spatialLists = spatialLists;
		this.qLoc = qLoc;
		this.vertexDistanceMap=vertexDistanceMap;
		this.gtreeSpatiallist=gtreeSpatiallist;
		// sort the spatial lists by the distance
		for(int i=0;i < kwdNum;i++){
			for(int j=0;j < spatialLists.get(i).size();j++){
				spatialLists.get(i).get(j).zorder = (int)(distScale * qLoc.getDist(spatialLists.get(i).get(j).point)); 
			}
			Collections.sort(spatialLists.get(i));
		}	
		
		// initialize the four most important data structures in CA algorithm
		candidates = new HashMap<Integer, Candidate>();
		topkResults = new PriorityQueue<Candidate>();
		upperBounds = new PriorityQueue<UpperScore>();
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



		
	
		

	}
	
	
	
	
	public void run()
	{
		do{
			exploreLists();
			randomAccess();
			//printDebug();
		}while(!finish());
	}
	
	
	
	
	
	/*
	 * The algorithm terminates when the value of topk is larger than the object with minimum upper bound.
	 * 
	 */
	private boolean finish()
	{
		return upperBounds.size() == 0 || topkResults.peek().score >= upperBounds.peek().score;
	}
	
	
	
	/*
	 * For each textual inverted list, move forward until reach the minTextRelevance
	 * 
	 */
	private void exploreLists()
	{
		for(int j=0;j < H;j++){
			for(int i=0;i < kwdNum;i++){
				if(pos < textLists.get(i).size()){
					ItemTextual doc = textLists.get(i).get(pos);
					if(doc.docID == 1905000){
						System.out.println("debug");
					}
					float score = Scorer.getTextScore(doc.textRelevance);
					float topk=topkResults.peek().score;
					Candidate cand = candidates.get(doc.docID);
					if(cand == null){
						cand = new Candidate(doc.docID,kwdNum,doc.vertexID);
						cand.addTextScore(i, score);
						candidates.put(doc.docID, cand);
					}else{
						if(( (cand.flag.RightMoveStepsForNew(1+i).andAtIndex(0))) == 0){
							cand.addTextScore(i, score);
						}
					}
					
					
					if(cand.score > topk){
						updateTopk(cand);
					}
					updateUpperBound(cand);
					
					high[i] = score;
					itemAccessed++;
				}
			}
			
			float maxScore = 1;
			
//			if(p)
			for(int i=0;i < kwdNum;i++){
				if(pos < spatialLists.get(i).size()){
					ItemSpatialCurve doc = spatialLists.get(i).get(pos);
					if(doc.docID == 1905000){
						System.out.println("debug");
					}
					int distance=-1;
					if(vertexDistanceMap.containsKey(doc.VertexID))
					{
						distance=vertexDistanceMap.get(doc.VertexID);
						
					}
					
					float score=Scorer.getGtreeSpatialSocre(distance);
//					float score = Scorer.getDistScore(qLoc.getDist(doc.point));
					maxScore = Math.min(maxScore, score);
					
					float topk=topkResults.peek().score;
					Candidate cand = candidates.get(doc.docID);
					if(cand == null){
						cand = new Candidate(doc.docID,kwdNum,doc.VertexID);
						cand.addSpatialScore(score);
						cand.seeTextFromSpatial(i);			
						candidates.put(doc.docID, cand);
					}else{
						if( (cand.flag.andAtIndex(0)) == 0){
							cand.addSpatialScore(score);
						}	
						cand.seeTextFromSpatial(i);
					}
					
					if(cand.score > topk){
						updateTopk(candidates.get(doc.docID));
					}
					updateUpperBound(candidates.get(doc.docID));
					itemAccessed++;
					
					
				}
			}
			high[kwdNum] = maxScore;
			
			pos++;
			
		}
		
	}
	
	
	
	/*
	 * When the worst score of a candidate is larger than top-k, we need to update the topkResults
	 * 
	 */
	private void updateTopk(Candidate cand)
	{
		if(topkResults.contains(cand)){
			// remove the one with old score and add it again. The equal() function only compares with the docID
			topkResults.remove(cand);	
			topkResults.add(cand);
		}else{
			topkResults.poll();
			topkResults.add(cand);
		}
	}
	
	
	/*
	 * When we access an item and its upper bound is larger than topkScore, we need to monitor its upper bound score.
	 * 
	 */
	private void updateUpperBound(Candidate cand)
	{
		if(! cand.accessAll(kwdNum)){
			UpperScore bound = new UpperScore(cand.docId, getUpperBound(cand));
			if(bound.score > topkResults.peek().score){
				upperBounds.add(bound);
			}
		}

	}
	
	
	private float getUpperBound(Candidate cand)
	{
		if(cand.docId == 22126){
			System.out.println("debug");
		}
		float bound = cand.score;
		for(int i=0;i < kwdNum;i++){
			if((cand.flag.andAtIndex(1+i)) == 0){
				bound += high[i];
			}
		}
		if( (cand.flag.andAtIndex(0)) == 0){
			bound += high[kwdNum];
		}
		return bound;
	}
	
	
	private void randomAccess()
	{
		
		Candidate cand = null;
		float topk = topkResults.peek().score;
		while(upperBounds.size() > 0){
			cand = candidates.get(upperBounds.poll().docID);
			if(!cand.accessAll(kwdNum) && getUpperBound(cand) > topkResults.peek().score){
				break;
			}
		}
	
		fillCandidateScore(cand);
		if(cand.score > topk){
			updateTopk(cand);
		}
		
	}
	
	
	/*
	 * A candidate may only contain partial results. Fill it to be a confirmed top-k
	 * score.
	 */
	private void fillCandidateScore(Candidate cand)
	{
		if(cand.docId == 1905000){
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
				if( (cand.flag.andAtIndex(1+j)) == 0 ){
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
		System.out.println(topkResults);
		System.out.println(upperBounds);
		for(int i=0;i <= kwdNum;i++){
			System.out.println(high[i]);
		}
		System.out.println(upperBounds.size());
	}
	
}
