package com.yellowforktech.littlefamilytree.util;

import android.content.Context;
import android.util.Log;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;

import org.gedcomx.types.GenderType;

import java.util.ArrayList;
import java.util.List;

public class RelationshipCalculator
{
	public static String getRelationship(LittlePerson me, LittlePerson p, Context context) {
		if (me.equals(p)) return context.getResources().getString(R.string.you);
		if (p.getTreeLevel()!=null && me.getTreeLevel()!=null) {
			DataService dataService = DataService.getInstance();
			try {
				if (p.getTreeLevel().equals(me.getTreeLevel())) {
					List<LittlePerson> spouses = dataService.getSpouses(me);
					if (spouses.contains(p)) {
						if (p.getGender() == GenderType.Female) {
							return context.getResources().getString(R.string.wife);
						} else {
							return context.getResources().getString(R.string.husband);
						}
					}
					List<LittlePerson> parents = dataService.getParents(me);
					for(LittlePerson parent : parents) {
						List<LittlePerson> myFamily = dataService.getChildren(parent);
						if (myFamily.contains(p)) {
							if (p.getGender() == GenderType.Female) {
								return context.getResources().getString(R.string.sister);
							} else {
								return context.getResources().getString(R.string.brother);
							}
						}
						//-- check for in-laws
						for (LittlePerson bs : myFamily) {
							if (bs.getTreeLevel()!=null && bs.getTreeLevel().equals(me.getTreeLevel())) {
								List<LittlePerson> bsspouses = dataService.getDBHelper().getSpousesForPerson(bs.getId());
								if (bsspouses.contains(p)) {
									if (p.getGender() == GenderType.Female) {
										return context.getResources().getString(R.string.sister)+" "+context.getResources().getString(R.string.inlaw);
									} else {
										return context.getResources().getString(R.string.brother)+" "+context.getResources().getString(R.string.inlaw);
									}
								}

							}
						}
					}
					return context.getResources().getString(R.string.cousin);
				}
				if (p.getTreeLevel().equals(me.getTreeLevel()-1)) {
					List<LittlePerson> myFamily = dataService.getFamilyMembers(me);
					if (myFamily.contains(p)) {
						if (p.getGender() == GenderType.Female) {
							return context.getResources().getString(R.string.daughter);
						} else {
							return context.getResources().getString(R.string.son);
						}
					} else {
						if (p.getGender() == GenderType.Female) {
							return context.getResources().getString(R.string.niece);
						} else {
							return context.getResources().getString(R.string.nephew);
						}
					}
				}
				if (p.getTreeLevel() > me.getTreeLevel()) {
					int distance = p.getTreeLevel() - me.getTreeLevel();
					String rel = getGreatness(distance, context);
					int d = 0;
					List<LittlePerson> levelPeople = new ArrayList<>();
					levelPeople.add(me);

					List<LittlePerson> inLaws = new ArrayList<>();
					List<LittlePerson> spouses = dataService.getDBHelper().getSpousesForPerson(me.getId());
					inLaws.addAll(spouses);
					do {
						List<LittlePerson> nextLevel = new ArrayList<>();
						for(LittlePerson pp : levelPeople) {
							List<LittlePerson> parents = dataService.getDBHelper().getParentsForPerson(pp.getId());
							nextLevel.addAll(parents);
						}
						levelPeople = nextLevel;

						List<LittlePerson> nextInLaw = new ArrayList<>();
						for(LittlePerson pp : inLaws) {
							List<LittlePerson> parents = dataService.getDBHelper().getParentsForPerson(pp.getId());
							nextInLaw.addAll(parents);
						}
						inLaws = nextInLaw;

						d++;
					} while(d<distance);

					if (levelPeople.contains(p)) {
						if (p.getGender() == GenderType.Female) {
							rel += context.getResources().getString(R.string.mother);
						} else {
							rel += context.getResources().getString(R.string.father);
						}
					}
					else if (inLaws.contains(p)) {
						if (p.getGender() == GenderType.Female) {
							rel += context.getResources().getString(R.string.mother);
						} else {
							rel += context.getResources().getString(R.string.father);
						}
						rel += " "+context.getResources().getString(R.string.inlaw);
					}
					else {
						rel = rel.replaceAll("Grand", "Great");
						if (p.getGender() == GenderType.Female) {
							rel += context.getResources().getString(R.string.aunt);
						} else {
							rel += context.getResources().getString(R.string.uncle);
						}
					}
					return rel;
				}
				if (p.getTreeLevel() < me.getTreeLevel() -1) {
					int distance = Math.abs(p.getTreeLevel() - me.getTreeLevel());
					String rel = getGreatness(distance, context);
					if (p.getGender() == GenderType.Female) {
						rel += context.getResources().getString(R.string.daughter);
					} else {
						rel += context.getResources().getString(R.string.son);
					}
					return rel;
				}
			}catch (Exception e) {
				Log.d("RelationshipCalculator", "Error getting relationship between "+me.getName()+" and "+p.getName());
			}
		}
		return null;
	}

	public static String getGreatness(int depth, Context context) {
		String rel = "";
		if (depth>4) {

			String great = (depth-2)+"th";
			switch (depth) {
				case 5:
					great = context.getResources().getString(R.string.great3);
					break;
				case 6:
					great = context.getResources().getString(R.string.great4);
					break;
				case 7:
					great = context.getResources().getString(R.string.great5);
					break;
				case 8:
					great = context.getResources().getString(R.string.great6);
					break;
				case 9:
					great = context.getResources().getString(R.string.great7);
					break;
				case 10:
					great = context.getResources().getString(R.string.great8);
					break;
				case 11:
					great = context.getResources().getString(R.string.great9);
					break;
				case 12:
					great = context.getResources().getString(R.string.great10);
					break;
				case 13:
					great = context.getResources().getString(R.string.great11);
					break;
				case 14:
					great = context.getResources().getString(R.string.great12);
					break;
				case 15:
					great = context.getResources().getString(R.string.great13);
					break;
				case 16:
					great = context.getResources().getString(R.string.great14);
					break;
			}
			rel = great + " "+context.getResources().getString(R.string.great)+" ";

		} else {
			for(int g=3; g<=depth; g++) {
				rel += context.getResources().getString(R.string.great)+" ";
			}
		}
		if (depth>=2) {
			rel += context.getResources().getString(R.string.grand)+" ";
		}
		return rel;
	}

	public static String getAncestralRelationship(int depth, LittlePerson p, LittlePerson me, boolean isRoot, boolean isChild, boolean isInLaw, Context context){
		String rel = getGreatness(depth, context);

        if (p.getGender()== GenderType.Female) {
            if (depth==0) {
                if (isRoot) {
                    if (p==me) rel = context.getResources().getString(R.string.you);
                    else rel = context.getResources().getString(R.string.wife);
                } else {
                    if (isChild) rel = context.getResources().getString(R.string.daughter);
					else rel = context.getResources().getString(R.string.sister);
                }
            } else {
                rel += context.getResources().getString(R.string.mother);
            }
        }
        else if (p.getGender()==GenderType.Male) {
            if (depth==0) {
                if (isRoot) {
                    if (p==me) rel = context.getResources().getString(R.string.you);
                    else rel = context.getResources().getString(R.string.husband);
                } else {
                    if (isChild) rel = context.getResources().getString(R.string.son);
					else rel = context.getResources().getString(R.string.brother);
                }
            } else {
                rel += context.getResources().getString(R.string.father);
            }
        } else {
            rel += context.getResources().getString(R.string.parent);
        }
		if (isInLaw) {
			rel += " "+context.getResources().getString(R.string.inlaw);
		}
        return rel;
    } 
}
