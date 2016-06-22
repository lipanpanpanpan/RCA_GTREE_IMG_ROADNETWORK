/*
 * CMain.java
 *  Created on: 2008-9-16
 *    Language: Java
 *  Operator System:Ubuntu
 *         IDE:Eclipse
 *      Author: XXXX
 *      瀛﹀彿锛歑XXX
 *      鐝骇锛歑XXX
 */
package bitVector;

import java.util.*;

public class CMain {

	public static void main(String[] arges){
		
		CBitArray BYRDCT=new CBitArray(20);
		
		BYRDCT.PrintBit();
//		BYRDCT.SetAllOne();
		BYRDCT.assignOne(0);
		BYRDCT.PrintBit();
//		System.out.println(BYRDCT.orAtIndex(0));
		BYRDCT.orAtIndex(1);
		BYRDCT.PrintBit();
//		System.out.println(BYRDCT.andAtIndex(2));
		BYRDCT.assignOne(18);
		BYRDCT.assignOne(19);
		BYRDCT.assignOne(4);
		 BYRDCT.PrintBit();
		 BYRDCT.SetAllOne();
		 BYRDCT.PrintBit();
		BYRDCT.RightMoveSteps(3);
		 BYRDCT.PrintBit();
		 System.out.println("===================================");
		 
		 CBitArray ad=BYRDCT.copyOne();
		 ad.RightMoveSteps(2);
		 BYRDCT.PrintBit();
		 ad.PrintBit();
		 
		 
		 
		 
//		BYRDCT.assignOne(0);
//		BYRDCT.assignOne(1);
//		BYRDCT.assignOne(19);
//		BYRDCT.assignOne(18);
//		BYRDCT.PrintBit();
//		BYRDCT.OROperator(255);
//		BYRDCT.PrintBit();
//		BYRDCT.RightMoveOneStep();
//		BYRDCT.PrintBit();
//		
//		BYRDCT.OROperator(2);
//		BYRDCT.PrintBit();
		
//		if(BYRDCT.and(4)==0)
//		{
//			System.out.println("And的结果是"+BYRDCT.and(6));
//		}
//		else
//		{
//			
//		}
//			byte uuu=~0;
//			System.out.println("测试"+uuu);
//		CBitArray BYRDCT=new CBitArray(20);
//		BYRDCT.PrintBit();
//		BYRDCT=BYRDCT.neg();
//		BYRDCT.PrintBit();
//		Boolean flag=false;
//		flag=BYRDCT.EachBitEquil(19);
//		System.out.println(flag);
////		System.out.println(BYRDCT.ChangeToString());
//		int checkBit=5;
//		int flag=0;
//		int checkBitLength=1;
//		int temp=checkBit;
//		int currentBit=0;
//		Vector<Integer> rTV=new Vector<Integer>();
//		//获取得到最后一位的数0/1;
//		currentBit=checkBit%2;
//		rTV.addElement(currentBit);
//		while(temp>1)
//		{
//			temp=temp/2;
//			currentBit=temp%2;
//			rTV.addElement(currentBit);
//			checkBitLength++;
//		}
//		System.out.println(rTV.toString());
	}
}
