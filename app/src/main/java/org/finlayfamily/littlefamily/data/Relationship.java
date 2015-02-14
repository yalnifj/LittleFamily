package org.finlayfamily.littlefamily.data;

public class Relationship
{
	private int id1;
	private int id2;
	private RelationshipType type;

	public void setType(RelationshipType type)
	{
		this.type = type;
	}

	public RelationshipType getType()
	{
		return type;
	}

	public void setId2(int id2)
	{
		this.id2 = id2;
	}

	public int getId2()
	{
		return id2;
	}

	public void setId1(int id1)
	{
		this.id1 = id1;
	}

	public int getId1()
	{
		return id1;
	}
}
