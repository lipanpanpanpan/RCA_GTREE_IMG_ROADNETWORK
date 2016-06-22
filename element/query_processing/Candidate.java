package element.query_processing;

import bitVector.CBitArray;




public class Candidate implements Comparable<Candidate>, HeapItem
{
	/**
	 * maskĿǰ�õ��ĵط�ֻ��У���Ƿ�ȫ���õ�
	 */
	private static final char[] masks = {1, 3, 7, 15, 31, 63, 127, 255};
	
	public CBitArray keyword;
	public CBitArray flag;
	public float score;	// The score is a lower bound of the real score unless all the query bits have been set.
	public int docId;
	public int iteration;	// The score of a candidate changes with more and more iterations.
	public int vertexID;

	public Candidate(int docId,int kwdNums,int vertexID)
	{
		this.docId = docId;
		score = 0f;
		flag = new CBitArray(kwdNums+1);
		keyword= new CBitArray(kwdNums+1);
		iteration=0;
		this.vertexID=vertexID;
	}
	
	public Candidate(int docId, float score,int vertexID)
	{
		this.docId = docId;
		this.score = score;
		this.vertexID=vertexID;
	}
	
	public Candidate(Candidate other)
	{
		this.docId = other.docId;
		this.score = other.score;
	
		this.flag = other.flag;
		this.vertexID=other.vertexID;
//		System.err.println("��ȡ����FlagֵΪ��"+this.flag);
	}
	
	
	
	public void copy(Candidate other)
	{
		this.docId = other.docId;
		this.score = other.score;
		this.flag = other.flag;
		this.vertexID=other.vertexID;
	}
	
	public int getId()
	{
		return docId;
	}
	
	public float getScore()
	{
		return score;
	}
	
	/*
	 * Only if a candidate has accessed all the related items, its score is accurate.
	 * 
	 */
	public boolean accessAll(int kwdNum)
	{
//		System.out.println("kwdNum"+kwdNum);
//		System.err.println("��ǰflagֵΪ��\t"+flag);
		return flag.CompareWithMask(kwdNum);
//		return true;
	}
	
	public void addTextScore(int kwd, float score)
	{
		this.score += score;
		seeText(kwd);
	}
	
	public void addSpatialScore(float score)
	{
		
		if((flag.andAtIndex(0)) == 0){
			this.score += score;
			seeSpatial();
		}
	}
	
	public void seeAll()
	{
		flag.SetAllOne();
	}
	
	// If the candidate has known the precise score of a query keyword
	public void seeText(int kwd)
	{
//		flag |= 0x1 << (1+kwd);
		flag.orAtIndex(kwd+1);
	}
	
	public void seeTextFromSpatial(int kwd)
	{
//		keyword |= 0x1 << (1+kwd);
//		System.out.println(kwd+"   jkwd");
		keyword.orAtIndex(kwd+1);
	}
	
	// If the candidate has know the precise score of its location
	public void seeSpatial()
	{
//		flag |= 1;
		flag.orAtIndex(0);
	}
	
	public boolean spatialAccessed()
	{
//		return (flag & 1) == 1;
		return flag.andAtIndex(0)==1;
	}
	
	
	
	public int getAccessCount()
	{
		CBitArray tmp = flag.copyOne();
		int count = 0;
		for(int i=0;i < 8;i++){
			if( (tmp.andAtIndex(0)) == 1){
				count++;
			}
//			tmp >>= 1;
			tmp.RightMoveSteps(1);
		}
		return count;
	}
	
	private String getFlag()
	{
		CBitArray tmp = flag;
		String str = "";
		for(int i=0;i <flag.m_v.size();i++){
			if( (tmp.andAtIndex(0)) == 1){
				str += "1";
			}else{
				str += "0";
			}
//			tmp >>= 1;
			tmp.RightMoveSteps(1);
		}
		return str;
	}
	
	@Override
	public boolean equals(Object o) 
	{
	    if (o instanceof Candidate) 
	    {
	    	Candidate c = (Candidate) o;
	    	if ( this.docId == c.docId){ 
	    		return true;
	    	}
	    }
	    return false;
	}
	
	
	@Override
	public String toString()
	{
		return "(" + docId + ", " + score + ", "+getFlag() + "\t"+iteration+")";
	}

	@Override
	public int compareTo(Candidate other) {
		return new Float(score).compareTo(other.score);
	}
}
