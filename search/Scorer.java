package search;

import element.spatial.Point;

public class Scorer {
	//final static double  MAX_SPATIAL_DIST = new Point(-90,-180).getDist(new Point(90,180));
	public final static double  MAX_SPATIAL_DIST = 200;
	
	public final static double MAX_SPATIAL_GTREE_DIST=1000000000;
	public static float alpha = 0.7f;
	
	public static float getSpatialScore(Point qLoc, Point loc)
	{
		double dist = qLoc.getDist(loc);
		if(dist > MAX_SPATIAL_DIST){
			return 0;
		}
		return alpha*(1 - (float)(dist/MAX_SPATIAL_DIST));
	}
//	
	
	public static float getGtreeSpatialSocre(int distance)
	{
		
		
		if(distance > MAX_SPATIAL_GTREE_DIST){
			return 0;
		}
		if(distance<0)
		{
			return 0;
		}
		return alpha*(1 - (float)(distance/MAX_SPATIAL_GTREE_DIST));
		
	}

	
//	public static float getDistScore(double dist)
//	{
//		if(dist > MAX_SPATIAL_DIST){
//			return 0f;
//		}
//		return  alpha*(1 - (float)(dist/MAX_SPATIAL_DIST));
//	}
	
	public static double getDistFromScore(float score)
	{
		return MAX_SPATIAL_DIST*score;
	}
	
	public static float getTextScore(float tf)
	{
		return (1-alpha)*tf;
	}
}
