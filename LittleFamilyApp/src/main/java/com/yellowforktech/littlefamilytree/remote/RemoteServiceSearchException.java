package com.yellowforktech.littlefamilytree.remote;

/**
 * Created by Parents on 12/29/2014.
 */
public class RemoteServiceSearchException extends Exception {
    private int statusCode;

    public RemoteServiceSearchException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public RemoteServiceSearchException(String message, int statusCode, Exception e) {
        super(message, e);
        this.statusCode = statusCode;
    }
}
