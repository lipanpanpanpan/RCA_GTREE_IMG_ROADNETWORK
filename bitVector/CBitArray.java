package bitVector;
import java.util.*;
public class CBitArray {
	public Vector<Integer> m_v ;
//	private Vector<Integer> m_c;
	
	
	public  CBitArray(int bitLength)
	{
		m_v=new Vector<Integer>();

		for(int i=0;i<bitLength;i++)
		{
			m_v.add(i, 0);
		}
	}
	public CBitArray() {
		// TODO Auto-generated constructor stub
	}
	
	
	
	public CBitArray copyOne()
	{
		CBitArray copytmp=new CBitArray(m_v.size());
		copytmp.m_v=(Vector<Integer>) m_v.clone();
		
		return copytmp;
	}
	
	
	/**
	 * 瀵规煇浣嶇疆1
	 * @param index
	 */
	public void assignOne(int index)
	{
		m_v.setElementAt(1, index);
	}
	
	/**
	 * 瀵规煇浣嶇疆0
	 * @param index
	 */
	public void assignZero(int index)
	{
		m_v.setElementAt(0, index);
	}
	
	
	/**
	 * and鎿嶄綔
	 * @param cba
	 * @return
	 */
	public int andAtIndex(int index)
	{
		if(index>=m_v.size())
		{
			return 0;
		}
		else
		{
			return m_v.elementAt(index);
		}
//		return 0;
		
	}
	
	

	
	
	public void SetAllOne()
	{
		for(int i=0;i<m_v.size();i++)
		{
			m_v.setElementAt(1, i);
		}
	}
	
	
	/**
	 * 灏嗘煇涓�綅缃�
	 * @date 2014-8-3
	 * @Editor BYRD
	 * @param index
	 * @return
	 */
	public CBitArray orAtIndex(int index)
	{
//		System.out.println(m_v.size()+"SIze");
		if(index>=m_v.size())
		{

		}
		else
		{
			m_v.setElementAt(1, index);
		}
		
		return this;
	}
	
	

	
	/**
	 * 
	 * @date 2014-7-29
	 * @Editor BYRD
	 */
	public CBitArray RightMoveSteps(Integer steps)
	{
	
		for(int i=0;i<m_v.size();i++)
		{
			if(i<(m_v.size()-steps))
			{
				m_v.setElementAt(m_v.elementAt(i+steps), i);
			}
			else
			{
				m_v.setElementAt(0, i);
			}
		}

		
		return this;
	}
	
	
	
	public CBitArray RightMoveStepsForNew(Integer steps)
	{
	
		CBitArray abc=this.copyOne();
		
		abc.RightMoveSteps(steps);
		
		return abc;
	}
	
	
	
	public  Boolean CompareWithMask(int keyNums)
	{
		Boolean flag=true;
		
		if(keyNums==(m_v.size()-1))
		{
			for(int i=0;i<m_v.size();i++)
			{
				if(m_v.elementAt(i).equals(0))
				{
					flag=false;
					break;
				}
			}
		}
		else
		{
			return false;
		}
		return flag;
	}
	
//	/**
//	 * 鍙栧弽
//	 * @return
//	 */
//	public CBitArray neg()
//	{
//		CBitArray ncba;
//		ncba=new CBitArray();
//		ncba.SetMC(m_c);
//		for(int i=0;i<m_v.size();i++)
//		{
//			if(item(i).equals(1))
//				ncba.assignZero(i);
//			else
//				ncba.assignOne(i);
//		}
//		return ncba;
//	}
//	
	
	/**
	 * 寰楀埌鏌愪釜item
	 * @param index
	 * @return
	 */
	public Object item(int index)
	{
		return m_v.elementAt(index);
	}

	/**
	 * 鎵�湁浣嶉兘娓呴浂
	 */
	public void Clean()
	{
		for(int i=0;i<m_v.size();i++)
			this.assignZero(i);
	}
	
	/**
	 * 璁＄畻涓�鐨勫灏戜綅
	 * @return
	 */
	public int SizeWithOne()
	{
		int count=0;
		for(int i=0;i<m_v.size();i++)
			if(m_v.elementAt(i).equals(1))
				count++;
		return count;
	}
	
	/**
	 * 鎵撳嵃浣嶆暟
	 */
	public void PrintBit()
	{

		for(int i=0;i<m_v.size();i++){
		    System.out.print(m_v.elementAt(i).toString());
		}
		System.out.println();
	}
	/**
	 * 杞崲鎴怱tring鏁扮粍
	 * @return
	 */
	public String ChangeToString()
	{
		String bitString="";
		for(int i=0;i<m_v.size();i++){
			bitString+=m_v.elementAt(i).toString().trim();
		}
		return bitString;
	}
	
	
//	
//	/**
//	 * 鎵撳嵃Item
//	 */
//	public void PrintItem()
//	{
//		for(int i=0;i<m_c.size();i++)
//			if((m_v.elementAt(i)).equals(1))
//				System.out.print(m_c.elementAt(i).toString()+" ");
//		System.out.println();
//	}	
	
	 
}
