package org.finlayfamily.littlefamily.familysearch;

/**
 * Created by jfinlay on 12/30/2014.
 */
public class FSResult {
    private int statusCode;
    private String data;
    private boolean success=false;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
