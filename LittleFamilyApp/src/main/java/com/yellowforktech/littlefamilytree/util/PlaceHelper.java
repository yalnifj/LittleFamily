package com.yellowforktech.littlefamilytree.util;

import java.util.Arrays;
import java.util.Locale;

/**
 * Created by Parents on 3/24/2015.
 */
public class PlaceHelper {
    public static String UNKNOWN="Unknown";
    private static String[] usStates = {
            "alabama", "alaska", "arizona", "arkansas", "california", "colorado", "connecticut",
            "delaware","florida","georgia","hawaii","idaho","illinois","indiana","iowa","kansas",
            "kentucky","louisiana","maine","maryland","massachusetts","michigan","minnesota",
            "mississippi","missouri","montana","nebraska","nevada","new hampshire","new jersey",
            "new mexico","new york","north carolina","north dakota","ohio","oklahoma","oregon",
            "pennsylvania","rhode island", "south carolina","south dakota","tennessee","texas",
            "utah","vermont","virginia","washington","west virginia","wisconsin","wyoming",
			"british colonial america", "colonial america", "british america"
    };

    private static String[] abbvStates = {
            "ala","alaska","ariz","ark","calif","colo","conn","del","fla","ga","hawaii","idaho",
            "ill","ind","iowa","kans","ky","la","maine","md","mass","mich","minn","miss","mo",
            "mont","nebr","nev","n.h","n.j","n.m","n.y","n.c","n.d","ohio","okla","ore",
            "pa","r.i","s.c","s.d","tenn","tex","utah","vt","va","wash","w.va","wis","wyo",
            "al","ak","az","ar","ca","co","con","ct","de","fl","ga","hi","id","il","in","ia","ks","ky",
            "la","me","md","ma","mi","mn","ms","mo","mt","ne","nv","nh","nj","nm","ny","nc","nd",
            "oh","ok","or","pa","ri","sc","sd","tn","tx","ut","vt","va","wa","wv","wi","wy"

            };

    static {
        Arrays.sort(abbvStates);
    }
	
	private static String[] tribes = {
		"cherokee", "apache", "navajo", "iriquois"
	};

    public static boolean isInUS(String place) {
        String tempPlace = place.toLowerCase();
        tempPlace = tempPlace.replaceAll("territory", "")
			.replaceAll("nation", "")
			.trim();
        if (tempPlace.equals("united states")) return true;
        if (tempPlace.equals("united states of america")) return true;
        if (tempPlace.equals("us")) return true;
        if (tempPlace.equals("usa")) return true;
        int i = Arrays.binarySearch(usStates, tempPlace);
        if (i>=0) return true;
        i = Arrays.binarySearch(abbvStates, tempPlace);
        if (i>=0) return true;
        String[] parts = tempPlace.split(" ");
        if (parts.length==1) return false;
        for(String p : parts) {
            i = Arrays.binarySearch(usStates, p);
            if (i>=0) return true;
            i = Arrays.binarySearch(abbvStates, p);
            if (i>=0) return true;
        }
        return false;
    }

    public static String getCountryLanguage(String country) {
        for(Locale locale : Locale.getAvailableLocales()) {
            if (locale.getDisplayCountry().equalsIgnoreCase(country)) {
                return locale.getDisplayLanguage();
            }
        }
        return country;
    }
    public static int countPlaceLevels(String place) {
        if (place==null) return 0;
        String[] parts = place.split("[,]+");
        return parts.length;
    }

    public static String getTopPlace(String place) {
        if (place==null) return null;
        String[] parts = place.split("[,]+");
        return parts[parts.length-1].trim().replaceAll("[<>\\[\\]\\(\\)\\.\"]+", "");
    }

    public static String getTopPlace(String place, int level) {
        if (place==null) return null;
        String[] parts = place.split("[,]+");
        if (parts.length>=level) {
            return parts[parts.length - level].trim().replaceAll("[<>\\[\\]\\(\\)\\.\"]+", "");
        }
        return parts[parts.length - 1].trim().replaceAll("[<>\\[\\]\\(\\)\\.\"]+", "");
    }

    public static String getPlaceCountry(String p) {
        String place = PlaceHelper.getTopPlace(p);
        if (place.equals("United Kingdom")) {
            place = getTopPlace(p, 2);
        }
        if (place == null) place = UNKNOWN;
        if (!place.equals("United States") && PlaceHelper.isInUS(place))
            place = "United States";
        return place;
    }
}
