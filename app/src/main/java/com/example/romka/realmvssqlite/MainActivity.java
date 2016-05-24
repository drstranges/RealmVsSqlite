package com.example.romka.realmvssqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.romka.realmvssqlite.db.DatabaseHelper;
import com.example.romka.realmvssqlite.db.table.SingleTestModelTable;
import com.example.romka.realmvssqlite.model.SingleTestModel;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

@SuppressWarnings("ConstantConditions")
public class MainActivity extends AppCompatActivity {

    private TextView tvRealmResult;
    private TextView tvSqLiteResult;
    private EditText etItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvRealmResult = (TextView) findViewById(R.id.realmResult);
        tvSqLiteResult = (TextView) findViewById(R.id.sqliteResult);
        etItemCount = (EditText) findViewById(R.id.etItemCount);

        findViewById(R.id.btnTestRealm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testRealm();
            }
        });

        findViewById(R.id.btnTestSqlite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testSqlite();
            }
        });

        initRealm();
        initSqLite();
    }

    private void initRealm() {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getApplicationContext()).build();
        Realm.setDefaultConfiguration(realmConfig);
    }

    private void initSqLite() {
//        DatabaseHelper.getInstance(getApplicationContext()).openSingleTestModelTable()
    }

    private List<SingleTestModel> generateTestModels() {
        final int itemCount = Integer.parseInt(etItemCount.getText().toString());
        List<SingleTestModel> models = new ArrayList<>(itemCount);
        for (int i=0; i < itemCount; i++) {
            models.add(new SingleTestModel(i, "Label_" + i, ((long) i)*2));
        }
        return models;
    }

    private void showResult(String label, TextView textView, long... timeProbes) {

        final StringBuilder builder = new StringBuilder();
        long oldProbe = -1L;
        for (long timeProbe : timeProbes) {
            if (oldProbe == -1L) {
                builder.append((timeProbes[timeProbes.length -1] - timeProbes[0]) / 1000000);
            } else {
                builder.append(", ").append((timeProbe - oldProbe) / 1000000);
            }
            oldProbe = timeProbe;
        }
        final String message = builder.toString();
        textView.setText(message);
        Log.d("LOG_TAG", label + ": " + message);
    }

    private void testRealm() {
        List<SingleTestModel> models = generateTestModels();

        Realm realm = Realm.getDefaultInstance();

        long timeProbe1 = System.nanoTime();
        realm.beginTransaction();
        realm.delete(SingleTestModel.class);
        realm.commitTransaction();
        long timeProbe2 = System.nanoTime();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(models);
        realm.commitTransaction();
        long timeProbe3 = System.nanoTime();

        final RealmResults<SingleTestModel> sorted = realm.where(SingleTestModel.class).between("longValue", 100, 1500).findAllSorted("label", Sort.DESCENDING);
        long timeProbe4 = System.nanoTime();

        final List<SingleTestModel> resultModels = realm.copyFromRealm(sorted);
        long timeProbe5 = System.nanoTime();

        realm.close();
        showResult("Realm", tvRealmResult, timeProbe1, timeProbe2, timeProbe3, timeProbe4, timeProbe5);
    }

    private void testSqlite() {
        List<SingleTestModel> models = generateTestModels();


        final DatabaseHelper helper = DatabaseHelper.getInstance(getApplicationContext());
        final SingleTestModelTable singleTestModelTable = helper.openSingleTestModelTable(true);

        long timeProbe1 = System.nanoTime();
        singleTestModelTable.deleteAll();
        long timeProbe2 = System.nanoTime();

        final ContentValues[] contentValues = helper.convertToCv(models, SingleTestModel.class);
        singleTestModelTable.bulkInsertOrUpdate(contentValues);
        long timeProbe3 = System.nanoTime();

        final Cursor sorted = singleTestModelTable.getAll(null, "longValue >= ? and longValue <= ?", new String[]{String.valueOf(100), String.valueOf(1500)}, "label desc", null);
        long timeProbe4 = System.nanoTime();

        final List<SingleTestModel> resultModels = singleTestModelTable.getList(sorted, true);
        long timeProbe5 = System.nanoTime();

        showResult("SqLite", tvSqLiteResult, timeProbe1, timeProbe2, timeProbe3, timeProbe4, timeProbe5);
    }
}
