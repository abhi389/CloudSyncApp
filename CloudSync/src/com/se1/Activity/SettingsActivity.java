package com.se1.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.TokenPair;
import com.se1.DropBox.Constants;
import com.se1.DropBox.DropboxDownload;
import com.se1.DropBox.UploadFile;
import com.se1.DropBox.Utils;
import com.se1.dao.DatabaseOperation;
import com.se1.dao.User;
import com.se1.main.MainActivity;
import com.se1.main.R;

import java.io.File;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SettingsActivity extends SherlockFragmentActivity  {
    private DatabaseOperation datasource;
    private static final String ALPHA_PATTERN ="[a-zA-Z]+";
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.title_activity_settings);

        setContentView(R.layout.activity_home);
        datasource = new DatabaseOperation(this);
        datasource.open();
        Button editProfile = (Button)findViewById(R.id.editProfile);
        final EditText firstName   = (EditText)findViewById(R.id.firstNameId);
        final EditText lastName   = (EditText)findViewById(R.id.lastNameId);
        final EditText email   = (EditText)findViewById(R.id.email);
        User user=datasource.getUserDetail();
        firstName.setText(user.getFirstName(), TextView.BufferType.EDITABLE);
        lastName.setText(user.getLastName(), TextView.BufferType.EDITABLE);
        email.setText(user.getEmailId(), TextView.BufferType.EDITABLE);
        editProfile.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {

                        User user=datasource.getUserDetail();

                        if(( (firstName.getText()== null)
                                || lastName.getText()== null
                                || (email.getText()== null )

                        ))
                        {

                            Toast.makeText(getApplicationContext()," Enter all fields!",
                                    Toast.LENGTH_LONG).show();
                        }
                        else if(email.getText().toString() != null )
                        {
                            Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
                            Matcher matcher = emailPattern.matcher(email.getText().toString());
                            Boolean emailMatches = matcher.matches();
                            Pattern alphaPattern = Pattern.compile(ALPHA_PATTERN);
                            Matcher fnameMatch = alphaPattern.matcher(firstName.getText().toString());
                            Matcher lnameMatch = alphaPattern.matcher(lastName.getText().toString());

                            if(!emailMatches){
                                Toast.makeText(getApplicationContext(),"Enter a valid email id.",
                                        Toast.LENGTH_LONG).show();
                            }
                            else if(!fnameMatch.matches() || !lnameMatch.matches()){
                                Toast.makeText(getApplicationContext(),"Only characters are allowed for first and last name.",
                                        Toast.LENGTH_LONG).show();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "You have been successfully changed Your Profile!",
                                        Toast.LENGTH_LONG).show();
                                datasource.editProfile(email.getText().toString(),firstName.getText().toString(),lastName.getText().toString()) ;
                                goToHomePage();
                            }
                        }
                    }



                });

    }





    //Navigate to registration page
    public void goToResetPassword(View view)
    {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        startActivity(intent);
    }
    //Navigate to Home page
    public void goToHomePage()
    {
        Intent intent = new Intent(this, com.se1.navdrawer.NavigationMainActivity.class);
        startActivity(intent);
    }





}
