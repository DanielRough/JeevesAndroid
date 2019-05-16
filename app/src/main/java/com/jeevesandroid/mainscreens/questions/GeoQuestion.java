package com.jeevesandroid.mainscreens.questions;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jeevesandroid.R;
import com.jeevesandroid.firebase.FirebaseQuestion;
import com.jeevesandroid.mainscreens.SurveyActivity;

import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class GeoQuestion extends Question implements GoogleApiClient.OnConnectionFailedListener,
    OnMapReadyCallback{
    private GoogleMap map;
    private final int PLACE_PICKER_REQUEST = 1;

    public GeoQuestion(SurveyActivity activity, List<FirebaseQuestion> questions, List<String> answers) {
        super(activity,questions,answers);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, context);
                String toastMsg = place.getName().toString();

                Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show();
                TextView txtPlaceName = qView.findViewById(R.id.txtPlaceName);
                txtPlaceName.setText(toastMsg);
                LatLng coords = place.getLatLng();
                map.moveCamera(CameraUpdateFactory.newLatLng(coords));
                map.addMarker(new MarkerOptions()
                    .position(coords)
                    .title(place.getName().toString()));
                String answer = coords.latitude + ":" + coords.longitude + ";";
                answers.set(currentIndex, answer);
            }
        }
        if(requestCode == 1234){ //code i've defined for heart but cba making a constant
            Button btnStart = qView.findViewById(R.id.btnStart);
            btnStart.setText(context.getResources().getString(R.string.heartrate));
            btnStart.setEnabled(false);
            PhotoView heartview = qView.findViewById(R.id.heartview);
            heartview.setImageResource(R.drawable.fingerdone);
            int result = data.getIntExtra("result",0);
            answers.set(currentIndex,Integer.toString(result));

        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        LatLng sydney = new LatLng(-33.867, 151.206);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));
        map.getUiSettings().setZoomControlsEnabled(true);
        Criteria criteria = new Criteria();
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        double lat, lng;
        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
        } else {
            lat = 56.341703;
            lng = -2.792;
        }
        LatLng coordinate = new LatLng(lat, lng);
        map.moveCamera(CameraUpdateFactory.zoomTo(10));

        map.moveCamera(CameraUpdateFactory.newLatLng(coordinate)); //This oughta put our camera at the current location
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng GoogleMap) {
                MarkerOptions options = new MarkerOptions();
                MarkerOptions opts = options.position(GoogleMap);
                map.clear();
                map.addMarker(opts);
                answers.set(currentIndex, opts.getPosition().latitude + ":" + opts.getPosition().longitude);
                map.moveCamera(CameraUpdateFactory.newLatLng(GoogleMap)); //This oughta put our camera at the current location
                map.animateCamera(CameraUpdateFactory.zoomTo(16));
            }
        });
    }

    @Override
    public void handle(int position) {
        SupportMapFragment mapFragment = (SupportMapFragment) context.getSupportFragmentManager()
            .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final TextView txtPlaceName = qView.findViewById(R.id.txtPlaceName);

        String answer = answers.get(currentIndex);
        if (!answer.isEmpty()) {
            String[] locationbits = answer.split(":");
            double latitude = Double.parseDouble(locationbits[0]);
            double longitude = Double.parseDouble(locationbits[1]);
            LatLng coords = new LatLng(latitude, longitude);
            String placename = locationbits[2];
            txtPlaceName.setText(placename);
            map.moveCamera(CameraUpdateFactory.newLatLng(coords));
            map.addMarker(new MarkerOptions()
                .position(coords)
                .title(placename));
        } else {
            answers.set(currentIndex, "");

        }

        Button btnPlacePicker = qView.findViewById(R.id.btnPlacePicker);

        btnPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    context.startActivityForResult(builder.build(context), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.qu_geo;
    }
}
