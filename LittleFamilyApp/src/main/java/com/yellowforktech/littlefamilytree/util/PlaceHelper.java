package com.yellowforktech.littlefamilytree.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Parents on 3/24/2015.
 */
public class PlaceHelper {
    public static String UNKNOWN="Unknown";
    private static String[] usStates = {
            "alabama", "alaska", "arizona", "arkansas", "british america",
            "british colonial america", "california","colonial america", "colorado", "connecticut",
            "delaware","florida","georgia","hawaii","idaho","illinois","indiana","iowa","kansas",
            "kentucky","louisiana","maine","maryland","massachusetts","michigan","minnesota",
            "mississippi","missouri","montana","nebraska","nevada","new hampshire","new jersey",
            "new mexico","new york","north carolina","north dakota","ohio","oklahoma","oregon",
            "pennsylvania","rhode island", "south carolina","south dakota","tennessee","texas",
            "utah","vermont","virginia","washington","west virginia","wisconsin","wyoming"
    };

    private static String[] abbvStates = {
            "ak","al", "ala","alaska","ar","ariz","ark","az","ca","calif","co","colo","con","conn",
            "ct","de","del","fl","fla","ga","hawaii","hi","ia","id","idaho",
            "il","ill","in","ind","iowa","kans","ks","ky","la","ma","maine","md","me","mass","mi",
            "mich","minn","miss","mn","mo","ms","mt","mont","n.h","n.j","n.m","n.y","n.c","n.d",
            "ne","nebr","nev","nc","nd","nh","nj","nm","nv","ny","oh","ohio","ok","okla","or","ore",
            "pa","penn","r.i","ri","s.c","s.d","sc","sd","tenn","tn","tex","tx","ut","utah","vt",
            "va","wa","wash","w.va","wi","wis","wv","wy","wyo"
            };

    static {
        Arrays.sort(abbvStates);
    }

    private static String[] canadaStates = {"alberta","british columbia","manitoba","new brunswick","newfoundland",
            "newfoundland and labrador","nova scotia","ontario","prince edward island","quebec","saskatchewan"};

    private static Map<String, String> synonyns = new HashMap<>();
    static {
        synonyns.put("holland","Netherlands");
        synonyns.put("prussia","Germany");
        synonyns.put("eng","England");
        synonyns.put("great britain","England");
        synonyns.put("gb","England");
        synonyns.put("northern ireland","Ireland");
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
        String[] parts = place.split("[,\\\\/]+");
        return parts[parts.length-1].trim().replaceAll("[<>\\[\\]\\(\\)\\.\"]+", "");
    }

    public static String getTopPlace(String place, int level) {
        if (place==null) return null;
        String[] parts = place.split("[,\\\\/]+");
        if (parts.length>=level) {
            return parts[parts.length - level].trim().replaceAll("[<>\\[\\]\\(\\)\\.\"]+", "");
        }
        return parts[parts.length - 1].trim().replaceAll("[<>\\[\\]\\(\\)\\.\"]+", "");
    }

    public static String getPlaceCountry(String p) {
        String place = PlaceHelper.getTopPlace(p);
        if (place == null || place.trim().isEmpty()) return UNKNOWN;
        if (!place.equalsIgnoreCase("United States") && PlaceHelper.isInUS(place))
            return "United States";

        if (place.equalsIgnoreCase("United Kingdom")) {
            place = getTopPlace(p, 2);
        }

        if (Arrays.binarySearch(canadaStates, place.toLowerCase())>=0) return "Canada";

        if (synonyns.containsKey(place.toLowerCase())) {
            place = synonyns.get(place.toLowerCase());
        }

        return place;
    }
}
