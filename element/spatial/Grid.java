package element.spatial;


import java.io.Serializable;
import java.util.Arrays;


/**
 * Grid is the simplest data structure to capture spatial information. In the NICHE project,
 * we prefer light-weighted indexes for better extensibility. Of course, the performance will
 * slightly degrade compared to more accurate spatial index, like R-Tree. 
 * 
 * The z-order looks like this:
 * -----------
 * \
 *  \
 *   \
 *    \
 *     \
 *      \
 *       \
 *        \
 *         \
 * ---------        
 * @author dongxiang
 *
 */
public class Grid implements Serializable {
	private static final long serialVersionUID = -1285709258104411495L;

	// ref:
	// http://graphics.stanford.edu/~seander/bithacks.html#InterleaveTableObvious
	static final short[] MortonTable256 = { 0x0000, 0x0001, 0x0004, 0x0005,
			0x0010, 0x0011, 0x0014, 0x0015, 0x0040, 0x0041, 0x0044, 0x0045,
			0x0050, 0x0051, 0x0054, 0x0055, 0x0100, 0x0101, 0x0104, 0x0105,
			0x0110, 0x0111, 0x0114, 0x0115, 0x0140, 0x0141, 0x0144, 0x0145,
			0x0150, 0x0151, 0x0154, 0x0155, 0x0400, 0x0401, 0x0404, 0x0405,
			0x0410, 0x0411, 0x0414, 0x0415, 0x0440, 0x0441, 0x0444, 0x0445,
			0x0450, 0x0451, 0x0454, 0x0455, 0x0500, 0x0501, 0x0504, 0x0505,
			0x0510, 0x0511, 0x0514, 0x0515, 0x0540, 0x0541, 0x0544, 0x0545,
			0x0550, 0x0551, 0x0554, 0x0555, 0x1000, 0x1001, 0x1004, 0x1005,
			0x1010, 0x1011, 0x1014, 0x1015, 0x1040, 0x1041, 0x1044, 0x1045,
			0x1050, 0x1051, 0x1054, 0x1055, 0x1100, 0x1101, 0x1104, 0x1105,
			0x1110, 0x1111, 0x1114, 0x1115, 0x1140, 0x1141, 0x1144, 0x1145,
			0x1150, 0x1151, 0x1154, 0x1155, 0x1400, 0x1401, 0x1404, 0x1405,
			0x1410, 0x1411, 0x1414, 0x1415, 0x1440, 0x1441, 0x1444, 0x1445,
			0x1450, 0x1451, 0x1454, 0x1455, 0x1500, 0x1501, 0x1504, 0x1505,
			0x1510, 0x1511, 0x1514, 0x1515, 0x1540, 0x1541, 0x1544, 0x1545,
			0x1550, 0x1551, 0x1554, 0x1555, 0x4000, 0x4001, 0x4004, 0x4005,
			0x4010, 0x4011, 0x4014, 0x4015, 0x4040, 0x4041, 0x4044, 0x4045,
			0x4050, 0x4051, 0x4054, 0x4055, 0x4100, 0x4101, 0x4104, 0x4105,
			0x4110, 0x4111, 0x4114, 0x4115, 0x4140, 0x4141, 0x4144, 0x4145,
			0x4150, 0x4151, 0x4154, 0x4155, 0x4400, 0x4401, 0x4404, 0x4405,
			0x4410, 0x4411, 0x4414, 0x4415, 0x4440, 0x4441, 0x4444, 0x4445,
			0x4450, 0x4451, 0x4454, 0x4455, 0x4500, 0x4501, 0x4504, 0x4505,
			0x4510, 0x4511, 0x4514, 0x4515, 0x4540, 0x4541, 0x4544, 0x4545,
			0x4550, 0x4551, 0x4554, 0x4555, 0x5000, 0x5001, 0x5004, 0x5005,
			0x5010, 0x5011, 0x5014, 0x5015, 0x5040, 0x5041, 0x5044, 0x5045,
			0x5050, 0x5051, 0x5054, 0x5055, 0x5100, 0x5101, 0x5104, 0x5105,
			0x5110, 0x5111, 0x5114, 0x5115, 0x5140, 0x5141, 0x5144, 0x5145,
			0x5150, 0x5151, 0x5154, 0x5155, 0x5400, 0x5401, 0x5404, 0x5405,
			0x5410, 0x5411, 0x5414, 0x5415, 0x5440, 0x5441, 0x5444, 0x5445,
			0x5450, 0x5451, 0x5454, 0x5455, 0x5500, 0x5501, 0x5504, 0x5505,
			0x5510, 0x5511, 0x5514, 0x5515, 0x5540, 0x5541, 0x5544, 0x5545,
			0x5550, 0x5551, 0x5554, 0x5555 };
	
	
	
	int _N = 100;					
	double minX;
	double minY;
	double maxX;
	double maxY;
	double xSeg;
	double ySeg;
	
	

	public Grid(double minX, double maxX, double minY,  double maxY, short N) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		this._N = N;

		if (minX > maxX || minY > maxY) {
			System.out.println("minimum X(Y) cannot exceed maximum X(Y)");
			System.exit(-1);
		}

		xSeg = (maxX - minX) / _N;
		ySeg = (maxY - minY) / _N;
	}
	
	
	public Grid(Region root, short N) {
		this.minX = root.min_x;
		this.minY = root.min_y;
		this.maxX = root.max_x;
		this.maxY = root.max_y;
		this._N = N;

		if (minX > maxX || minY > maxY) {
			System.out.println("minimum X(Y) cannot exceed maximum X(Y)");
			System.exit(-1);
		}

		xSeg = (maxX - minX) / _N;
		ySeg = (maxY - minY) / _N;
	}

	/**
	 * ith cell of x lies in [minX * i, minX*i + xSeg) handle the exception for
	 * the maxX
	 * 
	 * @param x
	 * @return
	 */
	public short getX(double x) {
		short idx;

		if (x == maxX)
			idx = (short) (this._N - 1);
		else
			idx = (short) ((x - minX) / xSeg);

		return idx;
	}

	/**
	 * ith cell of y lies in [minY * i, minY*i + ySeg) handle the exception for
	 * the maxY
	 * 
	 * @param y
	 * @return
	 */
	public short getY(double y) {
		short idx;

		if (y == maxY) {
			idx = (short) (this._N - 1);
		} else
			idx = (short) ((y - minY) / ySeg);

		return idx;
	}

	/**
	 * x must be less than or equal to maxX y must be less than or equal to maxY
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int getIndex(double x, double y) {

		if (x < this.minX)	x = this.minX;
		if (y < this.minY)	y = this.minY;
		if (x > this.maxX)	x = this.maxX;
		if (y > this.maxY)	y = this.maxY;
		
		int zValue;
		short xIdx = getX(x);
		short yIdx = getY(y);

		zValue = getMortonNumber(xIdx, yIdx);

		return zValue;
	}

	/**
	 * Interleave bits of xValue and yValue, so that all of the bits of x are in
	 * the even positions and y in the odd;
	 * 
	 * @param xValue
	 *            : x coordinate
	 * @param yValue
	 *            : y coordinate
	 * @return z: gets the resulting 32-bit Morton Number.
	 */
	public int getMortonNumber(short xValue, short yValue) {
		int z;

		z = MortonTable256[yValue >> 8] << 17
				| MortonTable256[xValue >> 8] << 16
				| MortonTable256[yValue & 0xFF] << 1
				| MortonTable256[xValue & 0xFF];

		return z;
	}

	/**
	 * @param minX
	 * @param minY
	 * @param maxX
	 * @param maxY
	 * @return: a set of zValues according to a rectangle
	 */
	public int[] getRectangleIndex(double minX, double minY, double maxX,
			double maxY) {
		if (minX < this.minX)	minX = this.minX;
		if (minY < this.minY)	minY = this.minY;
		if (maxX > this.maxX)	maxX = this.maxX;
		if (maxY > this.maxY)	maxY = this.maxY;

		short minXIdx = getX(minX);
		short maxXIdx = getX(maxX);
		short minYIdx = getY(minY);
		short maxYIdx = getY(maxY);
		int numOfCells = (maxXIdx - minXIdx + 1) * (maxYIdx - minYIdx + 1);
		int[] range = new int[numOfCells];

		short i, j;
		int idx = 0;
		for (i = minXIdx; i <= maxXIdx; i++) {
			for (j = minXIdx; j <= maxYIdx; j++) {
				range[idx++] = getMortonNumber(i, j);
			}
		}
		Arrays.sort(range);

		return range;
	}

	/**
	 * @param minX
	 * @param minY
	 * @param maxX
	 * @param maxY
	 * @return: a set of zValues according to a circle
	 */
	public int[] getCirleIndex(double x, double y, double radius) {
		double minX = x - radius;
		double maxX = x + radius;
		double minY = y - radius;
		double maxY = y + radius;

		return getRectangleIndex(minX, maxX, minY, maxY);
	}
	
	
	/*
	 * Given a query location and radius, return the maximum and minimum cells whose 
	 * z-order are contained within the range. Any cells with z-order outside this
	 * range are considered to be far away form (x,y).
	 */
	public int[] getMinMaxRange(double x, double y, double radius)
	{
		int[] range = new int[2];
		double minX = x - radius;
		double maxX = x + radius;
		double minY = y - radius;
		double maxY = y + radius;
		
		if (minX < this.minX)	minX = this.minX;
		if (minY < this.minY)	minY = this.minY;
		if (maxX > this.maxX)	maxX = this.maxX;
		if (maxY > this.maxY)	maxY = this.maxY;

		short minXIdx = getX(minX);
		short maxXIdx = getX(maxX);
		short minYIdx = getY(minY);
		short maxYIdx = getY(maxY);
		range[0] = getMortonNumber(minXIdx, minYIdx);
		range[1] = getMortonNumber(maxXIdx, maxYIdx);
		
		return range;
	}

	
	public double getMinDist(double x, double y, int min_x, boolean forward)
	{
		double dist = 0.0;
		
		return dist;
	}
	
	
	public static void main(String[] args) {
		Grid grid = new Grid(1d, 1d, 10d, 10d, (short) 10);

		System.out.println(grid.getMortonNumber((short) 10, (short) 10));
		
		System.out.println(grid.getIndex(3, 3));
		

		int[] range = grid.getRectangleIndex(6.2, 6.2, 8.1, 8.1);
		System.out.println(Arrays.toString(range));
		
		
		
		int[] minMaxRange = grid.getMinMaxRange(2, 2, 1.0);
		System.out.println(Arrays.toString(minMaxRange));
	}
	
}
