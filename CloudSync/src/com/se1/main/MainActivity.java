package com.se1.main;

import android.app.Application;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.se1.dao.DatabaseOperation;
import com.se1.dao.Mail;
import com.se1.dao.User;

import java.util.Random;


public class MainActivity extends SherlockFragmentActivity {
    private DatabaseOperation datasource;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button loginButton = (Button)findViewById(R.id.login);
        final EditText password   = (EditText)findViewById(R.id.password);
        final EditText email   = (EditText)findViewById(R.id.email);
        final CheckBox loggedIn=(CheckBox)findViewById(R.id.loggedIn);
        final TextView signUp   = (TextView)findViewById(R.id.signUp);
        signUp.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view) {
                        goToRegistration(view);
                    }


                });
        datasource = new DatabaseOperation(this);
        datasource.open();
        Intent myIntent = getIntent();
        String logout = myIntent.getStringExtra("Logout");
        User user=datasource.getUserDetail();

        if(user!=null && ((user.getEmailId()!= null || user.getEmailId().equalsIgnoreCase("")) && user.getLoggedIn() == 1 ))//logged button checked
        {
            //Toast.makeText(getApplicationContext(), "already checked loggedIn"+user.getLoggedIn(),
            //
            //    Toast.LENGTH_LONG).show();
            Log.d("user.getLoggedIn()","inside");
            if(logout == null)
                goToHomePage();
            else if(logout != null && !logout.equalsIgnoreCase("Yes"))
                goToHomePage();

        }

        if(user!=null && ((user.getEmailId()!= null)))
        {
            signUp.setVisibility(View.GONE);
        }
        //Start: Check email Id is valid or not
        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    Is_Valid_Email(email); // pass your EditText Obj here.
                }
            }
            public void Is_Valid_Email(EditText editText) {
                String valid_email = null;
                if (editText.getText().toString() == null) {
                    editText.setError("Invalid Email Address");
                    valid_email = null;
                } else if (isEmailValid(editText.getText().toString()) == false) {
                    editText.setError("Invalid Email Address");
                    valid_email = null;
                } else {
                    valid_email = editText.getText().toString();
                }
            }
            boolean isEmailValid(CharSequence email) {
                return android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                        .matches();
            }
        });
        //End Check email Id is valid or not
        loginButton.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view) {
                        if(!Is_Valid_Email(email))
                        {

                            Toast.makeText(getApplicationContext(), "Please Enter valid Email Id",
                                    Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            User user = datasource.getUserDetail();

                            if (user != null && user.getEmailId().equalsIgnoreCase(email.getText().toString()))
                            {
                                if(user.getPassword().equals(password.getText().toString()))
                                {
                                    int loggedInValue = (loggedIn.isChecked()) ? 1 : 0;
                                    Log.d("loggedIn", "" + loggedInValue);
                                    if (loggedInValue == 1) {
                                        datasource.addSignIn(user.getEmailId());
                                    }
                                    goToHomePage();
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(),"Please Enter Valid Password",
                                            Toast.LENGTH_LONG).show();
                                }
                                //Toast.makeText(getApplicationContext(),""+user.getLoggedIn(),
                                //       Toast.LENGTH_LONG).show();
                                //Toast.makeText(getApplicationContext(), user.getEmailId().toString(),
                                //     Toast.LENGTH_LONG).show();
                            }
                            else
                                Toast.makeText(getApplicationContext(), "Please Register to Application",
                                        Toast.LENGTH_LONG).show();
                        }
                    }

                    public boolean Is_Valid_Email(EditText editText) {
                        String valid_email = null;
                        if (editText.getText().toString() == null) {
                            valid_email = null;
                            return false;
                        } else if (isEmailValid(editText.getText().toString()) == false) {
                            valid_email = null;
                            return false;
                        } else {
                            valid_email = editText.getText().toString();
                            return true;
                        }
                    }
                    boolean isEmailValid(CharSequence email) {
                        return android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                                .matches();
                    }
                });

        Button forgotPassword = (Button) findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        // send email
                        Log.d("here","here");
                        Mail mail = new Mail("cloud360user@gmail.com", "cloudsync");
                        final User user = datasource.getUserDetail();
                        if (user != null && user.getEmailId() != "") {
                            Random r = new Random();
                            int randomNo = r.nextInt(99999 - 999) + 999;
                            String[] toArr = {user.getEmailId(), user.getEmailId()};

                            mail.set_to(toArr);
                            mail.set_from("cloud360user@gmail.com");
                            mail.set_subject("This is an email sent using my Mail JavaMail wrapper from an Android device.");
                            mail.setBody("Your New Password is"+randomNo);
                            try {
                                if (mail.send()) {
                                    runOnUiThread(new Runnable()
                                    {
                                        public void run()
                                        {
                                            Toast.makeText(getApplicationContext(), "New Password sent to "+user.getEmailId(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    datasource.forgotPassword(user.getEmailId(),randomNo);

                                } else {
                                    /*runOnUiThread(new Runnable()
                                    {
                                        public void run()
                                        {
                                            Toast.makeText(getApplicationContext(), "Email was not sent.",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });*/

                                }
                            } catch (Exception e) {
                                //Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
                                Log.e("MailApp", "Could not send email", e);
                            }
                        }
                    }
                };
                Thread t = new Thread(r);
                t.start();
            }
        });





    }
    @Override
    public void onBackPressed() {

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
        startActivity(intent);
        finish();
        System.exit(0);
    }
    //Navigate to registration page
    public void goToRegistration(View view)
    {
        Intent intent = new Intent(this, com.se1.Activity.RegisterUserActivity.class);
        startActivity(intent);
    }
    //Navigate to home page
    public void goToHomePage()
    {
        Intent intent = new Intent(MainActivity.this, com.se1.navdrawer.NavigationMainActivity.class);
        MainActivity.this.startActivity(intent);
    }

}
