package edu.wmdd.sqlite_example;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getName();
    private RentalDBHelper helper = null;
    private SQLiteDatabase db = null;
    String edditedString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the database, potentially creating it
        helper = new RentalDBHelper(this);
        db = helper.getReadableDatabase();

        // Only populate the db if it is empty
        Cursor c = db.rawQuery("SELECT count(*) FROM issues", null);
        c.moveToFirst();
        if (c.getInt(0) == 0) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    // We have to init the data in a separate thread because of networking
                    helper.initData();

                    // We are now ready to initialize the view on the UI thread
                    runOnUiThread(() -> {
                        initView();
                    });
                }
            };
            t.start();
        } else {
            // We are already inside the UI thread
            initView();
        }
        c.close();
    }

    private void initView() {
        Spinner tv = findViewById(R.id.spinnerTextView);
        ArrayList<String> areas = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT DISTINCT(area) FROM issues", null);
        while (cursor.moveToNext()) {
            String area = cursor.getString(0);
            areas.add(area);
        }
        cursor.close();
        ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, areas);

        tv.setAdapter(areaAdapter);
        tv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedArea = ((TextView) view).getText().toString();
                Cursor cursor1 = db.rawQuery("SELECT operator FROM issues WHERE area = ?", new String[]{selectedArea});
                ArrayList<String> operators = new ArrayList<>();
                while (cursor1.moveToNext()) {
                    String operator = cursor1.getString(0);
//                    String businessURL = cursor1.getString(1);
                    operators.add(operator);
//                    operators.add(businessURL);
//                    Log.e("url", businessURL);
                }

                ListView lv = findViewById(R.id.listView);


                ArrayAdapter<String> operatorsAdapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_dropdown_item_1line, operators);
                lv.setAdapter(operatorsAdapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                        String selectedArea = ((TextView) view).getText().toString();

                        String operator = operatorsAdapter.getItem(i);
//                        Log.e("oper", operator);
                        Cursor cursor2 = db.rawQuery("SELECT businessURL FROM issues WHERE operator = '" + operator +"' LIMIT 1",null);
                        while (cursor2.moveToNext()){
                            String businessURL  = cursor2.getString(0);
                            Log.e("url", businessURL);

                            addChar(businessURL,'s',4 );
                            Log.e("editedString", edditedString );

                            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                            intent.putExtra("url", edditedString);
                            startActivity(intent);
                        }
                    }
                });

//                TextView textView = findViewById(R.id.textView);
//                textView.setText(operators.stream().reduce("", (s, s2) -> s + "\n" + s2));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void addChar(String str, char ch, int position) {
        StringBuilder sb = new StringBuilder(str);
        sb.insert(position, ch);
        edditedString =  sb.toString();
    }

}
