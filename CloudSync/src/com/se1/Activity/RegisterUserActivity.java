package com.se1.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.se1.dao.DatabaseOperation;
import com.se1.dao.User;
import com.se1.main.R;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterUserActivity extends SherlockFragmentActivity {
    private DatabaseOperation datasource;
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final String ALPHA_PATTERN =
            "[a-zA-Z]+";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        getSupportActionBar().setTitle(R.string.title_activity_register_user);
        datasource = new DatabaseOperation(this);
        datasource.open();
        Button registerUser = (Button)findViewById(R.id.registerUser);
        final EditText password   = (EditText)findViewById(R.id.password);
        final EditText firstName   = (EditText)findViewById(R.id.firstNameId);
        final EditText lastName   = (EditText)findViewById(R.id.lastNameId);
        final EditText email   = (EditText)findViewById(R.id.email);
        final CheckBox loggedIn=(CheckBox)findViewById(R.id.loggedIn);
        final EditText confirmPassword   = (EditText)findViewById(R.id.confirmPassword);


        registerUser.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        int loggedInValue = (loggedIn.isChecked())? 1 : 0;
                        User user=datasource.getUserDetail();

                        if(user!=null && ( (firstName.getText()== null)
                                || lastName.getText()== null
                                || (email.getText()== null )
                                ||(password.getText()==null
                        )
                                ))
                        {

                            Toast.makeText(getApplicationContext()," Enter all fields!",
                                    Toast.LENGTH_LONG).show();
                        }
                        else if(email.getText().toString() != null || password.getText().toString() != null )
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
                            else if(!password.getText().toString().equals(confirmPassword.getText().toString()))
                            {
                                Toast.makeText(getApplicationContext(),"Password and Confirm Password are not matching",
                                        Toast.LENGTH_LONG).show();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "You have been successfully registered!",
                                        Toast.LENGTH_LONG).show();
                                datasource.insertUser(email.getText().toString(),password.getText().toString(),loggedInValue,firstName.getText().toString(),lastName.getText().toString()) ;
                                goToHomePage();
                            }
                        }
                    }



                });
    }

    //Navigate to Home page
    public void goToHomePage()
    {
        Intent intent = new Intent(this, com.se1.navdrawer.NavigationMainActivity.class);
        startActivity(intent);
    }


}
