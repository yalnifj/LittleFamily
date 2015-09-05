package com.yellowforktech.littlefamilytree.util;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import android.content.Context;
import com.yellowforktech.littlefamilytree.R;
import org.gedcomx.types.GenderType;

public class RelationshipCalculator
{
	public static String getAncestralRelationship(int depth, LittlePerson p, LittlePerson me, boolean isRoot, boolean isChild, boolean isInLaw, Context context){
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
