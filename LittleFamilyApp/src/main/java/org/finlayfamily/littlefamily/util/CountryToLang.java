package org.finlayfamily.littlefamily.util;

import java.util.Locale;

public class CountryToLang {
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
