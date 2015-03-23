package org.finlayfamily.littlefamily.familysearch;

/**
 * Created by Parents on 12/29/2014.
 */
public class FamilySearchException extends Exception {
    private int statusCode;

    public FamilySearchException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public FamilySearchException(String message, int statusCode, Exception e) {
        super(message, e);
        this.statusCode = statusCode;
    }
}
