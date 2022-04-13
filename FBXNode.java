package com.nmaid.asset.fbx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FBXNode {
	public boolean singleProperty;
	public long id;
	public String attrName;
	public String attrType;
	public String name;
	public Object node;
	public List<Object> propertyList;
	//public Map<String,Object> SubNodeMap;
	public Object a;
	public List<Object> connections;
	
	//public FBXNode parent;
	public List<FBXNode> children;
	public FBXNode(){
		//SubNodeMap=new HashMap();
	}
	
	public void add(FBXNode child){
		if(child==null)
			return;
		if(children==null){
			children=new ArrayList<FBXNode>();
		}
		//child.parent=this;
		this.children.add(child);
	}
}
