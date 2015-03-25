package org.finlayfamily.littlefamily.util;

import java.util.Arrays;
import java.util.Locale;

/**
 * Created by Parents on 3/24/2015.
 */
public class PlaceHelper {
    private static String[] usStates = {
            "alabama", "alaska", "arizona", "arkansas", "california", "colorado", "connecticut",
            "delaware","florida","georgia","hawaii","idaho","illinois","indiana","iowa","kansas",
            "kentucky","louisiana","maine","maryland","massachusetts","michigan","minnesota",
            "mississippi","missouri","montana","nebraska","nevada","new hampshire","new jersey",
            "new mexico","new york","north carolina","north dakota","ohio","oklahoma","oregon",
            "pennsylvania","rhode island", "south carolina","south dakota","tennessee","texas",
            "utah","vermont","virginia","washington","west virginia","wisconsin","wyoming"
    };

    private static String[] abbvStates = {
            "ala","alaska","ariz","ark","calif","colo","conn","del","fla","ga","hawaii","idaho",
            "ill","ind","iowa","kans","ky","la","maine","md","mass","mich","minn","miss","mo",
            "mont","nebr","nev","n.h","n.j","n.m","n.y","n.c","n.d","ohio","okla","ore",
            "pa","r.i","s.c","s.d","tenn","tex","utah","vt","va","wash","w.va","wis","wyo",
            "al","ak","az","ar","ca","co","ct","de","fl","ga","hi","id","il","in","ia","ks","ky",
            "la","me","md","ma","mi","mn","ms","mo","mt","ne","nv","nh","nj","nm","ny","nc","nd",
            "oh","ok","or","pa","ri","sc","sd","tn","tx","ut","vt","va","wa","wv","wi","wy"

            };

    static {
        Arrays.sort(abbvStates);
    }

    public static boolean isInUS(String place) {
        if (place.equals("United States")) return true;
        int i = Arrays.binarySearch(usStates, place);
        if (i>=0) return true;
        i = Arrays.binarySearch(abbvStates, place);
        return (i >= 0);
    }

    public static String getCountryLanguage(String country) {
        for(Locale locale : Locale.getAvailableLocales()) {
            if (locale.getDisplayCountry().equalsIgnoreCase(country)) {
                return locale.getDisplayLanguage();
            }
        }
        return country;
    }

    public static String getTopPlace(String place) {
        if (place==null) return null;
        String[] parts = place.split("[, ]+");
        return parts[parts.length-1];
    }
}
