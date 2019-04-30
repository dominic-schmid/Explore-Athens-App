package com.ea.exploreathens.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ea.exploreathens.MainActivity;
import com.ea.exploreathens.MapsActivity;
import com.ea.exploreathens.R;
import com.ea.exploreathens.RequestHelper;
import com.ea.exploreathens.SiteActivity;
import com.ea.exploreathens.code.CodeUtility;
import com.ea.exploreathens.code.Coordinate;
import com.ea.exploreathens.code.Route;
import com.ea.exploreathens.code.Site;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    private OnFragmentInteractionListener mListener;

    private MapView mapView;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private String provider;
    private double currentLat=0, currentLng=0;
    private boolean drawPolyline;
    private List<Polyline> polylines = new ArrayList<Polyline>();

    private static final double ZOOM_ATHENS = 11;
    private static final LatLng ATHENS_LAT_LNG = new LatLng(37.9841098,23.7213537);
    public static HashMap<String, Marker> markerList = new HashMap<>();


    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        String myValue = this.getArguments().getString("routeTo");

        for(Site s : CodeUtility.sites){
            if(s.getName().equals(myValue)){
                RouteRequest req = new RouteRequest();
                req.execute(CodeUtility.baseURL + "/route/" + currentLat + "," + currentLng + "/" + s.getX() + "," + s.getY());
                break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View wView = inflater.inflate(R.layout.fragment_maps, container, false);
        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        //        .findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);



        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        if(missingMapsPermissions())
            getPermission();
        else{
            @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(provider);

            if(location != null){
                onLocationChanged(location);
            }
        }

        return wView;
    }

    /*
        In "onMapReady" the map_menu gets actually loaded and with CcameraPosition the initial map_menu starts in Athens (See variables ZOOM_ATHENS and ATHENS_LAT_LNG)
        At the end the it looks if the required permission are given, otherwise it will ask for them (see getPermission)
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //drawRadar(100.0);
        LatLng position = new LatLng(currentLat, currentLng);

        CameraPosition athens = new CameraPosition.Builder().target(ATHENS_LAT_LNG).zoom((float)ZOOM_ATHENS).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(athens));

        if(missingMapsPermissions()){
            getPermission();
        }else {
            // TODO mMap.setMyLocationEnabled(true);
        }
    }

    private boolean missingMapsPermissions() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    public void redrawRoute(Route route){
        ArrayList <Coordinate> coordinatesList = route.getCoordinates();
        LatLng latLngPrevious = new LatLng(currentLat, currentLng);

        if (mMap != null) {
            for (Coordinate c : coordinatesList) {

                //get current coordinates with LatLngNext
                LatLng latLngNext = new LatLng(c.getX(), c.getY());

                polylines.add(mMap.addPolyline((new PolylineOptions())
                        .add(latLngPrevious, latLngNext)
                        .width(5)
                        .color(Color.BLUE)
                        .geodesic(true)));

                latLngPrevious = latLngNext;
            }
        }
    }

    public void drawSiteMarkers() {
        /* TODO Am Anfang sollen alle Sites angezeigt werden, die wir kennen
        TODO Durch einen Button / Menüeintrag sollen dann nur noch die Sites im gewählten Radius angezeigt werden, dann könnte man
        TODO auch die Entfernung dazuschreiben wenn das nicht zu viel Arbeit wird
         */


        for (Site s : CodeUtility.sites) {
            Log.d("info", "Iterating item " + s);
            if (mMap != null) {
                LatLng position = new LatLng(s.getX(), s.getY());
                MarkerOptions opt = new MarkerOptions()
                        .title(s.getName())
                        .position(position)
                        .snippet(s.getDescription());
                Log.d("info", "Put Marker on map_menu " + opt.toString());
                markerList.put(s.getName(), mMap.addMarker(opt));
            }
        }

        mMap.setOnInfoWindowClickListener(new InfoClickListener());

        // This inflates the info window when you click on a Marker
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker arg0) {
                Site site = getSiteFromMarker(arg0);

                View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);
                TextView tvTitle = v.findViewById(R.id.tvName); // TODO change these IDS
                TextView tvDistance =  v.findViewById(R.id.tvDistance);

                // TODO change this to distance or just write address?
                tvDistance.setText(site.getAddress());
                tvTitle.setText(site.getName());

                return v;
            }
        });
    }

    public void drawRadar(double meter){
        if(mMap != null){
            CircleOptions radarCircle;
            mMap.addCircle(radarCircle = new CircleOptions()
                    .center(ATHENS_LAT_LNG)
                    .radius(meter)
                    .strokeWidth(0f)
                    .fillColor(0x550000FF));

            //TODO: change ATHENS_LAT_LNG with position

            for (Site s : CodeUtility.sites) {
                if(CodeUtility.haversine(s.getX(), s.getY(), ATHENS_LAT_LNG.latitude, ATHENS_LAT_LNG.longitude) > meter){
                    markerList.get(s.getName()).remove();
                }
            }
        }

    }


    /**
     * Gets the Site object from a Marker on the Google Map
     * @param arg0 The Marker
     * @return the Site object for the coordinates of the Marker
     */

    private Site getSiteFromMarker(Marker arg0) {
        LatLng latLng = arg0.getPosition();
        Site site = null;

        for(Site sit : CodeUtility.sites){
            //Toast.makeText(getApplicationContext(), lat+"----"+lon+" = " + sit.getX() + "----" + sit.getY(),Toast.LENGTH_SHORT).show();
            // Site von diesem Marker gefunden
            if(sit.getX() == latLng.latitude && sit.getY() == latLng.longitude)
                return sit;
        }

        return null;
    }

    class InfoClickListener implements GoogleMap.OnInfoWindowClickListener {

        @Override
        public void onInfoWindowClick(Marker marker) {
            Site site = getSiteFromMarker(marker);
            Intent intent = new Intent(getActivity(), SiteActivity.class);
            Bundle b = new Bundle();
            b.putString("sitename", site.getName());
            intent.putExtras(b); //Put your id to your next Intent
            startActivity(intent);
        }
    }

    // A pop-up asks if the app can use the service "ACCESS_COARSE_LOCATION" and "ACCESS_FINE_LOCATION"
    public void getPermission(){
        ActivityCompat.requestPermissions(getActivity(), new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION},
                0);
    }
    //This method initializes the upper right menu corner
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }*/

    //onOptionsItemSelected sets up the action to complete if a menu option is clicked
   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.action_normal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.action_hybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.action_satellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.action_settings:
                Intent intent = new Intent(MapsActivity.this, Settings.class);
                startActivity(intent);
                break;
        }
        return true;
    }*/






    // If the phone location is changed it will change the latitude and longitude of the variable location
    @Override
    public void onLocationChanged(Location location) {
        currentLat = location.getLatitude();
        currentLng = location.getLongitude();


        //TODO: Make request , get destination Coordinates
        if (drawPolyline){
            LatLng destination = new LatLng(0,0);

            for(Polyline line : polylines)
                line.remove();

            polylines.clear();

            //If you arrive to the destination
            if(destination.longitude + 0.01 >= location.getLongitude() && destination.longitude - 0.01 <= location.getLongitude() &&
                    destination.latitude + 0.01 >= location.getLatitude() && destination.latitude - 0.01 <= location.getLatitude()){
                //msg: you arrived to your destination
            }else{
                redrawRoute();
                // TODO ???? drawRoute();
            }
        }
    }








    //Just "must have" methods
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public void drawRoute(Route route){
        ArrayList <Coordinate> coordinatesList = route.getCoordinates();
        LatLng latLngPrevious = new LatLng(currentLat, currentLng);

        if (mMap != null) {
            for (Coordinate c : coordinatesList) {

                //get current coordinates with LatLngNext
                LatLng latLngNext = new LatLng(c.getX(), c.getY());

                polylines.add(mMap.addPolyline((new PolylineOptions())
                        .add(latLngPrevious, latLngNext)
                        .width(5)
                        .color(Color.BLUE)
                        .geodesic(true)));

                latLngPrevious = latLngNext;
            }
        }
    }

    public void redrawRoute(){
        LatLng destination = new LatLng(0,0); // TODO you need site here

        for(Polyline line : polylines)
            line.remove();

        polylines.clear();

        //If you arrive to the destination
        if(destination.longitude + 0.01 >= currentLng && destination.longitude - 0.01 <= currentLng &&
                destination.latitude + 0.01 >= currentLat && destination.latitude - 0.01 <= currentLat){
            //msg: you arrived to your destination
        }else{
            drawRoute(null); // TODO param site
            // TODO ???? drawRoute();
        }

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void showError(String message){
        CodeUtility.showError(getContext(), message);
        //pullToRefresh.setRefreshing(false);
    }

    class RouteRequest extends AsyncTask<String, Void, String> {

        private String contentType;
        public Route route;

        @Override
        protected String doInBackground(String... urls) {

            String responseType = "";

            try {
                //if(!CodeUtility.internetAvailable(getContext()))
                //    return "Internet not available";

                RequestHelper helper = new RequestHelper();
                helper.getRequestContent(urls[0]);
                Log.d("connection-helper", "Helper " + helper.toString() + " returned ");
                contentType = helper.responseType;

                Log.d("json-response", helper.responseContent);
                Log.d("json-responsetype", helper.responseType);
                Log.d("json-responsecode", ""+helper.responseCode);

                return helper.responseContent;
            } catch (Exception e) {
                e.printStackTrace();
                //String err = (e.getMessage() == null) ? "SD Card failed" : e.getMessage();
                //og.e("connection-error", ""+err);
                return "Oh no, an error occured :(";
            }

        }

        @Override
        protected void onPostExecute(String response){
            Object obj = getJSON(response);

            if(obj instanceof String) {
                // Error
                showError(""+obj);
                Log.e("error", obj.toString());
                // If response is String an error occured and you can read it by calling string
            } else if (obj instanceof Route){
                // Otherwise a request to /sites must return AL<Site> so you can just cast

                Route route = (Route) obj;
                this.route = route;
                drawRoute(route);

                Log.d("json-sites", route.toString());
            } else
                Log.e("json-error", "JSON Parse error. Type not found");
        }

        public Object getJSON(String responseContent){
            JSONParser parser = new JSONParser();

            try {
                // Depending on response-type return correct object
                if (contentType.equalsIgnoreCase("sites")) {
                    JSONObject jo = (JSONObject) parser.parse(responseContent);

                    Route r = Route.parse(jo);
                    return r;
                }
            } catch (Exception ex) {
                String err = (ex.getMessage() == null) ? "SD Card failed" : ex.getMessage();
                ex.printStackTrace();
                Log.e("json-parse-error:", "Could not parse JSON response. Error: "+err);
            }

            return responseContent;
        }

    }
}