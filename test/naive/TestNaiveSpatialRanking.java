package test.naive;



import index.KwdInvItem;
import element.SpatialDoc;
import element.spatial.Point;


import java.util.Collections;
import java.util.Vector;


import search.Scorer;
import test.InputFileIO;


public class TestNaiveSpatialRanking {
	
	public TestNaiveSpatialRanking()
	{
	}
	
	private void query(String inputFile, Point qLoc, Vector<String> queryKwds, int K) {
		try{
			InputFileIO fin = new InputFileIO(inputFile);
			SpatialDoc doc = null;
			Vector<KwdInvItem> results = new Vector<KwdInvItem>();
			int id = 0;
			while( (doc=fin.nextSpatialDocument()) != null){
				if(id ++ % 10000 == 1){
					System.out.println("Scanning "+id);
				}
				float textualRelevance = 0.0f;
				for(String kwd : queryKwds){
					if(doc.getTerms().containsKey(kwd)){
						textualRelevance += doc.getTerms().get(kwd);
					}
				}

				if(textualRelevance > 0){
					//dist = qLoc.getDist(doc.getLoc());
					float score = (float)(Scorer.getSpatialScore(qLoc, doc.getLoc()) + Scorer.getTextScore(textualRelevance));
					if(id == 23480 ){
						System.out.println(qLoc);
						System.out.println(doc.getLoc());
						System.out.println(id+" : "+Scorer.getSpatialScore(qLoc, doc.getLoc())+"\t"+textualRelevance+"\t"+Scorer.getTextScore(textualRelevance)+"\t"+score);
					}
					results.add(new KwdInvItem(doc.getID(), score));
				}
				
			}
			Collections.sort(results);
			for(int i=0;i < K;i++){
				System.out.println(results.get(i));
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	
	public static void main(String[] args)
	{
		Point qLoc = new Point(51.51739501953125,-0.12799674272537231);
		Vector<String> queryKwds = new Vector<String>();

		//queryKwds.add("san");
		queryKwds.add("hotel");
		//queryKwds.add("antonio");

		TestNaiveSpatialRanking TATest = new TestNaiveSpatialRanking();
		TATest.query("twitter_0.1m.txt", qLoc, queryKwds, 50);
	}
}
