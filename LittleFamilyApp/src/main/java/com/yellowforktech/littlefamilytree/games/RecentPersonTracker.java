package com.yellowforktech.littlefamilytree.games;
import java.util.*;
import com.yellowforktech.littlefamilytree.data.*;

public class RecentPersonTracker
{
	private Deque<Integer> recentPersonIds;
	private int maxRecent = 10;
	
	private static RecentPersonTracker instance;
	
	private RecentPersonTracker() {
		recentPersonIds = new LinkedList<>();
	}
	
	public static RecentPersonTracker getInstance() {
		if (instance==null) {
			instance = new RecentPersonTracker();
		}
		
		return instance;
	}
	
	public void addPerson(LittlePerson person) {
		recentPersonIds.remove(person.getId());
		recentPersonIds.add(person.getId());
		if (recentPersonIds.size() > maxRecent) {
			recentPersonIds.poll();
		}
	}
	
	public boolean personRecentlyUsed(LittlePerson person) {
		if (recentPersonIds.contains(person.getId()))
			return true;
		return false;
	}
} 
