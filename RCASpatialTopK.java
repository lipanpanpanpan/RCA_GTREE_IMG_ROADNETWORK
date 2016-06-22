



import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Vector;

import search.Scorer;
import element.ItemSpatialCurve;
import element.ItemSpatialDoc;
import element.ItemTextual;
import element.query_processing.Candidate;
import element.spatial.Grid;
import element.spatial.Point;

public class RCASpatialTopK {
	private int kwdNum;
	private Point qLoc;
	private int qZorder;
	private Grid grid;
	private int iterationNum = 100;
	private double expandDist = 0;
//	private double radius = 0;
	private int tmpMaxDistance=0;
	
	private int numOfGtreeEachTime=20;
	

	private double upperBound;
	private double[] maxTextScore;
	private double maxSpatialScore;
	private int[] tIndex;
	private int[] sForwardIndex;
	private int[] sBackwardIndex;
	
	private Vector<Vector<DocBuf>> spatialBuf;
	private float[] minTextScore;
	

	
	private HashMap<Integer, Candidate> candidates;
	private PriorityQueue<Candidate> topkResults;
	

	
	private Vector<Vector<ItemSpatialDoc>> docLists; 
	private Vector<Vector<ItemTextual>> textLists;
	private Vector<Vector<ItemSpatialCurve>> spatialLists;
	
	private HashMap<Integer, Integer> vertexDistanceMap;
	private Vector<ItemSpatialCurve> gtreeSpatialList;
	
	long debugTime = 0;
	long itemAccessed = 0;
	
	
	
	
	private class DocBuf implements Comparable<DocBuf>{
		public int docID;
		public float score;
		public int kwd;
		public int vertexID;
		public DocBuf(int docID, float score, int kwd,int vertexID)
		{
			this.docID = docID;
			this.score = score;
			this.kwd = kwd;
			this.vertexID=vertexID;
		}
		
		@Override
		public String toString()
		{
			return "("+docID+","+score+","+kwd+")";
		}

		@Override
		public int compareTo(DocBuf arg0) {
			return new Integer(docID).compareTo(arg0.docID);
		}
		


		@Override
		public boolean equals(Object o) 
		{
		    if (o instanceof DocBuf) 
		    {
		    	DocBuf c = (DocBuf) o;
		      if ( this.docID == c.docID) 
		         return true;
		    }
		    return false;
		}


	}
	
	public RCASpatialTopK(Vector<Vector<ItemSpatialDoc>> docLists, Vector<Vector<ItemTextual>> textLists, Vector<Vector<ItemSpatialCurve>> spatialLists,  Grid grid, Point qLoc, int K, HashMap<Integer, Integer> vertexDistanceMap, Vector<ItemSpatialCurve> gtreeSpatiallist)
	{
		this.kwdNum = docLists.size();
		this.docLists = docLists;
		this.textLists = textLists;
		this.spatialLists = spatialLists;
		this.vertexDistanceMap=vertexDistanceMap;
		this.gtreeSpatialList=gtreeSpatiallist;
//		numOfGtreeEachTime=gtreeSpatialList.size()/iterationNum;
		this.grid = grid;
		this.qLoc = qLoc;
		this.qZorder = grid.getIndex(qLoc.getX(), qLoc.getY());
		
		upperBound = Double.MAX_VALUE;
		candidates = new HashMap<Integer, Candidate>();
		
		maxTextScore = new double[kwdNum];
		topkResults = new PriorityQueue<Candidate>();
		tIndex = new int[kwdNum];
		sForwardIndex = new int[kwdNum];
		sBackwardIndex = new int[kwdNum];
		//spatialBuf = new Vector<Vector<Vector<DocBuf>>>(kwdNum);
		
		minTextScore = new float[kwdNum];
		spatialBuf = new Vector<Vector<DocBuf>>(iterationNum);
		for(int j=0;j < iterationNum;j++){
			spatialBuf.add(new Vector<DocBuf>());
		}
	
		
		// put dummy objects to topkResults and upperBounds
		for(int i=0;i < K;i++){
			topkResults.add(new Candidate(-1,-1,-1));
		}
		
		//expandDist = Scorer.getDistFromScore((1.0f/iterationNum)*(1-Scorer.alpha)/Scorer.alpha);
		expandDist = Scorer.MAX_SPATIAL_DIST / iterationNum;
		//expandDist = 5;
	}
	
	
	
	/**
	 * 锟斤拷锟絊patiallist之锟斤拷喜锟斤拷锟紸nd锟斤拷锟斤拷
	 */
	@SuppressWarnings("unchecked")
	public  void  SpatialListsIncorporate()
	{
		/**
		 * 锟斤拷锟絣ist锟狡讹拷Flag
		 */
		int[] listFlag=new int[spatialLists.size()];
		
		//锟狡讹拷之前锟斤拷锟斤拷锟斤拷
		
		
		for(int i=0;i<spatialLists.size();i++)
		{
			//print
			if(i==0)
			{
			
				Vector<ItemSpatialCurve> item=new Vector<ItemSpatialCurve>();
				item=(Vector<ItemSpatialCurve>) spatialLists.get(i).clone();
				System.out.println("锟斤拷锟斤拷锟斤拷锟斤拷"+item.size());
				for(int j=0;j<item.size()&&j<10;j++)
				{
						System.out.println(item.get(j).toString());
				}
			}
		}
	}
	
	

	
	/**
	 * 锟斤拷询锟斤拷锟斤拷始
	 */
	public void run()
	{
		init();
		tmpMaxDistance=(int) Scorer.MAX_SPATIAL_GTREE_DIST;
		System.out.println("numOfGtreeEachTime:"+numOfGtreeEachTime);
		for(int i=0;i < iterationNum;i++){
			
			//
			exploreTextLists();
			for(int j=i*numOfGtreeEachTime;j<(i+1)*numOfGtreeEachTime&&j<gtreeSpatialList.size();j++)
			{
				ItemSpatialCurve doc = gtreeSpatialList.get(j);
				
				float score=Scorer.getGtreeSpatialSocre(doc.zorder);

					aggregateSpacialDoc(doc.docID, score, 0,doc.VertexID);
					itemAccessed++;
				tmpMaxDistance=tmpMaxDistance>gtreeSpatialList.get(j).zorder?tmpMaxDistance:gtreeSpatialList.get(j).zorder;
			}
//			exploreBackwardSpatialLists();
//			exploreForwardSpatialLists();


				

		
			itemAccessed += spatialBuf.get(i).size();

			confirmTopk();
			updateUpperBound();
			tmpMaxDistance=(int) Scorer.MAX_SPATIAL_GTREE_DIST;
//			System.out.println("ite "+i);
			if(finish()){
				refineAllCandidates();
				break;
			}
		
			//System.out.println("=============  Iteration "+i+" (radius)"+radius+"  =================");
			//printDebug();
		}

	}
	
	
	/*
	 * Init the starting location of pointers for the text and spatial domain. For text lists, we start
	 * from the first element in the list. For spatial lists, we start from the one closest to the qZorder;
	 * 
	 */
	private void init()
	{
		
//		System.out.println(spatialLists.size()+"   sdsada");
		for(int i=0;i < kwdNum;i++){
			tIndex[i] = 0;
			
//			System.out.println(i);
			//System.out.println(spatialLists.get(i));
//			sForwardIndex[i] = Collections.binarySearch(spatialLists.get(i), new ItemSpatialCurve(-1, qZorder,null,-1));
//			if(sForwardIndex[i] < 0){
//				sForwardIndex[i] = -sForwardIndex[i]-1;
//			}
//		
//			sBackwardIndex[i] = sForwardIndex[i]-1;
			sForwardIndex[i]=1;
			sBackwardIndex[i] = sForwardIndex[i]-1;
			
//			System.out.println(i+"ge:"+sBackwardIndex[i]);
			minTextScore[i] = textLists.get(i).firstElement().textRelevance - 1.0f/iterationNum;
			//minTextScore[i] = 1.0f - 1.0f/iterationNum;
		}
		
		
	}
	
	
	/*
	 * The algorithm terminates when the value of topk is larger than the minTextRelevance.
	 * upper bound score of the unvisited items.
	 * 
	 */
	private boolean finish()
	{
		return topkResults.peek().score >= upperBound;
	}
	
	
	
	/**
	 * For each textual inverted list, move forward until reach the minTextRelevance
	 * 锟斤拷锟斤拷每锟斤拷锟侥憋拷锟斤拷锟脚�前锟斤拷直锟斤拷锟斤拷锟斤拷minTextRelevance
	 */
	private void exploreTextLists()
	{
		for(int i=0;i < textLists.size();i++){
			while(tIndex[i] < textLists.get(i).size()){
				ItemTextual doc = textLists.get(i).get(tIndex[i]);
				
				if(doc.textRelevance < minTextScore[i]){
					break;
				}
				
				if(doc.docID == 14466618){
					System.out.println("debug");
				}
				
				float topk=topkResults.peek().score;
				float score = Scorer.getTextScore(doc.textRelevance);
				Candidate cand = candidates.get(doc.docID);
				if(cand == null){
					cand = new Candidate(doc.docID,kwdNum,doc.vertexID);
					cand.addTextScore(i, score);
					candidates.put(doc.docID, cand);
				}else{
					
					//2014-7-29 14:55:23
					//
					if(( (cand.flag.RightMoveStepsForNew(1+i)).andAtIndex(0)) == 0){
						cand.addTextScore(i, score);
					}
				}
				if(cand.score > topk){
					updateTopk(cand);
				}
				tIndex[i]++;
				itemAccessed++;
			}
			minTextScore[i] -= 1.0f/iterationNum;
		}
	}
	
	
	
	
	
	/*
	 * For each spatial inverted list, we move forward the list
	 * 
	 */
	private void exploreForwardSpatialLists()
	{
//		System.out.println("hehe exploreForwardSpatialLists");
		for(int i=0;i < spatialLists.size();i++){
			int countII=0;
			while(sForwardIndex[i] < spatialLists.get(i).size()){
				if(countII>numOfGtreeEachTime)
				{
					break;
				}
				
				countII++;
				ItemSpatialCurve doc = spatialLists.get(i).get(sForwardIndex[i]);
//				if(doc.zorder > maxOrder){
//					break;
//				}
				if(doc.docID == 14466618){
					System.out.println("debug");
				}
				
//				double dist = qLoc.getDist(doc.point);
				
				int distance=-1;
				if(vertexDistanceMap.containsKey(doc.VertexID))
				{
					distance=vertexDistanceMap.get(doc.VertexID);
					tmpMaxDistance=tmpMaxDistance<distance?tmpMaxDistance:distance;
				}
				
				float score=Scorer.getGtreeSpatialSocre(distance);
//				float score = Scorer.getSpatialScore(qLoc, doc.point);
				
				//docLocs.put(doc.docID, doc.point);
				
//				if(dist <= radius){
					aggregateSpacialDoc(doc.docID, score, i,doc.VertexID);
					itemAccessed++;
//				}else{
//					int bucket = (int)(dist/expandDist);
//					bucket = bucket >= iterationNum ? iterationNum-1 : bucket;
//					spatialBuf.get(bucket).add(new DocBuf(doc.docID, score, i,doc.VertexID));
//				}
			
				sForwardIndex[i]++;
			}
		}
		
	}
	
	
	
	/*
	 * For each spatial inverted list, we move backward the list
	 * 
	 */
	private void exploreBackwardSpatialLists()
	{
//		System.out.println("hehe exploreBackwardSpatialLists");
		for(int i=0;i < spatialLists.size();i++){
			int countII=0;
			while(sBackwardIndex[i] >= 0){
				
				if(countII>numOfGtreeEachTime)
				{
					break;
				}
				
				countII++;
				ItemSpatialCurve doc = spatialLists.get(i).get(sBackwardIndex[i]);
				//docLocs.put(doc.docID, doc.point);
//				if(doc.zorder < minOrder){
//					break;
//				}
				if(doc.docID == 14466618){
					System.out.println("debug");
				}
//				double dist = qLoc.getDist(doc.point);
//				float score = Scorer.getSpatialScore(qLoc, doc.point);
				int distance=-1;
				if(vertexDistanceMap.containsKey(doc.VertexID))
				{
					distance=vertexDistanceMap.get(doc.VertexID);
					tmpMaxDistance=tmpMaxDistance<distance?tmpMaxDistance:distance;
				}
				
				float score=Scorer.getGtreeSpatialSocre(distance);
				
				
				
//				if(dist <= radius){
					aggregateSpacialDoc(doc.docID, score, i,doc.VertexID);
					itemAccessed++;
//				}else{
//					int bucket = (int)(dist/expandDist);
//					bucket = bucket >= iterationNum ? iterationNum-1 : bucket;
//					spatialBuf.get(bucket).add(new DocBuf(doc.docID, score, i,doc.VertexID));
//				}
				
				sBackwardIndex[i]--;
			}
		}
	}
	
	private void aggregateSpacialDoc(int docID, float score, int kwd,int vertexID)
	{
		if(docID == 14466618){
			System.out.println("debug");
		}
		Candidate cand = candidates.get(docID);
		float topk=topkResults.peek().score;
		if(cand == null){
			
			cand = new Candidate(docID,kwdNum,vertexID);
			cand.addSpatialScore(score);
			for(int i=0;i<kwdNum;i++)
			{
				cand.seeTextFromSpatial(kwd);
			}
			
			candidates.put(docID, cand);
			
		}else{
			if( (cand.flag.andAtIndex(0)) == 0){
				cand.addSpatialScore(score);
			}	
			
			for(int i=0;i<kwdNum;i++)
			{
				cand.seeTextFromSpatial(kwd);
			}
			
		}
		if(cand.score > topk){
			updateTopk(cand);
		}
	}
	
	
	private void updateUpperBound()
	{
		maxSpatialScore=Scorer.getGtreeSpatialSocre(tmpMaxDistance);
		upperBound = maxSpatialScore;
		for(int i=0;i < kwdNum;i++){
			if(tIndex[i] < textLists.get(i).size()){
				maxTextScore[i] = Scorer.getTextScore(textLists.get(i).get(tIndex[i]).textRelevance);
				upperBound += maxTextScore[i];
			}
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
	 * In the sortCandiateBuf, the score is the lower bound of the real score. We need
	 * to confirm whether it is the real score. If not, we will fetch its missing partial
	 * score in other inverted lists.
	 * 
	 */
	private void confirmTopk()
	{
		PriorityQueue<Candidate> tmpResults = new PriorityQueue<Candidate>();
		while(topkResults.size() > 0){
			Candidate cand = topkResults.poll();
			fillCandidateScore(cand);
			tmpResults.add(cand);
		}
		topkResults = tmpResults;
		
	}
	
	
	
	private void refineAllCandidates()
	{
		//printDebug();
		for(Candidate cand: topkResults){
			cand.iteration = 1;
		}
		int partial=0;
		System.out.println(candidates.size());
		for(Integer docID : candidates.keySet()){
			Candidate cand = candidates.get(docID);
			if(cand.docId == 14466618){
				System.out.println("debug");
			}
			if(cand.iteration == 0){
				if(!cand.accessAll(kwdNum)){
					double maxScore = cand.score;
					
					if( (cand.flag.andAtIndex(0)) == 0){
						// If the spatial attribute has not been seen, we aggregate all the possible cases.
						maxScore += maxSpatialScore;
						for(int j=0;j < kwdNum;j++){
							if( (cand.flag.andAtIndex(1+j)) == 0){
								maxScore += maxTextScore[j];
							}
						}
					}else{
						// If the spatial attribute has been accessed, we only need to aggregate the score
						// which is set by the keyword in the Candidate
						for(int j=0;j < kwdNum;j++){
							if( (cand.flag .andAtIndex(1+j)) == 0 && (cand.keyword.andAtIndex(1+j)) != 0){
								maxScore += maxTextScore[j];
							}
						}
					}
					
					if(maxScore > topkResults.peek().score){
						fillCandidateScore(cand);
						partial++;
						if(cand.score > topkResults.peek().score){
							updateTopk(cand);
						}
					}
				}else{
					
					if(cand.score > topkResults.peek().score){
						updateTopk(cand);
					}
				}
			}
		}
//		System.out.println("partial : "+partial);
	}
	
	
	/*
	 * A candidate may only contain partial results. Fill it to be a confirmed top-k
	 * score.
	 */
	private void fillCandidateScore(Candidate cand)
	{

		
		//System.out.println(cand);
		// First check if the spatial attribute is accessed
		if( (cand.flag.andAtIndex(0)) == 0){
			
			// There must be at least one of the keyword is set 1. Otherwise, the score 
			// would be zero and will not appear in top-k 
			for(int j=0;j < kwdNum;j++){
				if( (cand.flag.andAtIndex(1+j)) != 0){
					int index = Collections.binarySearch(docLists.get(j), new ItemSpatialDoc(cand.docId,0,null,cand.vertexID));
					if(index > 0){
						
						int distance=-1;
						if(vertexDistanceMap.containsKey(cand.vertexID))
						{
							distance=vertexDistanceMap.get(cand.vertexID);
							tmpMaxDistance=tmpMaxDistance>distance?tmpMaxDistance:distance;
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
				if( (cand.flag.andAtIndex(1+j)) == 0 && (cand.keyword.andAtIndex(1+j)) != 0){
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
		System.out.println("======================================");
		System.out.print("Terminate condition : " + topkResults.peek().score + " >= "+ upperBound+"  (");
		for(int i=0;i < kwdNum;i++){
			System.out.print(maxTextScore[i]+"+");
		}
		System.out.println(maxSpatialScore + ")");
		
		System.out.println(candidates.size() + "visited items");
		
		System.out.println("Textual Expand :");
		for(int i=0;i < kwdNum;i++){
			System.out.println("[0, "+textLists.get(i).size()+"] => "+tIndex[i]);
		}
		System.out.println("Spatial Expand :");
		for(int i=0;i < kwdNum;i++){
			System.out.println("[0, "+spatialLists.get(i).size()+"] => ["+sBackwardIndex[i]+","+sForwardIndex[i]+"]");
		}
	}
}
