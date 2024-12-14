package com.example.gps_pet_tracker;

import static com.mapbox.maps.plugin.animation.CameraAnimationsUtils.getCamera;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.addOnMapClickListener;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.CameraState;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.observable.eventdata.CameraChangedEventData;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener;
import com.mapbox.maps.plugin.gestures.OnMapClickListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.generated.LocationComponentSettings;
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider;
import com.mapbox.search.autocomplete.PlaceAutocomplete;
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion;
import com.mapbox.search.ui.adapter.autocomplete.PlaceAutocompleteUiAdapter;
import com.mapbox.search.ui.view.CommonSearchViewConfiguration;
import com.mapbox.search.ui.view.SearchResultsView;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.jvm.functions.Function1;

public class User_map_set_location extends AppCompatActivity implements ConnectionReceiver.ReceiverListener{

    MapView mapView;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private Point userPoint;
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 100;  // in milliseconds

    Button setRoute;
    private PlaceAutocomplete placeAutocomplete;
    private SearchResultsView searchResultsView;
    private PlaceAutocompleteUiAdapter placeAutocompleteUiAdapter;
    private TextInputEditText searchET;
    String set = "";
    double zoomLevell = 0.0;
    private boolean ignoreNextQueryUpdate = false;
    FrameLayout map_type;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    String userId;
    PointAnnotationManager pointAnnotationManager;
    boolean focuslLocationisClick = false;
    LocationComponentPlugin locationComponentPlugin;

    NavigationLocationProvider navigationLocationProviderr = new NavigationLocationProvider();
    private boolean checkMarker = true;
    String register_class,previousFragment_fromregister;
    private boolean no_safe_location = false;
    boolean haveInternet = false;
    String longitude , latitude;
    TableLayout tableLayout;
    Slider meter_value;
    TextView meter_val;
    AlertDialog alert_load,Internet;
    float radius;
    AlertDialog alertt;
    private FloatingActionButton focusLocation, map_type_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_map_set_location);

        mapView = findViewById(R.id.mapView);
        focusLocation = findViewById(R.id.focusLocation);
        setRoute = findViewById(R.id.setRoute);
        map_type = findViewById(R.id.map_type);
        map_type_button = findViewById(R.id.btn_map_type);
        tableLayout = findViewById(R.id.tabLayout);
        meter_value = findViewById(R.id.meter_value);
        meter_val = findViewById(R.id.meter_val);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userId = firebaseAuth.getUid();

        locationComponentPlugin = getLocationComponent(mapView);

        AlertDialog.Builder alert = new AlertDialog.Builder(User_map_set_location.this);
        alert.setCancelable(false);
        View view = getLayoutInflater().inflate(R.layout.custom_loading_process,null);
        alert.setView(view);

        alert_load = alert.create();

        AlertDialog.Builder internet = new AlertDialog.Builder(this);
        View view1 = getLayoutInflater().inflate(R.layout.custom_no_internet,null);
        internet.setView(view1);

        Button btn_ok = view1.findViewById(R.id.btn_ok);

        Internet = internet.create();

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Internet.dismiss();
            }
        });

        placeAutocomplete = PlaceAutocomplete.create(getString(R.string.mapbox_access_token));
        searchET = findViewById(R.id.searchET);

        searchResultsView = findViewById(R.id.search_results_view);
        searchResultsView.initialize(new SearchResultsView.Configuration(new CommonSearchViewConfiguration()));

        placeAutocompleteUiAdapter = new PlaceAutocompleteUiAdapter(searchResultsView, placeAutocomplete, LocationEngineProvider.getBestLocationEngine(User_map_set_location.this));


        Intent i = getIntent();
        register_class = i.getStringExtra("register_class");
        previousFragment_fromregister = i.getStringExtra("previousFragment");

        // Set the custom thumb tint programmatically
        meter_value.setThumbTintList(ColorStateList.valueOf(getResources().getColor(R.color.buttn)));
        // Set track tint colors programmatically
        meter_value.setTrackTintList(ColorStateList.valueOf(getResources().getColor(R.color.buttn)));
        meter_value.setTrackActiveTintList(ColorStateList.valueOf(getResources().getColor(R.color.buttn)));
        meter_value.setTrackInactiveTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));

        searchET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    map_type_button.hide();
                    focusLocation.hide();
                }else {
                    map_type_button.show();
                    focusLocation.show();
                    searchResultsView.setVisibility(View.GONE);
                }
            }
        });

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (ignoreNextQueryUpdate) {
                    ignoreNextQueryUpdate = false;
                } else {
                    placeAutocompleteUiAdapter.search(charSequence.toString(), new Continuation<Unit>() {
                        @NonNull
                        @Override
                        public CoroutineContext getContext() {
                            return EmptyCoroutineContext.INSTANCE;
                        }

                        @Override
                        public void resumeWith(@NonNull Object o) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    searchResultsView.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        if (pointAnnotationManager == null) {
            setRoute.setVisibility(View.GONE);
            tableLayout.setVisibility(View.GONE);
        }

        //this code when click then the searchEt is focus, will be remove focus on searchEt
        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    View focustView = getCurrentFocus();
                    if (focustView instanceof TextInputEditText){
                        focustView.clearFocus();
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(focustView.getWindowToken(),0);
                    }
                }
                return false;
            }
        });

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                // Center the map on the Philippines with a specific zoom level
                Point centerPhilippines = Point.fromLngLat(121.7740, 12.8797);
                CameraOptions cameraOptions = new CameraOptions.Builder()
                        .center(centerPhilippines) // Longitude, Latitude for the center of the Philippines
                        .zoom(5.0) // Adjust the zoom level as needed
                        .build();

                mapView.getMapboxMap().setCamera(cameraOptions);

                DatabaseReference databaseReference = firebaseDatabase.getReference("User SafeLocation")
                        .child(userId);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (snapshot.child("longitude").exists() && snapshot.child("latitude").exists()) {
                                latitude = snapshot.child("latitude").getValue(String.class);
                                longitude = snapshot.child("longitude").getValue(String.class);
                                radius = Float.parseFloat(snapshot.child("radius").getValue(String.class));
                                meter_value.setValue(radius);
                                meter_val.setText(snapshot.child("radius").getValue(String.class));

                                locationComponentPlugin.updateSettings(new Function1<LocationComponentSettings, Unit>() {
                                    @Override
                                    public Unit invoke(LocationComponentSettings locationComponentSettings) {
                                        locationComponentSettings.setPulsingEnabled(true);
                                        locationComponentSettings.setPulsingMaxRadius((float) (radius * 3.38));
                                        return null;
                                    }
                                });
                            }

                            if (latitude != null && longitude != null && !latitude.isEmpty() && !longitude.isEmpty()) {
                                Point locationsafe = Point.fromLngLat(Double.parseDouble(longitude), Double.parseDouble(latitude));

                                CameraOptions cameraOptions = new CameraOptions.Builder()
                                        .center(locationsafe)
                                        .zoom(18.0)
                                        .build();

                                getCamera(mapView).easeTo(cameraOptions, new MapAnimationOptions.Builder().duration(1000).build());

                                double longitude = locationsafe.longitude();
                                double latitude = locationsafe.latitude();

                                // Create a new Location object from the Point (Mapbox's Point to Android's Location)
                                Location newLocation = new Location("Mapbox");
                                newLocation.setLatitude(latitude);
                                newLocation.setLongitude(longitude);

                                // Create a list of Location objects (even if it's just one)
                                List<Location> locationList = Collections.singletonList(newLocation);

                                // You need to provide animation functions (they can be empty if you don't want animations)
                                Function1<ValueAnimator, Unit> startAnimation = new Function1<ValueAnimator, Unit>() {
                                    @Override
                                    public Unit invoke(ValueAnimator animator) {
                                        // No-op or custom animation start logic here
                                        return null;
                                    }
                                };

                                Function1<ValueAnimator, Unit> endAnimation = new Function1<ValueAnimator, Unit>() {
                                    @Override
                                    public Unit invoke(ValueAnimator animator) {
                                        // No-op or custom animation end logic here
                                        return null;
                                    }
                                };

                                // Now, call changePosition with all required parameters
                                navigationLocationProviderr.changePosition(newLocation, locationList, startAnimation, endAnimation);

                                locationPlugin();

                            }else {
                                // Center the map on the Philippines with a specific zoom level
                                Point centerPhilippines = Point.fromLngLat(121.7740, 12.8797);
                                CameraOptions cameraOptions = new CameraOptions.Builder()
                                        .center(centerPhilippines) // Longitude, Latitude for the center of the Philippines
                                        .zoom(5.0) // Adjust the zoom level as needed
                                        .build();

                                mapView.getMapboxMap().setCamera(cameraOptions);
                            }
                        }else {
                            no_safe_location = true;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                AnnotationPlugin annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);

                pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, mapView);
                addOnMapClickListener(mapView.getMapboxMap(), new OnMapClickListener() {
                    @Override
                    public boolean onMapClick(@NonNull Point point) {

                        pointAnnotationManager.deleteAll();
                        locationComponentPlugin.setEnabled(false);
                        if (userPoint != null) {
                            locationComponentPlugin.setLocationPuck(new LocationPuck2D(null,null));
                            checkMarker = false;
                            LocationUserRealTime();
                        }

                        double longitude = point.longitude();
                        double latitude = point.latitude();

                        // Create a new Location object from the Point (Mapbox's Point to Android's Location)
                        Location newLocation = new Location("Mapbox");
                        newLocation.setLatitude(latitude);
                        newLocation.setLongitude(longitude);

                        // Create a list of Location objects (even if it's just one)
                        List<Location> locationList = Collections.singletonList(newLocation);

                        // You need to provide animation functions (they can be empty if you don't want animations)
                        Function1<ValueAnimator, Unit> startAnimation = new Function1<ValueAnimator, Unit>() {
                            @Override
                            public Unit invoke(ValueAnimator animator) {
                                // No-op or custom animation start logic here
                                return null;
                            }
                        };

                        Function1<ValueAnimator, Unit> endAnimation = new Function1<ValueAnimator, Unit>() {
                            @Override
                            public Unit invoke(ValueAnimator animator) {
                                // No-op or custom animation end logic here
                                return null;
                            }
                        };

                        // Now, call changePosition with all required parameters
                        navigationLocationProviderr.changePosition(newLocation, locationList, startAnimation, endAnimation);

                        locationPlugin();

                        setRoute.setVisibility(View.VISIBLE);
                        tableLayout.setVisibility(View.VISIBLE);

                        setRoute.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                checkInternet();
                                if (haveInternet) {
                                    fetchRoute(point);
                                }
                            }
                        });
                        return true;
                    }
                });

                placeAutocompleteUiAdapter.addSearchListener(new PlaceAutocompleteUiAdapter.SearchListener() {
                    @Override
                    public void onSuggestionsShown(@NonNull List<PlaceAutocompleteSuggestion> list) {

                    }

                    @Override
                    public void onSuggestionSelected(@NonNull PlaceAutocompleteSuggestion placeAutocompleteSuggestion) {
                        ignoreNextQueryUpdate = true;
                        searchET.setText(placeAutocompleteSuggestion.getName());
                        searchResultsView.setVisibility(View.GONE);
                        pointAnnotationManager.deleteAll();
                        addOnMapClickListener(mapView.getMapboxMap(), new OnMapClickListener() {
                            @Override
                            public boolean onMapClick(@NonNull Point point) {
                                if (userPoint != null) {
                                    locationComponentPlugin.setEnabled(false);
                                    locationComponentPlugin.setLocationPuck(new LocationPuck2D(null,null));
                                    checkMarker = false;
                                    LocationUserRealTime();
                                }

                                setRoute.setVisibility(View.VISIBLE);
                                tableLayout.setVisibility(View.VISIBLE);

                                setRoute.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        checkInternet();
                                        if (haveInternet) {
                                            fetchRoute(point);
                                        }
                                    }
                                });
                                return true;
                            }
                        });

                        searchResult(placeAutocompleteSuggestion.getCoordinate());

                        updateCamera(placeAutocompleteSuggestion.getCoordinate());

                        setRoute.setVisibility(View.VISIBLE);
                        tableLayout.setVisibility(View.VISIBLE);

                        setRoute.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                checkInternet();
                                if (haveInternet) {
                                    fetchRoute(placeAutocompleteSuggestion.getCoordinate());
                                }
                            }
                        });
                    }

                    @Override
                    public void onPopulateQueryClick(@NonNull PlaceAutocompleteSuggestion placeAutocompleteSuggestion) {
                        //queryEditText.setText(placeAutocompleteSuggestion.getName());
                    }

                    @Override
                    public void onError(@NonNull Exception e) {

                    }
                });

            }
        });

        map_type_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater1 = getLayoutInflater();
                View map_type_view = layoutInflater1.inflate(R.layout.custom_map_type_layout, null);

                TextView tvDefult = map_type_view.findViewById(R.id.tvDefault);
                TextView tvSatellite = map_type_view.findViewById(R.id.tvSatellite);
                TextView tvTerrain = map_type_view.findViewById(R.id.tvTerrain);

                ImageButton default_view = map_type_view.findViewById(R.id.default_view);
                ImageButton satellite_view = map_type_view.findViewById(R.id.satelite_view);
                ImageButton terrain_view = map_type_view.findViewById(R.id.terrain_view);

                map_type.removeAllViews(); // clear existing views
                map_type.addView(map_type_view);// add new view
                map_type.setVisibility(View.VISIBLE); // show the frame layout
                setRoute.setVisibility(View.GONE);
                tableLayout.setVisibility(View.GONE);


                if (set != null) {
                    if (set == "1") {
                        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS);
                        tvDefult.setTextColor(Color.rgb(38, 166, 254));
                        tvSatellite.setTextColor(Color.BLACK);
                        tvTerrain.setTextColor(Color.BLACK);
                    }else if (set == "2"){
                        mapView.getMapboxMap().loadStyleUri(Style.SATELLITE);
                        tvDefult.setTextColor(Color.BLACK);
                        tvSatellite.setTextColor(Color.rgb(38, 166, 254));
                        tvTerrain.setTextColor(Color.BLACK);
                    } else if (set == "3") {
                        mapView.getMapboxMap().loadStyleUri(Style.OUTDOORS);
                        tvDefult.setTextColor(Color.BLACK);
                        tvSatellite.setTextColor(Color.BLACK);
                        tvTerrain.setTextColor(Color.rgb(38, 166, 254));
                    }
                }

                Button close_map_view = map_type_view.findViewById(R.id.close_map_type);

                close_map_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (pointAnnotationManager != null) {
                            setRoute.setVisibility(View.VISIBLE);
                            tableLayout.setVisibility(View.VISIBLE);
                        }else{
                            setRoute.setVisibility(View.GONE);
                            tableLayout.setVisibility(View.GONE);
                        }

                        map_type.setVisibility(View.GONE);
                    }
                });

                default_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS);
                        tvDefult.setTextColor(Color.rgb(38, 166, 254));
                        tvSatellite.setTextColor(Color.BLACK);
                        tvTerrain.setTextColor(Color.BLACK);
                        set = "1";
                    }
                });

                satellite_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mapView.getMapboxMap().loadStyleUri(Style.SATELLITE);
                        tvDefult.setTextColor(Color.BLACK);
                        tvSatellite.setTextColor(Color.rgb(38, 166, 254));
                        tvTerrain.setTextColor(Color.BLACK);
                        set = "2";
                    }
                });

                terrain_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mapView.getMapboxMap().loadStyleUri(Style.OUTDOORS);
                        tvDefult.setTextColor(Color.BLACK);
                        tvSatellite.setTextColor(Color.BLACK);
                        tvTerrain.setTextColor(Color.rgb(38, 166, 254));
                        set = "3";
                    }
                });
            }
        });

        focusLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pointAnnotationManager != null) {
                    pointAnnotationManager.deleteAll();
                }
                checkPermissionsAndProceed ();
                focuslLocationisClick = true;
                checkMarker = true;
            }
        });
    }

    private double calculateCircleRadius(double zoomLevel, double baseRadius) {
        double minZoom = 15;  // Adjust this to your desired minimum zoom level
        double maxZoom = 18;  // Adjust this to your desired maximum zoom level

        if (zoomLevel == maxZoom) {
            // If the zoom level is at or above maxZoom, the radius stays at the baseRadius (90.0)
            return baseRadius;
        } else if (zoomLevel >= maxZoom) {
            // Reduce the radius as zoom level increases, but ensure the radius does not grow
            double factor = Math.pow(2, maxZoom - zoomLevel);  // Exponentially decrease as zoom increases
            return baseRadius / factor;  // Shrink the radius based on zoom level
        }
        else if (zoomLevel < maxZoom && zoomLevel > minZoom) {
            // Reduce the radius as zoom level increases, but ensure the radius does not grow
            double factor = Math.pow(2, maxZoom - zoomLevel);  // Exponentially decrease as zoom increases
            return baseRadius / factor;  // Shrink the radius based on zoom level
        } else {
            // If the zoom level is below minZoom, the circle shrinks at a faster rate
            return 1.0;
        }
    }

    private void checkPermissionsAndProceed() {
        String[] permissions = null;
        permissions = new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (hasPermissions(permissions)) {
            // Permissions granted, check if location services are enabled
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                // Location services enabled, proceed with your logic
                alert_load.show();
                LocationUserRealTime();

            } else {
                // Location services disabled, show alert dialog
                showLocationServicesAlertDialog();
            }
        } else {
            // Permissions not granted, request permissions
            requestLocationPermissions(permissions);
        }
    }

    private void requestLocationPermissions(String[] permissions) {
        if (shouldShowRequestPermissionRationale(User_map_set_location.this, permissions)) {
            // Show a dialog explaining why the permission is needed, if required
            showPermissionRationaleDialog(permissions);
        } else {
            // Directly request permissions
            ActivityCompat.requestPermissions(User_map_set_location.this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private void showPermissionRationaleDialog(String[] permissions) {
        AlertDialog.Builder alert = new AlertDialog.Builder(User_map_set_location.this);
        View view = getLayoutInflater().inflate(R.layout.custom_permission,null);
        alert.setView(view);
        AlertDialog show = alert.create();

        show.show();

        Button btn_ok = view.findViewById(R.id.btn_set_now);
        Button btn_cancel = view.findViewById(R.id.btn_cancell);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show.dismiss();
                ActivityCompat.requestPermissions(User_map_set_location.this, permissions, PERMISSION_REQUEST_CODE);
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show.dismiss();
                Toast.makeText(User_map_set_location.this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean shouldShowRequestPermissionRationale(Activity activity, String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(User_map_set_location.this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void showLocationServicesAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(User_map_set_location.this);
        View view = getLayoutInflater().inflate(R.layout.custom_location_service_alert,null);
        builder.setView(view);

        AlertDialog alertt = builder.create();
        alertt.show();

        Button btn_turn_on = view.findViewById(R.id.btn_confirm);
        Button btn_cancel = view.findViewById(R.id.btn_cancel);

        btn_turn_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                alertt.dismiss();
                startActivity(intent);
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(User_map_set_location.this, "Location services are disabled. The app may not work as expected.", Toast.LENGTH_SHORT).show();
                alertt.dismiss();
            }
        });

    }

    private void LocationUserRealTime() {
        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(User_map_set_location.this);

        // Create a LocationEngineRequest for high accuracy
        LocationEngineRequest request = new LocationEngineRequest.Builder(1000) // Update interval in 3 seconds
                .setPriority(LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                //  .setDisplacement(5f)  // Minimum displacement of 10 meters before triggering an update
                .build();

        if (ActivityCompat.checkSelfPermission(User_map_set_location.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(User_map_set_location.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationEngine.requestLocationUpdates(request, new LocationEngineCallback<LocationEngineResult>() {
                @Override
                public void onSuccess(LocationEngineResult result) {
                    Context context = User_map_set_location.this;
                    if (context != null) {
                        Location location = result.getLastLocation();

                        if (location != null) {
                            // Convert location to a GeoJSON Point if needed
                            userPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());

                            if (userPoint !=  null) {
                                if (checkMarker) {
                                    setRoute.setVisibility(View.VISIBLE);
                                    tableLayout.setVisibility(View.VISIBLE);

                                    if (focuslLocationisClick) {
                                        focuslLocationisClick = false;
                                        proceedAfterPermission();
                                    }

                                    setRoute.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            checkInternet ();
                                            if (haveInternet) {
                                                fetchRoute(userPoint);
                                            }
                                        }
                                    });
                                }
                            }

                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(User_map_set_location.this,exception.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }, getMainLooper());
        }
    }

    private void updateCamera(Point point) {
        MapAnimationOptions animationOptions = new MapAnimationOptions.Builder().duration(1500L).build();
        CameraOptions cameraOptions = new CameraOptions.Builder().center(point).zoom(18.0).build();

        getCamera(mapView).easeTo(cameraOptions, animationOptions);
    }

    private void searchResult(Point point) {
        double longitude = point.longitude();
        double latitude = point.latitude();

        // Create a new Location object from the Point (Mapbox's Point to Android's Location)
        Location newLocation = new Location("Mapbox");
        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);

        // Create a list of Location objects (even if it's just one)
        List<Location> locationList = Collections.singletonList(newLocation);

        // You need to provide animation functions (they can be empty if you don't want animations)
        Function1<ValueAnimator, Unit> startAnimation = new Function1<ValueAnimator, Unit>() {
            @Override
            public Unit invoke(ValueAnimator animator) {
                // No-op or custom animation start logic here
                return null;
            }
        };

        Function1<ValueAnimator, Unit> endAnimation = new Function1<ValueAnimator, Unit>() {
            @Override
            public Unit invoke(ValueAnimator animator) {
                // No-op or custom animation end logic here
                return null;
            }
        };

        // Now, call changePosition with all required parameters
        navigationLocationProviderr.changePosition(newLocation, locationList, startAnimation, endAnimation);

        locationPlugin();
    }

    private void proceedAfterPermission( ) {
        Handler handlerr = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (userPoint != null) {
                    handlerr.removeCallbacks(this);
                    // Revert the location puck and settings to the default state after canceling navigation
                    alert_load.dismiss();
                    centerMapOnUserLocation();
                }else {
                    handlerr.postDelayed(this,500);
                }
            }
        };
        handlerr.post(runnable);
    }

    private void locationPlugin () {

        // Enable the location component and set up listeners
        locationComponentPlugin.setEnabled(true);
        locationComponentPlugin.setLocationProvider(navigationLocationProviderr);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map_marker);

        if (bitmap != null) {
            Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap,70,70,false);

            // Convert the resized bitmap to Drawable
            Drawable drawable = new BitmapDrawable(getResources(), resizeBitmap);

            locationComponentPlugin.updateSettings(new Function1<LocationComponentSettings, Unit>() {
                @Override
                public Unit invoke(LocationComponentSettings locationComponentSettings) {
                    locationComponentSettings.setEnabled(true);
                    locationComponentSettings.setLocationPuck(new LocationPuck2D(
                            null
                            ,drawable
                    ));
                    if (no_safe_location) {
                        float radiusDef = (float) (10.0 * 3.38);
                        no_safe_location = false;
                        locationComponentPlugin.updateSettings(new Function1<LocationComponentSettings, Unit>() {
                            @Override
                            public Unit invoke(LocationComponentSettings locationComponentSettings) {
                                locationComponentSettings.setPulsingEnabled(true);
                                locationComponentSettings.setPulsingMaxRadius(radiusDef);
                                return null;
                            }
                        });
                        locationComponentSettings.setPulsingEnabled(true);
                        locationComponentSettings.setPulsingMaxRadius(radiusDef);
                    }
                        dispaly();

                    return null;
                }
            });

        }
    }

    private void dispaly () {
        meter_value.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                meter_val.setText(String.valueOf((int) value));
                Log.d("MapFragmenthy", "Updated Circle Radius: " + value);
                mapView.getMapboxMap().addOnCameraChangeListener(new OnCameraChangeListener() {
                    @Override
                    public void onCameraChanged(@NonNull CameraChangedEventData cameraChangedEventData) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastUpdateTime >= UPDATE_INTERVAL) {
                            CameraState cameraState = mapView.getMapboxMap().getCameraState();
                            zoomLevell = cameraState.getZoom();

                            double newRadius = calculateCircleRadius(zoomLevell, value * 3.38);
                            Log.d("MapFragmenthy", "Updated Circle Radius: " + zoomLevell);

                            locationComponentPlugin.updateSettings(new Function1<LocationComponentSettings, Unit>() {
                                @Override
                                public Unit invoke(LocationComponentSettings locationComponentSettings) {
                                    locationComponentSettings.setPulsingEnabled(true);
                                    locationComponentSettings.setPulsingMaxRadius((float) newRadius);
                                    return null;
                                }
                            });

                            lastUpdateTime = currentTime;
                        }
                    }
                });

                locationComponentPlugin.updateSettings(new Function1<LocationComponentSettings, Unit>() {
                    @Override
                    public Unit invoke(LocationComponentSettings locationComponentSettings) {
                        locationComponentSettings.setPulsingEnabled(true);
                        locationComponentSettings.setPulsingMaxRadius((float) (value * 3.38));
                        return null;
                    }
                });
            }
        });
    }

    private void centerMapOnUserLocation() {
        // Center the map on the user's location
        CameraOptions cameraOptions4 = new CameraOptions.Builder()
                .center(userPoint)
                .zoom(18.0)
                .build();
        getCamera(mapView).easeTo(cameraOptions4, new MapAnimationOptions.Builder().duration(1000).build());

        double longitude = userPoint.longitude();
        double latitude = userPoint.latitude();

        // Create a new Location object from the Point (Mapbox's Point to Android's Location)
        Location newLocation = new Location("Mapbox");
        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);

        // Create a list of Location objects (even if it's just one)
        List<Location> locationList = Collections.singletonList(newLocation);

        // You need to provide animation functions (they can be empty if you don't want animations)
        Function1<ValueAnimator, Unit> startAnimation = new Function1<ValueAnimator, Unit>() {
            @Override
            public Unit invoke(ValueAnimator animator) {
                // No-op or custom animation start logic here
                return null;
            }
        };

        Function1<ValueAnimator, Unit> endAnimation = new Function1<ValueAnimator, Unit>() {
            @Override
            public Unit invoke(ValueAnimator animator) {
                // No-op or custom animation end logic here
                return null;
            }
        };

        // Now, call changePosition with all required parameters
        navigationLocationProviderr.changePosition(newLocation, locationList, startAnimation, endAnimation);
        locationPlugin();
    }

    public void fetchRoute (Point point) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setCancelable(false);

        View view = getLayoutInflater().inflate(R.layout.custom_update_alert,null);
        alert.setView(view);
        alertt = alert.create();

        Button update = view.findViewById(R.id.btn_yes_confirm);
        Button cancel = view.findViewById(R.id.btn_cancel);

        alertt.show();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double longitude = point.longitude();
                double latitude = point.latitude();

                // Round to 6 decimal places
                latitude = Math.round(latitude * 1_000_000.0) / 1_000_000.0;
                longitude = Math.round(longitude * 1_000_000.0) / 1_000_000.0;
                Log.e("POTAKA",String.valueOf(latitude + " " + longitude));


                DatabaseReference databaseReference = firebaseDatabase.getReference("User SafeLocation")
                        .child(userId);


                Map<String,Object> insertInfo = new HashMap<>();
                insertInfo.put("longitude",String.valueOf(longitude));
                insertInfo.put("latitude",String.valueOf(latitude));
                insertInfo.put("radius",(meter_val.getText().toString()));
                insertInfo.put("userID",userId);
                alertt.dismiss();
                databaseReference.setValue(insertInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if (register_class != null && !register_class.isEmpty()) {
                            Intent i = new Intent(User_map_set_location.this,Sucessfull_update.class);
                            i.putExtra("register_class","safe");
                            i.putExtra("previousFragment",previousFragment_fromregister);
                            startActivity(i);
                        }else {
                            // go to the map fragment
                            Intent i = new Intent(User_map_set_location.this,Sucessfull_update.class);
                            i.putExtra("SafeZonee","safe");
                            startActivity(i);
                        }
                    }
                });
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertt.dismiss();
            }
        });

    }

    private void checkInternet () {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(new ConnectionReceiver(), intentFilter);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
        showResultInternet(isConnected);
    }

    private void showResultInternet(boolean isConnectedd) {
        if (isConnectedd) {
            Internet.dismiss();
            haveInternet = true;
        }else {
            Internet.show();
            haveInternet = false;
        }
    }

    @Override
    public void onNetworkChange (boolean isCoonected) {
        // showResultInternet (isCoonected);
    }

    @Override
    public void onBackPressed () {
        Intent intent = new Intent(User_map_set_location.this,Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
        intent.putExtra("SafeZone","SafeZone");
        startActivity(intent);
        finish();
    }


}