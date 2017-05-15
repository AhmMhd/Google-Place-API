package com.example.abdulhakeem.googleplaceapi;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks {

    GoogleApiClient mGoogleApiClient;
    int PLACE_PICKER_REQUEST = 99;
    Button chooseLocation,guessLocation;
    Place jsonResponceString;
    GoogleMap googleMap;
    CameraPosition cameraPosition;
    MapView mapView;
    MarkerOptions markerOptions;
    Marker marker;
    TextView tvGuessLocation;
    AutoCompleteTextView autoCompleteTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGoogleApiClient = new GoogleApiClient
                .Builder( this )
                .enableAutoManage( this, 0, this )
                .addApi( Places.GEO_DATA_API )
                .addApi( Places.PLACE_DETECTION_API )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .build();

      autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        tvGuessLocation = (TextView) findViewById(R.id.tvGuessLocation);
        chooseLocation = (Button) findViewById(R.id.chooseLocation);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;


                markerOptions = new MarkerOptions()
                        .position(new LatLng(0,0));


                marker = googleMap.addMarker(markerOptions);


                // For zooming automatically to the location of the marker
                cameraPosition = new CameraPosition.Builder().target(marker.getPosition()).zoom(0).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }


        });

        chooseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if( mGoogleApiClient == null || !mGoogleApiClient.isConnected() )
                  return;

                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult( builder.build( getApplicationContext() ), PLACE_PICKER_REQUEST );
                } catch ( GooglePlayServicesRepairableException e ) {
                    Log.d( "PlacesAPI Demo", "GooglePlayServicesRepairableException thrown" );
                } catch ( GooglePlayServicesNotAvailableException e ) {
                    Log.d( "PlacesAPI Demo", "GooglePlayServicesNotAvailableException thrown" );
                }


            }
        });

        guessLocation = (Button) findViewById(R.id.guessLocation);
        guessLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace( mGoogleApiClient, null );

                result.setResultCallback( new ResultCallback<PlaceLikelihoodBuffer>() {
                    @Override
                    public void onResult( PlaceLikelihoodBuffer likelyPlaces ) {


                        PlaceLikelihood placeLikelihood = likelyPlaces.get( 0 );

                        String content = "";
                        if( placeLikelihood != null && placeLikelihood.getPlace() != null && !TextUtils.isEmpty( placeLikelihood.getPlace().getName() ) )
                            content = "Most likely place: " + placeLikelihood.getPlace().getName() + "\n";
                        if( placeLikelihood != null )
                            content += "Percent change of being there: " + (int) ( placeLikelihood.getLikelihood() * 100 ) + "%";
                        tvGuessLocation.append(content +"\n");

                        likelyPlaces.release();
                    }
                });
            }
        });




    }

    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        if( requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK ) {
            //Toast.makeText(getBaseContext(), ""+PlacePicker.getPlace( data, this ),Toast.LENGTH_LONG).show();

            jsonResponceString = PlacePicker.getPlace( data, this );


            marker.remove();
            markerOptions = new MarkerOptions()
                    .position(jsonResponceString.getLatLng()).title(jsonResponceString.getAddress()+"").snippet(jsonResponceString.getPhoneNumber()+"");

            marker = googleMap.addMarker(markerOptions);
            // For zooming automatically to the location of the marker
            cameraPosition = new CameraPosition.Builder().target(jsonResponceString.getLatLng()).zoom(10).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            //displayPlace( PlacePicker.getPlace( data, this ) );
        }
    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    @Override
    public void onConnected( Bundle bundle ) {
    }






}
