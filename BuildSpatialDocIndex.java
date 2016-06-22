

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import conf.Configure;

import test.InputFileIO;

import element.ItemSpatialCurve;
import element.ItemSpatialDoc;
import element.ItemTextual;
import element.SpatialDoc;
import element.spatial.Grid;



public class BuildSpatialDocIndex {
	
	
	Grid grid;													// To get the z-order
	final String DBName;
	DB db;
	
	HashMap<String, Integer> kwdCount;
	

	HTreeMap<String, Vector<ItemSpatialDoc>> docMap ;
	HTreeMap<String, Vector<ItemTextual>> textualMap;
	HTreeMap<String, Vector<ItemSpatialCurve>> spatialMap;
	
	
	
	HashMap<String, Vector<ItemSpatialDoc>> docLists;
	HashMap<String, Vector<ItemTextual>> textualLists;
	HashMap<String, Vector<ItemSpatialCurve>> spatialLists;

	public BuildSpatialDocIndex(String dbName)
	{
		grid = new Grid(-180f, 10000f, -180f, 10000f, (short)2000);
		kwdCount = new HashMap<String, Integer>();
		DBName = dbName;
		
		createDB();
		
	
		
		docMap = db.getHashMap("docMap");
		textualMap = db.getHashMap("textualMap");
		spatialMap = db.getHashMap("spatialMap");
		
		
		
		
		docLists = new HashMap<String, Vector<ItemSpatialDoc>>();
		textualLists = new HashMap<String, Vector<ItemTextual>>();
		spatialLists = new HashMap<String, Vector<ItemSpatialCurve>>();
	}
	
	void getKwdCount(String inputFile)
	{
		try{
			InputFileIO fin = new InputFileIO(inputFile);
			SpatialDoc doc = null;
			int num=0;
			while( (doc=fin.nextSpatialDocument()) != null){
				if(num++%10000==1){
					System.out.println(num);
				}
				for(String kwd : doc.getTerms().keySet()){
					if(kwdCount.containsKey(kwd)){
						kwdCount.put(kwd, kwdCount.get(kwd)+1);
					}else{
						kwdCount.put(kwd, 1);
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void run(String inputFile)
	{
		long start,end;

		System.out.println("getKwdCount");
		start = System.currentTimeMillis();
		getKwdCount(inputFile);
		end = System.currentTimeMillis();
		System.out.println("getKwdCount Time: " + ((end - start) / 1000.0f) + "s");
		
		start = System.currentTimeMillis();
		build(inputFile);
		end = System.currentTimeMillis();
		System.out.println("Build Time: " + ((end - start) / 1000.0f) + "s");
		
		db.close();
		
		
	}
	
	
	
	public void build(String inputFile)
	{
		
	
		try{
			
			
			InputFileIO fin = new InputFileIO(inputFile);
			SpatialDoc doc = null;
			int listNum=0;
//			Boolean tmpFlag=true;
			
			while( (doc=fin.nextSpatialDocument()) != null){
				int hashValue = grid.getIndex(doc.getLoc().getX(), doc.getLoc().getY());
				for(String kwd : doc.getTerms().keySet()){
					//���û������ʣ��򴴽�һ����
					if(spatialLists.get(kwd) == null){
						docLists.put(kwd, new Vector<ItemSpatialDoc>(kwdCount.get(kwd)));
						spatialLists.put(kwd, new Vector<ItemSpatialCurve>(kwdCount.get(kwd)));
						textualLists.put(kwd, new Vector<ItemTextual>(kwdCount.get(kwd)));
						
					}


					docLists.get(kwd).add(new ItemSpatialDoc(doc.getID(),  doc.getTerms().get(kwd), doc.getLoc(),doc.vertexID));
					spatialLists.get(kwd).add(new ItemSpatialCurve(doc.getID(),  hashValue, doc.getLoc(),doc.vertexID));
					textualLists.get(kwd).add(new ItemTextual(doc.getID(),  doc.getTerms().get(kwd),doc.vertexID));
					
				
					//spatailLists�е�kwd��Ӧ������
					if(spatialLists.get(kwd).size() == kwdCount.get(kwd)){
						
						
						
						Collections.sort(spatialLists.get(kwd));
						Collections.sort(textualLists.get(kwd));
						
						
							docMap.put(kwd, docLists.get(kwd));
				        	spatialMap.put(kwd, spatialLists.get(kwd));
				        	textualMap.put(kwd, textualLists.get(kwd));
				        	spatialLists.remove(kwd);
				        	textualLists.remove(kwd);
				        	docLists.remove(kwd);
				        	listNum++;
					
						
						
			        	
			        	if(listNum % 1000 == 0){
			        		System.out.println("\t\t"+listNum + "/"+kwdCount.size());
			        	}
			        	
			        	if(listNum > 1000 && listNum % 100000 == 1){
							System.out.println("garbage collection");
							System.gc();
						}
					}
				}
				
				
				if(doc.getID() % 1000 == 1){
					System.out.println(doc.getID());
				}
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createDB()
	{
		System.out.println(DBName);
		 File dbFile = new File(DBName);
	        if(dbFile.exists()){
	        	dbFile.delete();
	        }
	        db = DBMaker.newFileDB(dbFile)
	        		//.asyncWriteEnable()
	        		.transactionDisable()
	        		.freeSpaceReclaimQ(0)
	        		.asyncWriteEnable()
	        		
	        		//.cacheSize(1024*1024*100)
	        		//.cacheLRUEnable()
	        		//.cacheDisable()
	        		//.fullChunkAllocationEnable()
	        		//.mmapFileEnable()
	        		//.syncOnCommitDisable()
	        		.make();
	                
	}
	

	
	public static void main(String[] args)
	{
		
		String dbName=Configure.DATA_ROOT_FOLDER_PATH+Configure.DATA_INDEX_FOLDER_PATH+Configure.DATA_INDEX_DB_NAME;
		
		String inputFile=Configure.DATA_ROOT_FOLDER_PATH+Configure.DATA_POI_FOLDER_PATH+Configure.DATA_POI_RCA_FILE_NAME;
		
		BuildSpatialDocIndex builder = new BuildSpatialDocIndex(dbName);
		builder.run(inputFile);
	}
}
