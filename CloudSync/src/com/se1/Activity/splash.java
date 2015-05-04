package com.se1.Activity;


import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.se1.main.MainActivity;
import com.se1.main.R;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;

import static java.lang.Thread.sleep;


public class splash extends Activity
{
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.splashscreen);
            Thread t1 = new Thread()
            {
                public void run()
                {
                    try {
                        sleep(2000);

                        Intent x = new Intent(splash.this, MainActivity.class);
                        startActivity(x);
                        finish();
                        }
                    catch(InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                }
            };
            t1.start();
        }
}