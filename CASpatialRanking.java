

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import conf.Configure;

import search.Scorer;
import test.IOCounter;
import test.InputFileIO;
import element.ItemSpatialCurve;
import element.ItemSpatialDoc;
import element.ItemTextual;
import element.SpatialDoc;
import element.spatial.Grid;
import element.spatial.Point;

public class CASpatialRanking {
	Grid grid;												// To get the z-order
	final String DBName;
	DB db;
	
	HTreeMap<String, Vector<ItemSpatialDoc>> docMap ;
	HTreeMap<String, Vector<ItemTextual>> textualMap;
	HTreeMap<String, Vector<ItemSpatialCurve>> spatialMap;
	
	HashMap<String, Vector<ItemSpatialDoc>> docLists;
	HashMap<String, Vector<ItemTextual>> textualLists;
	HashMap<String, Vector<ItemSpatialCurve>> spatialLists;
	
	long queryTime = 0;
	long indexLoadTime = 0;
	long itemAccessed = 0;
	long totalItem = 0;
	GTreeAPI gtreeAPI;
	public CASpatialRanking(String dbName)
	{
		DBName = dbName;
		grid = new Grid(0f, 10000f, 0f, 10000f, (short)2000);
		
		loadDB();
		docMap = db.getHashMap("docMap");
		textualMap = db.getHashMap("textualMap");
		spatialMap = db.getHashMap("spatialMap");
		
		docLists = new HashMap<String, Vector<ItemSpatialDoc>>();
		textualLists = new HashMap<String, Vector<ItemTextual>>();
		spatialLists = new HashMap<String, Vector<ItemSpatialCurve>>();
		gtreeAPI=new GTreeAPI();
	}
	
	
	
	public void query(String queryFile, int K) throws IOException
	{
		int queryNum = 0;
		int termCount=0;
		try{
			InputFileIO fin = new InputFileIO(queryFile);	
			SpatialDoc doc = null;
			
			while( (doc=fin.nextSpatialDocument()) != null&&queryNum<100){
				//if(doc.getID() == 3){
					System.out.println(queryNum+"\t"+doc);
					long start = System.currentTimeMillis();
					int extraTime=query(doc.getLoc(), new Vector<String>(doc.getTerms().keySet()), K,doc.vertexID);
					long end = System.currentTimeMillis();
					queryTime += end - start-extraTime;
					termCount=doc.getTerms().size();
	
					queryNum++;
					//break;
				//}
				
				//break;
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		totalItem *= 2;
		System.out.println(itemAccessed);
		System.out.println(totalItem);
		System.err.println((1.0*queryTime/queryNum) + "\t" + (1.0*indexLoadTime/queryNum) + "\t" + (1.0*itemAccessed)/totalItem) ;
		FileWriter fw=new FileWriter(Configure.DATA_ROOT_FOLDER_PATH+Configure.DATA_QUERY_RESULT_FOLDER_PATH+Configure.DATA_QUERY_RESULT_DATA_CA_FOLDER_PATH+Configure.LAB_TYPE+termCount+"_"+Configure.A_OF＿SPATIAL+"_"+Configure.TOP_K+"_"+Configure.DATA_NUM_OF_INDEX+".txt");
		
		
		Double avgQueryTime=1.0*queryTime/queryNum;
		Double avgIndexLoaded=1.0*indexLoadTime/queryNum;
		Double avgItemAccessed=(1.0*itemAccessed)/totalItem;
		Double avgIO=(IOCounter.io*1.0)/queryNum;
		
		fw.write("avgQueryTime\t"+avgQueryTime+"\r\n");
		fw.write("avgIndexLoaded\t"+avgIndexLoaded+"\r\n");
		fw.write("avgItemAccessed\t"+avgItemAccessed+"\r\n");
		fw.write("avgIO"+avgIO+"\r\n");
		fw.close();
	}
	
	
	
	private int query(Point qLoc, Vector<String> queryKwds, int K,int queryVertexID) {
		// Retrieve the bucket inveted lists
		long start = System.currentTimeMillis();
		int kwdNum = queryKwds.size();
		Vector<Vector<ItemSpatialDoc>> docLists = new Vector<Vector<ItemSpatialDoc>>();
		Vector<Vector<ItemTextual>> textLists = new Vector<Vector<ItemTextual>>();
		Vector<Vector<ItemSpatialCurve>> spatialLists = new Vector<Vector<ItemSpatialCurve>>();
		//Vertex distance From QueryLocation
		HashMap<Integer, Integer> vertexDistanceMap=new HashMap<Integer, Integer>();
		
		
		for(int i=0;i < kwdNum;i++){
			Vector<ItemSpatialDoc> list = docMap.get(queryKwds.get(i));
			if(list != null){
				docLists.add(list);
				textLists.add(textualMap.get(queryKwds.get(i)));
				spatialLists.add(spatialMap.get(queryKwds.get(i)));
				
				totalItem += textLists.lastElement().size();
			}
		}
		
		long end = System.currentTimeMillis();
		indexLoadTime += end - start;
		if(true)
		{
			System.out.println("loadTime");
			return 0;
		}
		//get the GTREE Distance For Each list
				for(int i=0;i<docLists.size();i++)
				{
					Vector<ItemSpatialDoc> list =docLists.get(i);
					for(int j=0;j<list.size();j++)
					{
						ItemSpatialDoc item=list.get(j);
						if(!vertexDistanceMap.containsKey(item.VertexID))
						{
							vertexDistanceMap.put(item.VertexID, -1);
						}
					}
				}
				int[] vertexsToGTree=new int[vertexDistanceMap.size()];
				int vertexToGTreeCount=0;
				for(Integer vertexID:vertexDistanceMap.keySet())
				{
					vertexsToGTree[vertexToGTreeCount]=vertexID;
					vertexToGTreeCount++;
				}
				
				VertexDist[] vertexDistanceFromGtree=gtreeAPI.getAllCanditateDistWithExtraTimeReturn(queryVertexID, vertexsToGTree);
				int extraTime=0;
				if(vertexDistanceFromGtree.length>0)
				{
					extraTime=vertexDistanceFromGtree[0].distance;
					System.err.println("ExtraTime"+extraTime);
				}
				for(int i=1;i<vertexDistanceFromGtree.length;i++)
				{

					vertexDistanceMap.put(vertexDistanceFromGtree[i].vertexID, vertexDistanceFromGtree[i].distance);
				}
				
				Vector<ItemSpatialCurve> GtreeSpatiallist=new Vector<ItemSpatialCurve>();
				//将spatialList里面的ZOrder变成GTree中的distance
				for(int i=0;i<kwdNum;i++)
				{
					for(int j=0;j<spatialLists.get(i).size();j++)
					{
						if(vertexDistanceMap.containsKey(spatialLists.get(i).get(j).VertexID))
						{
							spatialLists.get(i).get(j).zorder=vertexDistanceMap.get(spatialLists.get(i).get(j).VertexID);
							if(!GtreeSpatiallist.contains(spatialLists.get(i).get(j)))
							{
								GtreeSpatiallist.add(spatialLists.get(i).get(j));
							}
						}else
						{
							spatialLists.get(i).get(j).zorder=(int) Scorer.MAX_SPATIAL_GTREE_DIST;
							if(!GtreeSpatiallist.contains(spatialLists.get(i).get(j)))
							{
								GtreeSpatiallist.add(spatialLists.get(i).get(j));
							}
							System.out.println("shdiashdjkabnsdjknakj");
						}
					}
				}
				Collections.sort(GtreeSpatiallist);
				System.out.println(GtreeSpatiallist.size()+"Doc in all");
		// start CA algorithm
		//NewCASpatialTopK alg = new NewCASpatialTopK(docLists, textLists, spatialLists, qLoc, K);
		CASpatialTopK alg = new CASpatialTopK(docLists, textLists, spatialLists, qLoc, K,vertexDistanceMap,GtreeSpatiallist);
		alg.run();
//		alg.printResult();
		//System.out.println("item accessed "+ alg.getAccessedItem());
		itemAccessed += alg.getAccessedItem();
		return extraTime;
	}
	
	
	
	public void loadDB()
	{
		File dbFile = new File(DBName);
		db = DBMaker.newFileDB(dbFile)
				.transactionDisable()
				//.cacheDisable()
                .closeOnJvmShutdown()
                .make();
	}
	

	
	public static void main(String[] args) throws IOException
	{
		
		String dbName = Configure.DATA_ROOT_FOLDER_PATH+Configure.DATA_INDEX_FOLDER_PATH+Configure.DATA_INDEX_DB_NAME;
		String queryFile = Configure.DATA_ROOT_FOLDER_PATH+Configure.DATA_QUERY_FOLDER_PATH+Configure.DATA_QUERY_FILE_NAME;

		int K =Configure.TOP_K;
		Scorer.alpha = (float) Configure.A_OF＿SPATIAL;
		
		
		CASpatialTopK.H = 1;
		
		CASpatialRanking TATest = new CASpatialRanking(dbName);
		TATest.query(queryFile, K);
	}
}
