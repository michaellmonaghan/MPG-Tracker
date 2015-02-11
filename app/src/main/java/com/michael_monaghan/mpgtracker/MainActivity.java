package com.michael_monaghan.mpgtracker;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Arrays;

import michael_monaghan.mpgtracker.R;


public class MainActivity extends Activity implements ActionBar.OnNavigationListener {

    // Database
    private MileageDatabase mdb;

    // Current Car
    private String[] carNames;
    private int carIndex = 0;

    // Loaders
    LoadMileageEntriesTask lastEntriesLoader;

    // Views
    private ListView list;
    private ProgressBar loading;

    // Adapters
    private MileageEntriesAdapter entriesAdapter;

    //ViewHandlers
    private UserEntryViewHandler userEntryViewHandler;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up Views
        setContentView(R.layout.activity_main);
        list = (ListView) findViewById(R.id.list);
        loading = (ProgressBar) findViewById(R.id.loading);
        showLoading();

        // Set up Database
        mdb = new MileageDatabase(this);
        carNames = mdb.getCarNames();

        // Set up action bar
        ActionBar ab = getActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<String> carsAdapter = new ArrayAdapter<String>(ab.getThemedContext(), android.R.layout.simple_spinner_item, carNames);

        carsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ab.setListNavigationCallbacks(carsAdapter, this);

        // Set up cars list
        entriesAdapter = new MileageEntriesAdapter(this);
        View inputView = getLayoutInflater().inflate(R.layout.user_entry_input,null);
        userEntryViewHandler = new UserEntryViewHandler(inputView, new UserEntryViewHandler.AddUserEntryListener() {
            @Override
            public void addEntry(MileageDatabase.UserEntry entry) {
                performAddEntry(entry);
            }
        });
        list.addHeaderView(inputView);
        registerForContextMenu(list);
        list.setAdapter(entriesAdapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (((AdapterView.AdapterContextMenuInfo)menuInfo).id != -1) {
            menu.add(Menu.NONE, 0, Menu.NONE, R.string.delete);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Toast.makeText(this, R.string.entry_deleted, Toast.LENGTH_SHORT).show();
        performDeleteEntry(menuInfo.id);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        if (i < carNames.length) {
            loadCarMileageEntries(carNames[i]);
            carIndex = i;
        } else {
            showLoading();
            performAddNewCar();
        }
        return true;
    }

    public void performAddNewCar() {
        //TODO implement.
    }

    private void performAddEntry(MileageDatabase.UserEntry entry) {
        userEntryViewHandler.clear();
        mdb.addEntry(carNames[carIndex], entry);
        Toast.makeText(this, R.string.entry_added, Toast.LENGTH_LONG).show();
        loadCarMileageEntries();
    }

    private void performDeleteEntry(long id) {
        mdb.deleteEntry(carNames[carIndex], id);
        loadCarMileageEntries();
    }

    private void loadCarMileageEntries() {
        loadCarMileageEntries(carNames[carIndex]);
    }


    /**
     * Loads a MileageDatabase
     */
    private class LoadMileageEntriesTask extends AsyncTask<String, Void, MileageDatabase.MileageEntry[]> {

        @Override
        protected MileageDatabase.MileageEntry[] doInBackground(String... strings) {
            Log.d(this.toString(), "Loading...");
            return mdb.getMileageEntries(strings[0]);
        }
        
        @Override
        protected void onPostExecute(MileageDatabase.MileageEntry[] entries) {
            updateCarMileageEntries(entries);
        }
    }

    private void loadCarMileageEntries(String carName) {
        showLoading();
        if (lastEntriesLoader != null) {
            lastEntriesLoader.cancel(false);
        }
        lastEntriesLoader = new LoadMileageEntriesTask();
        lastEntriesLoader.execute(carName);
    }

    private void updateCarMileageEntries(MileageDatabase.MileageEntry[] entries) {
        entriesAdapter.setMileageEntries(entries);
        hideLoading();
    }

    private void showLoading() {
        loading.setVisibility(loading.VISIBLE);
        list.setVisibility(list.GONE);
    }

    private void hideLoading() {
        loading.setVisibility(loading.GONE);
        list.setVisibility(list.VISIBLE);
    }

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
*/
/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
*/
}
