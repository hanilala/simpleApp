package com.lan.drawerlayout.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;

import com.lan.drawerlayout.R;

/**
 * Created by Administrator on 2015/10/02.
 */
public class test extends Activity {

    private Button bt;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.test);

        bt= (Button) findViewById(R.id.bt);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(test.this,SurfaceActi.class);
                startActivity(i);


            }
        });
    }
}
