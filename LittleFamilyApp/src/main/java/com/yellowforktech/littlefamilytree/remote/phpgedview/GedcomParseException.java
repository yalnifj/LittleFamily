package com.yellowforktech.littlefamilytree.remote.phpgedview;

/**
 * Created by jfinlay on 4/24/2015.
 */
public class GedcomParseException extends Exception {
    private String message;
    public GedcomParseException(String detailMessage) {
        super(detailMessage);
        this.message = detailMessage;
    }
}
