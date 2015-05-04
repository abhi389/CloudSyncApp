package com.se1.DropBox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.TokenPair;
import com.se1.dao.DatabaseOperation;
import com.se1.main.R;
import com.se1.navdrawer.NavigationMainActivity;

import java.io.File;


public class DropBoxLogin extends SherlockFragmentActivity implements OnClickListener {
    private static final int TAKE_PHOTO = 1;
    private Button dropboxLogin;
    private final String DIR = "/";
    private File f;
    private boolean mLoggedIn, onResume;
    private static DropboxAPI<AndroidAuthSession> mApi;
    public static DropboxAPI<AndroidAuthSession> getmApi() {
        return mApi;
    }

    public void setmApi() {
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dropbox_login_main);

        setmApi();

        // checkAppKeySetup();

        setLoggedIn(false);

        Intent myIntent = getIntent(); // gets the previously created intent
        String alreadyRegister = myIntent.getStringExtra("AlreadyRegister");
        Log.d("start","start");
        if(alreadyRegister.equalsIgnoreCase("Yes"))
        {
            checkAppKeySetup();
            if (!Constants.mLoggedIn) {
                mApi.getSession().startAuthentication(DropBoxLogin.this);
            }
            else
            {
                showToast("Already connected with Dropbox:");
            }
        }
        else {
            dropboxLogin = (Button) findViewById(R.id.dropbox_login);
            dropboxLogin.setOnClickListener(this);
        }

    }
    private void checkAppKeySetup() {
        if (Constants.DROPBOX_APP_KEY.startsWith("CHANGE")
                || Constants.DROPBOX_APP_SECRET.startsWith("CHANGE")) {
            showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
            finish();
            return;
        }
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + Constants.DROPBOX_APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm =getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
            showToast("URL scheme in your app's "
                    + "manifest is not set up correctly. You should have a "
                    + "com.dropbox.client2.android.AuthActivity with the "
                    + "scheme: " + scheme);

            finish();
        }
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(Constants.DROPBOX_APP_KEY,
                Constants.DROPBOX_APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0],
                    stored[1]);
            session = new AndroidAuthSession(appKeyPair, Constants.ACCESS_TYPE,
                    accessToken);
        } else {
            Log.d("in buildSession","buildSession");
            session = new AndroidAuthSession(appKeyPair, Constants.ACCESS_TYPE);
        }

        return session;
    }

    private String[] getKeys() {
        SharedPreferences prefs = getSharedPreferences(
                Constants.ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(Constants.ACCESS_KEY_NAME, null);
        String secret = prefs.getString(Constants.ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
            String[] ret = new String[2];
            ret[0] = key;
            ret[1] = secret;
            return ret;
        } else {
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == dropboxLogin) {
            checkAppKeySetup();
            if (!Constants.mLoggedIn) {

                mApi.getSession().startAuthentication(DropBoxLogin.this);

            }
            else
            {
                showToast("Already connected with Dropbox:");
            }
            //createDir();
			/*if (mLoggedIn) {
				logOut();
			}*/
            /*
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			f = new File(Utils.getPath(),new Date().getTime()+".jpg");
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
            */
            //startActivityForResult(intent, TAKE_PHOTO);
        }
    }

    private void logOut() {
        mApi.getSession().unlink();

        clearKeys();
    }

    private void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(
                Constants.ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("in result","here");
        if (resultCode == RESULT_OK) {
            if (requestCode == TAKE_PHOTO) {
//				f = new File(Utils.getPath() + "/temp.jpg");
                if (Utils.isOnline(DropBoxLogin.this)) {
                    mApi.getSession().startAuthentication(DropBoxLogin.this);
                    onResume = true;
                } else {
                    Utils.showNetworkAlert(DropBoxLogin.this);
                }
            }
        }
    }
    public void setLoggedIn(final boolean loggedIn) {
        /*mLoggedIn = loggedIn;
        if (loggedIn) {
            //instead of f send file from sd card
            UploadFile upload = new UploadFile(DropBoxLogin.this, mApi, DIR, f);
            upload.execute();
            onResume = false;

        }
        */
        Constants.mLoggedIn = loggedIn;


    }
    private void storeKeys(String key, String secret) {
        SharedPreferences prefs = getSharedPreferences(
                Constants.ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.putString(Constants.ACCESS_KEY_NAME, key);
        edit.putString(Constants.ACCESS_SECRET_NAME, secret);
        edit.commit();

    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    @Override
    protected void onResume() {


        AndroidAuthSession session = mApi.getSession();

        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();

                TokenPair tokens = session.getAccessTokenPair();
                storeKeys(tokens.key, tokens.secret);
                setLoggedIn(true);

                new DatabaseOperation(getApplicationContext()).dropBoxAdded(1);
                Intent intent = new Intent(DropBoxLogin.this,NavigationMainActivity.class);
                startActivity(intent);
            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:"
                        + e.getLocalizedMessage());
            }
        }
        super.onResume();
    }




}
