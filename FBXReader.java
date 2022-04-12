package com.nmaid.asset.fbx;

import java.io.DataInputStream;
import java.io.IOException;

public class FBXReader {
	long offset;
	long byteLength;
	DataInputStream dis;
	public FBXReader(DataInputStream dis){
		this.dis=dis;
		this.offset=0;
		try {
			this.byteLength=dis.available();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int readUInt8() throws IOException{
		offset+=1;
		return dis.readUnsignedByte();
	}
	public int readUInt16() throws IOException{
		offset+=2;
		return Short.reverseBytes((short) dis.readUnsignedShort());
	}
	public int readInt16() throws IOException{
		offset+=2;
		return Short.reverseBytes(dis.readShort());
	}
	public int readInt32() throws IOException{
		offset+=4;
		return Integer.reverseBytes(dis.readInt());
	}
	public long readInt64() throws IOException{
		offset+=8;
		return Long.reverseBytes(dis.readLong());
	}
	public float readFloat32() throws IOException{
		offset+=4;
		return Float.intBitsToFloat(Integer.reverseBytes(dis.readInt()));
	}
	public double readFloat64() throws IOException{
		offset+=8;
		return Double.longBitsToDouble(Long.reverseBytes(dis.readLong()));
	}
	public byte[] readBytes(int length) throws IOException{
		byte[] buffer=new byte[length];
		int size=dis.read(buffer);
		offset+=size;
		return buffer;
	}
	public Integer[] readBooleanArray(int count) throws IOException{
		Integer[] array=new Integer[count];
		for(int i=0;i<array.length;i++){
			array[i]=this.readUInt8();
		}
		return array;
	}
	public Integer[] readInt32Array(int count) throws IOException{
		Integer[] array=new Integer[count];
		for(int i=0;i<array.length;i++){
			array[i]=this.readInt32();
		}
		return array;
	}
	public Long[] readInt64Array(int count) throws IOException{
		Long[] array=new Long[count];
		for(int i=0;i<array.length;i++){
			array[i]=this.readInt64();
		}
		return array;
	}
	public Float[] readFloat32Array(int count) throws IOException{
		Float[] array=new Float[count];
		for(int i=0;i<array.length;i++){
			array[i]=this.readFloat32();
		}
		return array;
	}
	public Double[] readFloat64Array(int count) throws IOException{
		Double[] array=new Double[count];
		for(int i=0;i<array.length;i++){
			array[i]=this.readFloat64();
		}
		return array;
	}
	public String readString(int length) throws IOException{
		byte[] buffer=new byte[length];
		int size=dis.read(buffer);
		offset+=size;
		return new String(buffer);
	}
	/**
	 * Check if reader has reached the end of content.
	 * @return
	 */
	public boolean isEndOfContent(){
		// footer size: 160bytes + 16-byte alignment padding
		// - 16bytes: magic
		// - padding til 16-byte alignment (at least 1byte?)
		//	(seems like some exporters embed fixed 15 or 16bytes?)
		// - 4bytes: magic
		// - 4bytes: version
		// - 120bytes: zero
		// - 16bytes: magic
		if(this.byteLength%16==0){
			return ((this.offset+160+16)&~0xf)>=this.byteLength;
		}else{
			return this.offset+160+16>=this.byteLength;
		}
	}
}
