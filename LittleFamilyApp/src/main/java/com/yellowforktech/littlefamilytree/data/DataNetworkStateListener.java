package com.yellowforktech.littlefamilytree.data;

/**
 * Created by jfinlay on 7/7/2015.
 */
public interface DataNetworkStateListener {
    public void remoteStateChanged(DataNetworkState state);
    public void statusUpdate(String status);
}
