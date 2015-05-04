/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.se1.navdrawer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.se1.Activity.SettingsActivity;
import com.se1.DropBox.Constants;
import com.se1.DropBox.DropBoxLogin;
import com.se1.dao.DatabaseOperation;
import com.se1.dao.User;
import com.se1.main.MainActivity;
import com.se1.main.R;
import com.se1.oneDrive.SignInActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class NavigationMainActivity extends SherlockFragmentActivity {
    DrawerLayout mDrawerLayout;
    LinearLayout linearLayout;
    ListView mDrawerList;
    ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mPlanetTitles;
    public static DropboxAPI<AndroidAuthSession> getmApi() {
        return mApi;
    }


    public static void setmApi(DropboxAPI<AndroidAuthSession> mApi) {
        NavigationMainActivity.mApi = mApi;
    }
    private DatabaseOperation datasource;
    private static DropboxAPI<AndroidAuthSession> mApi;
    private SimpleAdapter mAdapter;
    private List<HashMap<String,String>> mList ;
    final private String TITLE = "title";
    final private String LEFTIMAGE = "leftImage";
    final private String RIGHTIMAGE = "rightImage";
    int[] mFlags = new int[]{
            R.drawable.home,
            R.drawable.title_dropbox_icon,
            R.drawable.onedrive_orig,
            R.drawable.profile_icon,
            R.drawable.logout_icon,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        datasource = new DatabaseOperation(this);
        datasource.open();
        setContentView(R.layout.activity_navbar_main);
        mTitle = mDrawerTitle = getTitle();
        mPlanetTitles =getResources().getStringArray(R.array.menu_list);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         linearLayout = (LinearLayout) findViewById(R.id.linear_Layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        final TextView textViewToChange = (TextView) findViewById(R.id.userNameText);
        textViewToChange.setText("  Welcome  "+((User)datasource.getUserDetail()).getFirstName()+" !");
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        // set up the drawer's list view with items and click listener
        //mDrawerList.setAdapter(new ArrayAdapter<String>(this,
        //		R.layout.drawer_list_item, mPlanetTitles));
        int[] mCount = null;
        SharedPreferences prefs = getSharedPreferences(
                Constants.ONEDRIVE_STATUS, 0);
        String oneDriveAdded = prefs.getString(Constants.ONEDRIVE_ADDED, null);
        boolean isDropboxAdded= new DatabaseOperation(this).isdropBoxAdded();
        if(isDropboxAdded && oneDriveAdded != null && oneDriveAdded.equalsIgnoreCase("Yes") )
        {
            mCount = new int[]{
                    0,
                    R.drawable.cloud_added,
                    R.drawable.cloud_added,
                    0,
                    0,
            };
        }
        else if(isDropboxAdded )
        {
            mCount = new int[]{
                    0,
                    R.drawable.cloud_added,
                    R.drawable.cloud_not_added,
                    0,
                    0,
            };
        }
        else if((oneDriveAdded != null && oneDriveAdded.equalsIgnoreCase("Yes")) ) {
            mCount = new int[]{
                    0,
                    R.drawable.cloud_not_added,
                    R.drawable.cloud_added,
                    0,
                    0,
            };

        }
        else
        {
            Log.d("at the else","here");
            mCount = new int[]{
                    0,
                    R.drawable.cloud_not_added,
                    R.drawable.cloud_not_added,
                    0,
                    0,
            };
        }

        Log.d("at the end","here");
        mList = new ArrayList<HashMap<String,String>>();
        for(int i=0;i<mPlanetTitles.length;i++){
            HashMap<String, String> hm = new HashMap<String,String>();
            hm.put(TITLE, mPlanetTitles[i]);
            hm.put(RIGHTIMAGE, Integer.toString(mCount[i]));
            hm.put(LEFTIMAGE, Integer.toString(mFlags[i]) );
            mList.add(hm);
        }

        int[] to = { R.id.leftImage , R.id.title , R.id.rightImage};

        String[] from = { LEFTIMAGE,TITLE,RIGHTIMAGE };

        mAdapter = new SimpleAdapter(getActionBar().getThemedContext(), mList, R.layout.drawer_list_item, from, to);


        mDrawerList.setAdapter(mAdapter);



         mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open, /* "open drawer" description for accessibility */
                R.string.drawer_close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to
                //onPrepareOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to
               // onPrepareOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(
            com.actionbarsherlock.view.MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home: {
                if (mDrawerLayout.isDrawerOpen(linearLayout)) {
                    mDrawerLayout.closeDrawer(linearLayout);
                } else {
                    mDrawerLayout.openDrawer(linearLayout);
                }
                break;
            }

        }

        return super.onOptionsItemSelected(item);
    }

    // The click listener for ListView in the navigation drawer
    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            selectItem(position);

        }
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void selectItem(int position) {

        switch (position) {
            case 0: setmApi(DropBoxLogin.getmApi());
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.content,
                                PageSlidingTabStripFragment.newInstance(),
                                PageSlidingTabStripFragment.TAG).commit();
                        break;
            case 1:
                Intent dropboxIntent = new Intent(this, DropBoxLogin.class);
                dropboxIntent.putExtra("AlreadyRegister","No");
                startActivity(dropboxIntent);
                break;
            case 2:
                Intent oneDriveIntent = new Intent(this, SignInActivity.class);
                startActivity(oneDriveIntent);
                break;
            case 3:
                startActivity(new Intent(this,SettingsActivity.class));
                break;
            case 4:
                DatabaseOperation datasource;
                datasource = new DatabaseOperation(this);
                datasource.open();
                User user=datasource.getUserDetail();
                if(user!=null && ((user.getEmailId()!= null || user.getEmailId().equalsIgnoreCase("")) && user.getLoggedIn() == 1 ))
                {
                    Toast.makeText(getApplicationContext(), "You have successfully Logout",
                            Toast.LENGTH_LONG).show();
                    datasource.removeSignIn(user.getEmailId());
                }
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("Logout","Yes");
                startActivity(intent);
                break;
        }
        mDrawerList.setItemChecked(position, false);
        mDrawerLayout.closeDrawer(linearLayout);
    }



}