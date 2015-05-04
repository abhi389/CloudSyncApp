package com.se1.navdrawer;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxInputStream;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.TokenPair;
import com.se1.DropBox.Constants;
import com.se1.DropBox.DownloadFileAdapter;
import com.se1.DropBox.DropboxDownload;
import com.se1.DropBox.DropboxDownloadImages;
import com.se1.DropBox.Utils;
import com.se1.main.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class SinglePanelRepresent extends SherlockFragment implements OnItemClickListener {
    private DropboxAPI<AndroidAuthSession> mApi;
    public  void setDataFromLogin( DropboxAPI<AndroidAuthSession> api) {
        mApi = api;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Log.d("in dropbox Download","inside");
            View v =inflater.inflate(R.layout.single_panel_repr,container,false);
            v.setFocusableInTouchMode(true);
            v.requestFocus();


            DropboxDownload dropboxDownload=new DropboxDownload();//create the fragment instance for the top fragment
            dropboxDownload.setDataFromLogin(mApi);
            // Middle_Fragment frg1=new Middle_Fragment();//create the fragment instance for the middle fragment
            //Bottom_Fragment frg2=new Bottom_Fragment();//create the fragment instance for the bottom fragment
        SuperAwesomeCardFragment superAwesomeCardFragment= new SuperAwesomeCardFragment();

            /*
            FragmentTransaction transaction=getChildFragmentManager().beginTransaction();//create an instance of fragment manager

            transaction.add(R.id.My_Container_1_ID, dropboxDownload, "Frag_Top_tag");
            transaction.add(R.id.My_Container_2_ID, superAwesomeCardFragment, "Frag_Middle_tag");
            //transaction.add(R.id.My_Container_3_ID, frg2, "Frag_Bottom_tag");


            transaction.commit();
            */
            return v;
    }
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {


    }





}
