package com.yellowforktech.littlefamilytree.data;

public enum RelationshipType
{
	PARENTCHILD(0), SPOUSE(1);
	
	int id;
	RelationshipType(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	
	public static RelationshipType getTypeFromId(int id) {
		return values()[id];
	}
}
