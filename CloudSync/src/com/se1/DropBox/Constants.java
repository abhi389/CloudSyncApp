package com.se1.DropBox;

import com.dropbox.client2.session.Session.AccessType;

public class Constants {

	public static final String OVERRIDEMSG = "File name with this name already exists.Do you want to replace this file?";
	final static public String DROPBOX_APP_KEY = "oujw7a401lgnthn";
	final static public String DROPBOX_APP_SECRET = "55u78g0394dnh40";
	public static boolean mLoggedIn = false;

	final static public AccessType ACCESS_TYPE = AccessType.DROPBOX;

	final static public String ACCOUNT_PREFS_NAME = "prefs";
	final static public String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static public String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    final static public String ONEDRIVE_STATUS = "OneDrive_status";
    final static public String ONEDRIVE_ADDED = "OneDrive_Added";
//	public static final int BOOKMARKS = 4;
//	public static final int OPENFILE = 5;

}
