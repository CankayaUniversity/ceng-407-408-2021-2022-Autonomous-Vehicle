package com.p4f.esp32camai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class listActivity extends AppCompatActivity {
    public static final String NAME = "NAME";
    private ArrayList<String> arrayList;
    public static final ArrayList<String> detections_list = new ArrayList<String>(10);
    public static String detections_list2 = new String();
    private static final String TAG = "";
    public TextView txt;
    private ArrayAdapter<String> adapter;
    String my_txt = "hey";
    public String name;
    ListView my_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        txt = (TextView) findViewById(R.id.textView4);
       // my_list = (ListView) findViewById(R.id.detectlist);
        //adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_list2, (List<String>) my_list);

        setContentView(R.layout.activity_list2);
        txt = findViewById(R.id.textView4);
        Intent i = getIntent();
        //my_list.setAdapter(adapter);
        //for (int j = 0; j < MainActivity.general_obj_cnt; j++) {
            my_txt = i.getStringExtra(NAME);
            txt.setText(my_txt);
           // arrayList.add(my_txt);
            //next thing you have to do is check if your adapter has changed
          //  adapter.notifyDataSetChanged();
        //}
    }
}
