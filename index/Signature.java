package index;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Signature implements Serializable {

	private static final long serialVersionUID = 5540007874891188362L;
	
	final static byte LEN = 4;
	final static int firstMask = 0xFF;
	final static int secondMask = 0x03;
	long[] signature;
	
	public Signature()
	{
		signature = new long[LEN];
	}
	
	public Signature(Signature other)
	{
		signature = other.signature.clone();
	}
	
	public static int getLen()
	{
		return LEN*64;
	}
	
	public void add(int id)
	{
		signature[(id >> 8) & secondMask] |= (1L << (id & firstMask));
	}
	
	public boolean match(int id)
	{
		return (signature[(id >> 8) & secondMask] & (1L << (id & firstMask))) != 0L;
	}
	
	public boolean intersect(Signature other)
	{
		for(int i=0;i < LEN;i++){
			if( (signature[i] & other.signature[i]) != 0){
				return true;
			}
		}
		return false;
	}
	
	public Signature getIntersection(Signature other)
	{
		Signature newSig = new Signature(this);
		for(int i=0;i < LEN;i++){
			newSig.signature[i] &= other.signature[i];
		}
		return newSig;
	}
	
	public boolean isSet(int bit)
	{
		return (signature[bit & secondMask] &  (1L << ( bit & firstMask))) != 0L;
	}
	
	public boolean empty()
	{
		for(int i=0;i < LEN;i++){
			if(signature[i] != 0L){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString()
	{
		String str = "";
		for(int i=0;i < LEN;i++){
			long sig = signature[i];
			for(int j=0;j < 64;j++){
				if( (sig & 0x1) == 1){
					str += "1";
				}else{
					str += "0";
				}
				sig >>= 1;
			}
			str += " ";
		}
		return str;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		for(int i=0;i < LEN;i++){
			out.writeLong(signature[i]);
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		signature = new long[LEN];
		for(int i=0;i < LEN;i++){
			signature[i] = in.readLong();
		}
	}
}
