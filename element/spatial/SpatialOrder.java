package element.spatial;



/*
 * The SpatialOrder controls how the spatial points are sorted in an inverted list.
 * 
 */
public class SpatialOrder {
	private static Grid grid = null;
	
	public static void setgrid(Grid grid)
	{
		SpatialOrder.grid = grid;
	}
	
	public static float getOrder(Point point)
	{
		return grid.getMortonNumber(grid.getX(point._x), grid.getY(point._y));
	}
}
