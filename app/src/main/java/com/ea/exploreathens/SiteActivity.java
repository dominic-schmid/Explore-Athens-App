package com.ea.exploreathens;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.ea.exploreathens.code.CodeUtility;
import com.ea.exploreathens.code.Site;
import com.google.android.gms.maps.model.LatLng;

public class SiteActivity extends AppCompatActivity {

    Site site;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site);

        //Toolbar toolbar = findViewById(R.id.app_toolbar);
        //TextView title = findViewById(R.id.toolbarTitle);
        //title.setText(sitename);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().hide();

        Bundle b = getIntent().getExtras();

        // Liest die Site aus der übergebenen Index aus, um die Activity mit infos zu füllen
        if(b != null){
            site = CodeUtility.sites.get(b.getInt("siteindex"));
            Log.d("info", "Got site through bundle: " +  site.toString());

            TextView name = findViewById(R.id.siteNameTv);
            TextView address = findViewById(R.id.siteAddressTv);
            TextView description = findViewById(R.id.siteDescriptionTv);

            // Set local image paths for this site
            ViewPager viewPager = findViewById(R.id.viewPager);
            ImageAdapter imageAdapter = new ImageAdapter(this, site.getImageRequestPaths());
            viewPager.setAdapter(imageAdapter);

            name.setText(site.getName());
            address.setText(site.getAddress());
            description.setText(site.getDescription());
        }

        FloatingActionButton fab = findViewById(R.id.siteRouteBtn);
        fab.setOnClickListener(view -> {
            /*Snackbar.make(view, "Route berechnen...", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("routeTo", sitename); // Returns routeTo - sitename to the parent activity
            setResult(Activity.RESULT_OK, resultIntent);
            finish();*/
            // TODO move sendlocation somewhere else and put route here
            //showSendLocation(this);
            Snackbar.make(view, site.getImageLocalPaths().toString(), Snackbar.LENGTH_LONG).show();
        });

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
