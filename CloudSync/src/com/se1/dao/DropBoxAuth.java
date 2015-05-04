package com.se1.dao;



import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;

import java.io.Serializable;

/**
 * Created by Abhitej on 3/28/2015.
 */
public  class   DropBoxAuth implements Serializable {

    public DropboxAPI<AndroidAuthSession> getmApi() {
        return mApi;
    }

    public void setmApi(DropboxAPI<AndroidAuthSession> mApi) {
        this.mApi = mApi;
    }

    private DropboxAPI<AndroidAuthSession> mApi;

}
