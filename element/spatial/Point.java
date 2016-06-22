package  element.spatial;

import java.io.Serializable;

/**
 * We are only handling two dimension data in this project.
 * 
 * @author dongxiang
 *
 */
public class Point implements Serializable{

	private static final long serialVersionUID = 7499834602526675401L;
	
	public Point(double x, double y)
	{
		_x = x;
		_y = y;
	}
	
	public double getX()
	{
		return _x;
	}
	
	public double getY()
	{
		return _y;
	}
	
	public double getDist(Point other)
	{
		return Math.sqrt((_x-other._x)*(_x-other._x) + (_y-other._y)*(_y-other._y));
	}
	
	@Override
	public String toString()
	{
		return "<"+_x+","+_y+">";
	}
	
	double _x;
	double _y;
}
