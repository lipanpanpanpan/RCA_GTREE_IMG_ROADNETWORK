package index;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;



/*
 * The BucketInvertedIndex is a compromise between DAAT and TAAT. In DAAT, all the elements in an
 * inverted list are sorted by the document id, which benefits the join procedure. However, the 
 * very relevant items may lie at the end of the list and get accessed very late. Similarly, in
 * DAAT, all the elements in an inverted list are sorted by the term relevance, but its join cost
 * is much higher. This is  not suitable for RNA algorithm to be applied in top-k search. Some real
 * top-k results may not get its full aggregation score but the algorithm has safely terminated.
 * 
 * The BucketInvertedIndex tries to combine the advantage of DAAT and TAAT. It partitions each inverted
 * list into buckets. We maintain summary information for each bucket to facilitate the join.
 * 
 *  
 */
public class BucketInvertedIndex implements Serializable{
	private static final long serialVersionUID = 7938205996163029802L;
	public static int BucketSize = 256;
	int bucketNum;
	Vector<InvListItem> invList;
	Vector<Signature> signatures;
	Vector<Integer> offsets;
	Vector<Float> maxScores;
	Vector<Float> minScores;

	public BucketInvertedIndex()
	{
		invList = new Vector<InvListItem>();
		signatures = new Vector<Signature>();
		maxScores = new Vector<Float>();
		minScores = new Vector<Float>();
		offsets = new Vector<Integer>();
	}
	
	public BucketInvertedIndex(int capacity)
	{
		invList = new Vector<InvListItem>(capacity);
		signatures = new Vector<Signature>(capacity/BucketSize+1);
		maxScores = new Vector<Float>(capacity/BucketSize+1);
		minScores = new Vector<Float>(capacity/BucketSize+1);
		offsets = new Vector<Integer>(capacity/BucketSize+1);
	}
	
	public int size()
	{
		return invList.size();
	}
	
	public Vector<InvListItem> getInvList()
	{
		return invList;
	}
	
	public InvListItem get(int index)
	{
		if(index >= invList.size()){
			return null;
		}
		return invList.get(index);
	}
	
	public int getBucketNum()
	{
		return bucketNum;
	}
	
	public int findClosestBucket(InvListItem item)
	{
		int index = Collections.binarySearch(invList, item);
		if(index < 0){
			index = -index-1;
		}
		return index / BucketSize;
	}
	
	public Iterator<InvListItem> getBucketIterator(int bucketId) {
		return invList.listIterator(BucketSize*bucketId);
	} 
	
	public void insert(InvListItem item)
	{
		invList.add(item);
	}
	
	
	public InvListItem getItem(int joinKey)
	{
		InvListItem item = null;
		for(int b=0;b < bucketNum;b++)
		{
			if(signatures.get(b).match(joinKey)){
				if( (item = getItemInBucket(joinKey, b)) != null){
					return item;
				}
			}
		}
		return null;
	}
	
	public InvListItem getItem(int joinKey, int startBucket, float minScore)
	{
		InvListItem item = null;
		for(int b=startBucket;b < bucketNum;b++)
		{
			if(maxScores.get(b) < minScore){
				return null;
			}
			
			if(signatures.get(b).match(joinKey)){
				if( (item = getItemInBucket(joinKey, b)) != null){
					return item;
				}
			}
		}
		return null;
	}
	
	public InvListItem firstElement()
	{
		return invList.get(0);
	}
	
	public InvListItem lastElement()
	{
		return invList.get(invList.size()-1);
	}
	
	/*
	 * If the joinKey exists in the bucket, return the score. Otherwise, 0 is returned.
	 * 
	 */
	public InvListItem getItemInBucket(int joinKey, int bucketId)
	{
		int start = offsets.get(bucketId);
		int end = (bucketId == bucketNum-1) ? invList.size()-1 : offsets.get(bucketId+1)-1;
		int mid;
		while(start <= end){
			mid = (start+end)/2;
			if(invList.get(mid).getJoinKey() == joinKey){
				return invList.get(mid); 
			}else if(invList.get(mid).getJoinKey() < joinKey){
				start = mid + 1;
			}else{
				end = mid - 1;
			}
		}
	    return null;
	}
	
	
	public float getMaxScore(int bucketId)
	{
		return maxScores.get(bucketId);
	}
	
	
	public float getMinScore(int bucketId)
	{
		return minScores.get(bucketId);
	}
	
	
	public Signature getSingature(int bucketId)
	{
		return signatures.get(bucketId);
	}
	
	public float getDistance(InvListItem item, int bucketId)
	{
		float dist = 0;
		if(item.getScore() < minScores.get(bucketId)){
			dist = minScores.get(bucketId) - item.getScore();
		}
		if(item.getScore() > maxScores.get(bucketId)){
			dist = item.getScore() - maxScores.get(bucketId);
		}
		
		return dist;
	}
	
	
	public void flush()
	{
		bucketNum = (int) Math.ceil(1.0f*invList.size() / BucketSize);
		for(int i=0;i < bucketNum;i++){
			maxScores.add(-100000f);
			minScores.add(100000f);
			signatures.add(new Signature());
			offsets.add(0);
		}

		
		Collections.sort(invList);
		Vector<Vector<InvListItem>> buckets = new Vector<Vector<InvListItem>>();
		buckets.setSize(bucketNum);

		for(int i=0;i < invList.size();i++){
			int bucketId = i/BucketSize;
			if(buckets.get(bucketId) == null){
				buckets.add(bucketId, new Vector<InvListItem>());
			}
			buckets.get(bucketId).add(invList.get(i));
		}
		
		invList.clear();
		for(int b=0;b < bucketNum;b++){
			if(buckets.get(b).size() > 0){
				minScores.add(b, buckets.get(b).lastElement().getSortVal());
				maxScores.add(b, buckets.get(b).firstElement().getSortVal());
			}
			for(InvListItem item : buckets.get(b)){
				signatures.get(b).add(item.getJoinKey());
			}
			if(b >= 1){
				offsets.add(b, offsets.get(b-1) + buckets.get(b).size());
			}
			Collections.sort(buckets.get(b), new DocIdComparator());
			invList.addAll(buckets.get(b));
		}
	}
	
	
	
	 private void writeObject(ObjectOutputStream out) throws IOException {
		 out.writeInt(bucketNum);
		 for(int b=0;b < bucketNum;b++){
			 out.writeFloat(minScores.get(b));
			 out.writeFloat(maxScores.get(b));
			 out.writeInt(offsets.get(b));
			 out.writeObject(signatures.get(b));
		 }
		 
		 out.writeInt(invList.size());
		 for(int i=0;i < invList.size();i++){
			 out.writeObject(invList.get(i));
		 }
	 }
	 
	 private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		 bucketNum = in.readInt();
		 minScores = new Vector<Float>(bucketNum);
		 maxScores = new Vector<Float>(bucketNum);
		 offsets = new Vector<Integer>(bucketNum);
		 signatures = new Vector<Signature>(bucketNum);
		 for(int b=0;b < bucketNum;b++){
			 minScores.add(b, in.readFloat());
			 maxScores.add(b, in.readFloat());
			 offsets.add(b, in.readInt());
			 signatures.add(b, (Signature)in.readObject());
		 }
		 
		 int itemNum = in.readInt();
		 invList = new Vector<InvListItem>(itemNum);
		 for(int i=0;i < itemNum;i++){
			 invList.add(i, (InvListItem)in.readObject());
		 }
	 }
	
	
	@Override
	public String toString()
	{
		String str = "\n--------------------------------------\n";
		str += bucketNum + "\n";
		
		for(int b=0;b < bucketNum;b++){
			if(! signatures.get(b).empty()){
				str += "bucket "+b+" : @pos "+offsets.get(b)+" ["+maxScores.get(b)+","+minScores.get(b)+"]  "+ signatures.get(b)+"\n";
			}
			
		}
		str += invList;
		return str;
	}
	
	
}
