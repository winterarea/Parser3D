package com.nmaid.asset.fbx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;
//https://blog.csdn.net/jli_family/article/details/87545176
//http://www.manongjc.com/article/55424.html
//数据结构：https://blog.csdn.net/jli_family/article/details/87545176
public class FBXLoader {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String path="G:/NMaidData";
		//FileReader fr=new FileReader(path);
		//BufferedReader br=new BufferedReader(fr);
		File file=new File(path,"Samba Dancing.fbx");
		file=new File(path,"untitled.fbx");
		InputStream is=new FileInputStream(file);
		System.out.println("Length:"+is.available());
		DataInputStream dis=new DataInputStream(is);
		FBXReader reader=new FBXReader(dis);
		String magic="Kaydara FBX Binary  ";
		byte[] buffer=new byte[1024];
		dis.read(buffer, 0, buffer.length);
		String magicRead=convertArrayBufferToString(buffer,0,magic.getBytes().length);
		System.out.println(magicRead);
		dis.close();
		is.close();
		is=new FileInputStream(file);
		dis=new DataInputStream(is);
		reader=new FBXReader(dis);
		//dis.skipBytes(23);
		magic=reader.readString(23);
		int version=reader.readInt32();
		//version>=7500:read 64,version<7500:read 32
		System.out.println("version:"+version);
		
		Map<String,FBXNode> allNodes=new HashMap();
		while(!reader.isEndOfContent()){
			FBXNode node=parseNode(reader,version);
			if(node!=null)
				allNodes.put(node.name, node);
		}
		dis.close();
		is.close();
		//PrintNodeMap(allNodes);
/*		Set<Entry<String, FBXNode>> set=allNodes.entrySet();
		Iterator<Entry<String, FBXNode>> it=set.iterator();
		while(it.hasNext()){
			Entry<String, FBXNode> entry=it.next();
			FBXNode node=entry.getValue();
			System.out.print(entry.getKey()+":");
			PrintNodeContent(node);
			if(node.SubNodeMap.size()>0){
				PrintNodeMap(node.SubNodeMap);
			}
		}*/
	}
	public static void PrintNodeMap(Object nodeMapObject){
		if(nodeMapObject instanceof HashMap){
			Set<Entry<String, Object>> set=((Map<String, Object>) nodeMapObject).entrySet();
			Iterator<Entry<String, Object>> it=set.iterator();
			while(it.hasNext()){
				Entry<String, Object> entry=it.next();
				Object nodeObject=entry.getValue();
				if(nodeObject instanceof FBXNode){
					FBXNode node=(FBXNode)nodeObject;
					System.out.print(entry.getKey()+":");
					PrintNodeContent(node);
					if(node.SubNodeMap.size()>0){
						PrintNodeMap(node.SubNodeMap);
					}
				}else{
					//System.out.print(nodeObject.toString()+"-");
				}
			}
		}else{
			System.out.print("PrintNodeMap:"+nodeMapObject.getClass().toString());
		}
		
	}
	public static void PrintNodeContent(Object nodeObject){
		if(nodeObject instanceof FBXNode){
			FBXNode node=(FBXNode)nodeObject;
			for(int i=0;i<node.propertyList.size();i++){
				if(node.propertyList.get(i).getClass().isArray()){
					try{
						if(node.propertyList.get(i) instanceof byte[]){
							System.out.print(DatatypeConverter.printHexBinary((byte[])node.propertyList.get(i))+",");
						}else{
							Object[] temp=(Object[]) node.propertyList.get(i);
							System.out.print("Array:");
							for(int j=0;j<temp.length;j++){
								System.out.print(temp[j]+",");
							}
						}
					}catch(Exception e){
						System.out.print("Exception"+":"+node.propertyList.get(i).getClass().toString());
					}
				}else{
					System.out.print(node.propertyList.get(i)+":");
				}
			}
			if(node.SubNodeMap.entrySet().size()>0){
				System.out.print("Child["+node.SubNodeMap.entrySet().size()+"]:");
				Iterator<Entry<String, Object>> it2=node.SubNodeMap.entrySet().iterator();
				while(it2.hasNext()){
					Entry<String, Object> entry2=it2.next();
					System.out.print(entry2.getKey()+",");
				}
			}
		}else{
			System.out.print("PrintNodeContent:"+nodeObject.getClass());
		}
		
		System.out.println();
	}
	public static FBXNode parseNode(FBXReader reader,int version) throws IOException{
		FBXNode node = new FBXNode();
		// The first three data sizes depends on version.
		int endOffset=reader.readInt32();
		int numProperties=reader.readInt32();
		long tmp=(version>=7500)? reader.readInt64():reader.readInt32();
		
		//System.out.println("endOffset:"+endOffset);
		//System.out.println("numProperties:"+numProperties);
		String name=reader.readString(reader.readUInt8());
		
		// Regards this node as NULL-record if endOffset is zero
		if ( endOffset == 0 ) return null;

		System.out.print(name+":");
		List<Object> propertyList=new ArrayList();
		for(int i=0;i<numProperties;i++){
			Object property=parseProperty(reader);
			propertyList.add(property);
			System.out.print(property+",");
		}
		System.out.println(" End.");
		
		Object id=propertyList.size()>0? propertyList.get(0):"";
		Object attrName=propertyList.size()>1? propertyList.get(1):"";
		Object attrType=propertyList.size()>2? propertyList.get(2):"";
		
		node.singleProperty=(numProperties==1&&reader.offset==endOffset)? true:false;
		while(endOffset>reader.offset){
			FBXNode subNode=parseNode(reader,version);
			if(subNode!=null){
				parseSubNode(name,node,subNode);
				//node.add(subNode);
			}
		}
		node.propertyList=propertyList;
		
		if(id instanceof Integer||id instanceof Long) node.id=Long.parseLong(id.toString()); 
		if(attrName!="") node.attrName=attrName.toString();
		if(attrType!="") node.attrType=attrType.toString();
		if(name!="") node.name=name;
		return node;
	}
	public static void parseSubNode(String name,FBXNode node,FBXNode subNode) throws IOException{
		System.out.print("SubNode->"+subNode.name+":");
		if(subNode.singleProperty){
			Object value=subNode.propertyList.get(0);
			if(value.getClass().isArray()){
				node.SubNodeMap.put(subNode.name, subNode);
				subNode.a=value;
				System.out.print("[Array:");
				Object[] temp=(Object[]) value;
				for(int j=0;j<temp.length;j++){
					System.out.print(temp[j]+",");
				}
				System.out.print("],");
			}else{
				node.SubNodeMap.put(subNode.name, value);
				System.out.print("[Single:"+value+"],");
			}
		}else if("Connections".equals(name)&&"C".equals(subNode.name)){
			List<Object> array=new ArrayList();
			for(int i=1;i<subNode.propertyList.size();i++){
				array.add(subNode.propertyList.get(i));
				System.out.print(subNode.propertyList.get(i)+",");
			}
			if(node.connections==null){
				node.connections=new ArrayList();
			}
			node.connections.add(array);
		}else if("Properties70".equals(subNode.name)){
			//to Do
			node.name=subNode.name;
			node.a=subNode.a;
			node.attrName=subNode.attrName;
			node.attrType=subNode.attrType;
			node.id=subNode.id;
			
			//System.out.println("Propertyies70:"+name);
			//to be confirm
			node.propertyList=subNode.propertyList;
			node.SubNodeMap.putAll(subNode.SubNodeMap) ;
			System.out.print(subNode.attrName+","+subNode.attrType+",");
		}else if("Properties70".equals(name)&&"P".equals(subNode.name)){
			String innerPropName=subNode.propertyList.get(0).toString();
			String innerPropType1=subNode.propertyList.get(1).toString();
			String innerPropType2=subNode.propertyList.get(2).toString();
			String innerPropFlag=subNode.propertyList.get(3).toString();
			Object[] innerPropValue;
			if(innerPropName.indexOf("Lcl ")==0) 
				innerPropName = innerPropName.replace( "Lcl ", "Lcl_" );
			if (innerPropType1.indexOf("Lcl ") == 0 )
				innerPropType1 = innerPropType1.replace( "Lcl ", "Lcl_" );
			if("Color".equals(innerPropType1)||"ColorRGB".equals(innerPropType1)||"Vector".equals(innerPropType1)||
					"Vector3D".equals(innerPropType1)||innerPropType1.indexOf("Lcl_")==0){
				innerPropValue=new Object[]{
						subNode.propertyList.get(4),
						subNode.propertyList.get(5),
						subNode.propertyList.get(6)};
			}else{
				innerPropValue=new Object[]{subNode.propertyList.size()>4? subNode.propertyList.get(4):""};
			}
			Map<String,Object> tempMap=new HashMap();
			tempMap.put("type", innerPropType1);
			tempMap.put("type2", innerPropType2);
			tempMap.put("flag", innerPropFlag);
			tempMap.put("value", innerPropValue);
			node.SubNodeMap.put(innerPropName,tempMap );
			System.out.print(innerPropName+","+innerPropType1+","+innerPropType2+","+innerPropFlag+",");
		}
	}
	public static Object parseProperty(FBXReader reader) throws IOException{
		char type = reader.readString(1).charAt(0);
		int length;
		switch(type){
		case 'C': return reader.readUInt8();
		case 'D': return reader.readFloat64();
		case 'F': return reader.readFloat32();
		case 'I': return reader.readInt32();
		case 'L': return reader.readInt64();
		case 'R': 
			length = reader.readInt32();
			return reader.readBytes(length);
		case 'S': 
			length = reader.readInt32();
			return reader.readString(length);
		case 'Y': return reader.readInt16();
		case 'b':
		case 'c':
		case 'd':
		case 'f':
		case 'i':
		case 'l':
			int arrayLength=reader.readInt32();
			int encoding=reader.readInt32();
			int compressedLength=reader.readInt32();
			if(encoding==0){
				switch(type){
				case 'b':
				case 'c':
					return reader.readBooleanArray( arrayLength );
				case 'd':
					return reader.readFloat64Array( arrayLength );
				case 'f':
					return reader.readFloat32Array( arrayLength );
				case 'i':
					return reader.readInt32Array( arrayLength );
				case 'l':
					return reader.readInt64Array( arrayLength );
				}
			}
			byte[] compressed=reader.readBytes(compressedLength);
			//System.out.println("Compressed:"+compressed.length);
			byte[] result=new byte[2048];
			Inflater inf=new Inflater();
			inf.setInput(compressed);
			int infLen=-1;
			ByteArrayOutputStream outStream=new ByteArrayOutputStream();
			ByteArrayInputStream inStream=null;
			try {
				while((infLen=inf.inflate(result))>0){
					outStream.write(result,0,infLen);
				}
				inf.end();
				inStream=new ByteArrayInputStream(outStream.toByteArray());
				//System.out.println("Decompressed:"+inStream.available());
				DataInputStream dis2=new DataInputStream(inStream);
				FBXReader reader2=new FBXReader(dis2);
				switch ( type ) {
				case 'b':
				case 'c':
					return reader2.readBooleanArray( arrayLength );
				case 'd':
					return reader2.readFloat64Array( arrayLength );
				case 'f':
					return reader2.readFloat32Array( arrayLength );
				case 'i':
					return reader2.readInt32Array( arrayLength );
				case 'l':
					return reader2.readInt64Array( arrayLength );
				default:
					throw new Error("Unknown property type " + type);
				}
			} catch (DataFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		default:
			throw new Error("Unknown property type " + type);
		}
	}
	public static String readString(DataInputStream dis,int length) throws IOException{
		byte[] buffer=new byte[length];
		dis.read(buffer);
		return new String(buffer);
	}
	public static String convertArrayBufferToString(byte[] buffer,int from,int to){
		byte[] dest=new byte[to-from];
		System.arraycopy(buffer, 0, dest, 0, dest.length);
		return new String(dest);
	}

}
