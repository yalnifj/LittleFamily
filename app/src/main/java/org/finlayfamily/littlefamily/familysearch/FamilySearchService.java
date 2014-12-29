package org.finlayfamily.littlefamily.familysearch;

/**
 * Created by Parents on 12/29/2014.
 */
public class FamilySearchService {
    private String sessionId = null;

    private static FamilySearchService ourInstance = new FamilySearchService();

    public static FamilySearchService getInstance() {
        return ourInstance;
    }

    private FamilySearchService() {
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
