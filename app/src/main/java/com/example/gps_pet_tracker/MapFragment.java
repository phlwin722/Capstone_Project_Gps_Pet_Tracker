package com.example.gps_pet_tracker;

// Importing necessary libraries and classes
// Importing necessary libraries and classes

import static android.os.Looper.getMainLooper;
import static com.google.android.material.internal.ViewUtils.hideKeyboard;
import static com.mapbox.maps.plugin.animation.CameraAnimationsUtils.getCamera;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;
import static com.mapbox.navigation.base.extensions.RouteOptionsExtensions.applyDefaultNavigationOptions;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.Bearing;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.bindgen.Expected;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.CameraState;
import com.mapbox.maps.CoordinateBounds;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.observable.eventdata.CameraChangedEventData;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotation;
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions;
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentConstants;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;
import com.mapbox.maps.plugin.locationcomponent.generated.LocationComponentSettings;
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.base.route.NavigationRoute;
import com.mapbox.navigation.base.route.NavigationRouterCallback;
import com.mapbox.navigation.base.route.RouterFailure;
import com.mapbox.navigation.base.route.RouterOrigin;
import com.mapbox.navigation.base.trip.model.RouteLegProgress;
import com.mapbox.navigation.base.trip.model.RouteProgress;
import com.mapbox.navigation.base.trip.model.RouteProgressState;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.directions.session.RoutesObserver;
import com.mapbox.navigation.core.directions.session.RoutesUpdatedResult;
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter;
import com.mapbox.navigation.core.trip.session.LocationMatcherResult;
import com.mapbox.navigation.core.trip.session.LocationObserver;
import com.mapbox.navigation.core.trip.session.RouteProgressObserver;
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer;
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi;
import com.mapbox.navigation.ui.maneuver.model.Maneuver;
import com.mapbox.navigation.ui.maneuver.model.ManeuverError;
import com.mapbox.navigation.ui.maneuver.view.MapboxManeuverView;
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider;
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi;
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView;
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView;
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineClearValue;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineError;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources;
import com.mapbox.navigation.ui.maps.route.line.model.RouteSetValue;
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi;
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer;
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement;
import com.mapbox.navigation.ui.voice.model.SpeechError;
import com.mapbox.navigation.ui.voice.model.SpeechValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MapFragment extends Fragment implements ConnectionReceiver.ReceiverListener {
    // Firebase instances for authentication, database, and storage
    private FirebaseAuth firebaseAuth;
    PointAnnotation existingMarker;
    private FirebaseDatabase firebaseDatabase;
    private ConnectionReceiver connectionReceiver;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private String Userid; // User ID
    private DatabaseReference fetchList; // Reference to fetch pet data from Firebase
    private long lastFetchTime = 0;
    private static final long FETCH_INTERVAL_MS = 2000; // 2 seconds
    private Point petPoint;
    long routeRequestId = -1;
    private Point userPoint;
    private Button Btn_reCenter;
    FusedLocationProviderClient fusedLocationClient;
    LocationEngine locationEnginee;
    private Button done_route;
    // Variable to store the last known position
    private FloatingActionButton btn_mylocation; // Button to center map on user location
    private FloatingActionButton btn_map_type, btn_safe_zone;
    String set = "";
    private FrameLayout arrival_pet;
    private Button btn_ok_on_check_internet;
    private Button viewInformation;
    private FrameLayout map_type;
    private FrameLayout info_panel;
    private FrameLayout petInformation; // Layout to display pet information
    private FrameLayout result_search_on_map_fragment;
    private FrameLayout pet_direction;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String DENIAL_COUNT_KEY = "DenialCount";
    private MapboxRouteLineView routeLineView;
    private MapboxRouteLineApi routeLineApi;
    private MapboxManeuverView mapboxManeuverView;
    boolean removeMarker = false;
    private MapboxManeuverApi maneuverApi;
    private MapboxRouteArrowView routeArrowView;
    private MapboxRouteArrowApi routeArrowApi = new MapboxRouteArrowApi();
    private MapboxNavigation mapboxNavigation;
    private AnnotationPlugin annotationPlugin;
    private PointAnnotationManager pointAnnotationManager; // Manages point annotations on the map
    private MapView mapView;
    private TextView distance;
    private TextView timeConsume;
    private LocationEngine locationEngineUser;
    private AlertDialog showInternet;
    private Marker marker;
    double newLng, newLat;
    private FirebaseFirestore firebaseFirestore;
    private double petLongitudeChange = 0.0;
    private double petLatitudeChange = 0.0;
    private boolean DirectionIsTrue = false;
    private MapboxVoiceInstructionsPlayer mapboxVoiceInstructionsPlayer;
    private final NavigationLocationProvider navigationLocationProvider = new NavigationLocationProvider();
    private final NavigationLocationProvider navigationLocationProviderr = new NavigationLocationProvider();
    private MapboxSpeechApi speechApi;
    private AlertDialog showLoadingProcess;
    private SearchView searchPet, searchPetFromResultLayout;
    private List<Marker> petList;
    private RecyclerView RecyclerViewPetResult;
    private ScrollView scrollViewPet;
    private serchResultPetAdapter serchResultPetAdapter;
    LocationComponentPlugin locationComponentPlugin;
    TextView petId, petName, petCategory, petStatus;
    ImageView petImagee, petImageeee;
    private String arduinoIDArriving = "";
    private String petNameArriving = "";
    Button close, Direction;
    private boolean fetchZone = false;
    private   PetInformationAndLongitudeLatitude petInformationAndLongitudeLatitude;
    private boolean updataCameraToNavView = false;
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private boolean haveInternet = false;
    private Point reCenter;
    private double reCenterBearing;
    private boolean focusLocation = false;
    private boolean Panel_info_navigation = false;
    private boolean turnOffPetInformation = false;
    private boolean isVoiceInstructionsMuted = false;
    private PointAnnotation currentAnnotation = null; // Variable to track the currently selected annotation
    private boolean isPetInformationVisible = false; // Variable to track if the pet_direction view is currently visible
    private boolean turnOnAfterPermissionWhenIsOff = false;
    private static final double OFF_ROUTE_THRESHOLD = 50.0; // in meters
    private Map<String, PointAnnotation> markerMap = new HashMap<>();  // Declare a map to store markers by ArduinoId
    // List to store annotations
    List<PointAnnotation> annotations = new ArrayList<>();

    private static final String PREFS_NAME = "map_prefs"; // Name of the SharedPreferences file
    private static final String KEY_LONGITUDE = "camera_center_longitude"; // Key for saving longitude
    private static final String KEY_LATITUDE = "camera_center_latitude"; // Key for saving latitude
    private static final String KEY_ZOOM = "camera_zoom"; // Key for saving zoom level
    private static final String KEY_BEARING = "camera_bearing"; // Key for saving bearing
    private static final String KEY_PITCH = "camera_pitch"; // Key for saving pitch

    private MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> speechCallback = new MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>>() {
        @Override
        public void accept(Expected<SpeechError, SpeechValue> speechErrorSpeechValueExpected) {
            speechErrorSpeechValueExpected.fold(new Expected.Transformer<SpeechError, Unit>() {
                @NonNull
                @Override
                public Unit invoke(@NonNull SpeechError input) {
                    mapboxVoiceInstructionsPlayer.play(input.getFallback(), voiceInstructionsPlayerCallback);
                    return Unit.INSTANCE;
                }
            }, new Expected.Transformer<SpeechValue, Unit>() {
                @NonNull
                @Override
                public Unit invoke(@NonNull SpeechValue input) {
                    mapboxVoiceInstructionsPlayer.play(input.getAnnouncement(), voiceInstructionsPlayerCallback);
                    return Unit.INSTANCE;
                }
            });
        }
    };

    private MapboxNavigationConsumer<SpeechAnnouncement> voiceInstructionsPlayerCallback = new MapboxNavigationConsumer<SpeechAnnouncement>() {
        @Override
        public void accept(SpeechAnnouncement speechAnnouncement) {
            speechApi.clean(speechAnnouncement);
        }
    };

    // Listener for map move gestures
    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            if (updataCameraToNavView) {
                if (focusLocation) {
                    focusLocation = false;
                    // Remove listeners when map movement begins
                    getGestures(mapView).removeOnMoveListener(this);
                    Btn_reCenter.setVisibility(View.VISIBLE);
                }
            }else {
                btn_mylocation.setImageResource(R.drawable.baseline_location_searching_24);
                getLocationComponent(mapView).removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
                getGestures(mapView).removeOnMoveListener(this);
            }
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {

        }
    };

    private final RoutesObserver routesObserver = new RoutesObserver() {
        @Override
        public void onRoutesChanged(@NonNull RoutesUpdatedResult routesUpdatedResult) {
            routeLineApi.setNavigationRoutes(routesUpdatedResult.getNavigationRoutes(), new MapboxNavigationConsumer<Expected<RouteLineError, RouteSetValue>>() {
                @Override
                public void accept(Expected<RouteLineError, RouteSetValue> routeLineErrorRouteSetValueExpected) {
                    Style style = mapView.getMapboxMap().getStyle();
                    if (style != null) {
                        routeLineView.renderRouteDrawData(style, routeLineErrorRouteSetValueExpected);
                    }
                }
            });
        }
    };

    private final LocationObserver locationObserver = new LocationObserver() {
        @Override
        public void onNewRawLocation(@NonNull Location location) {

        }

        @Override
        public void onNewLocationMatcherResult(@NonNull LocationMatcherResult locationMatcherResult) {
            Location location = locationMatcherResult.getEnhancedLocation();
            navigationLocationProvider.changePosition(location, locationMatcherResult.getKeyPoints(), null, null);
            updateCamera(Point.fromLngLat(location.getLongitude(), location.getLatitude()), (double) location.getBearing());
            reCenter = Point.fromLngLat(location.getLongitude(),location.getLatitude());
            reCenterBearing = location.getBearing();
        }
    };

    private void updateCamera(Point point, Double bearing) {
        if (focusLocation) {
            MapAnimationOptions animationOptions3 = new MapAnimationOptions.Builder().duration(1000).build();
            CameraOptions cameraOptions3 = new CameraOptions.Builder()
                    .zoom(17.0).bearing(bearing) // Maintain the current bearing
                    .center(point).pitch(50.0) // The current center point
                    .padding(new EdgeInsets(1000.0, 0.0, 0.0, 0.0)).build(); // Padding (optional)
            getCamera(mapView).easeTo(cameraOptions3, animationOptions3);
        }
    }

    private RouteProgressObserver routeProgressObserver = new RouteProgressObserver() {
        @Override
        public void onRouteProgressChanged(@NonNull RouteProgress routeProgress) {

          /*  if (isUserOffRoute(routeProgress)) {
                Toast.makeText(getContext(), "You are off route!", Toast.LENGTH_SHORT).show();
                // Additional logic for rerouting can be added here if necessary
            }*/

            // Get the remaining duration in seconds
            double durationRemaining = routeProgress.getDurationRemaining();
            // calculate hours remaining
            int hourRemaining = (int) (durationRemaining / 3600);
            int minutesRemainig = (int) ((durationRemaining % 3600) / 60);
            // Display hours and minutes in a textView
            String timeRemainingText;
            if (hourRemaining > 0) {
                // Format the time as "( X hr Y min )"
                timeRemainingText = String.format("%d hr %d min", hourRemaining, minutesRemainig);
            } else {
                // Format the time as "( X min )"
                timeRemainingText = String.format("%d min", minutesRemainig);
            }

            // get  // Distance remaining
            double distanceRemaining = routeProgress.getDistanceRemaining();
            String distanceRemainingText, distanceRemainingnotText = "";
            if (distanceRemaining >= 1000) {
                double kilometerRemaining = distanceRemaining / 1000.0;
                if (Panel_info_navigation) {
                    distanceRemainingText = String.format("%.2f km", kilometerRemaining);
                } else {
                    distanceRemainingText = String.format("( %.2f km )", kilometerRemaining);
                }
            } else {
                if (Panel_info_navigation) {
                    distanceRemainingText = String.format("%.0f meter", distanceRemaining);
                    //  distanceRemainingnotText =String.format("%.0f", distanceRemaining);
                } else {
                    distanceRemainingText = String.format("( %.0f meter )", distanceRemaining);
                }
            }

            // eta estimated ARRIVAL TIME
            long arrival_Time_user = 0;
            RouteLegProgress arrival_time = routeProgress.getCurrentLegProgress();
            if (arrival_time != null && routeProgress.getCurrentState() != RouteProgressState.COMPLETE) {
                arrival_Time_user = System.currentTimeMillis() + (long) (routeProgress.getDurationRemaining() * 1000);
            }

            if (Panel_info_navigation) {
                DirectionIsTrue = false;

                // get current hour to check day or night
                Calendar calendar = Calendar.getInstance();
                int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                if (hourOfDay >= 18 || hourOfDay < 6) {
                    mapView.getMapboxMap().loadStyleUri(Style.DARK);
                } else {
                    mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS);
                }

                Style style = mapView.getMapboxMap().getStyle();
                if (style != null) {
                    routeArrowView.renderManeuverUpdate(style, routeArrowApi.addUpcomingManeuverArrow(routeProgress));
                }

                maneuverApi.getManeuvers(routeProgress).fold(new Expected.Transformer<ManeuverError, Object>() {
                    @NonNull
                    @Override
                    public Object invoke(@NonNull ManeuverError input) {
                        return new Object();
                    }
                }, new Expected.Transformer<List<Maneuver>, Object>() {
                    @NonNull
                    @Override
                    public Object invoke(@NonNull List<Maneuver> input) {
                        mapboxManeuverView.setVisibility(View.VISIBLE);
                        mapboxManeuverView.renderManeuvers(maneuverApi.getManeuvers(routeProgress));
                        return new Object();
                    }
                });

                LayoutInflater layoutInflaterr = getLayoutInflater();
                View view_panel = layoutInflaterr.inflate(R.layout.custominfopaneltripprogress, null);

                Button closeTripProgess = view_panel.findViewById(R.id.closeTripProgess);
                TextView minutesRemainingPet = view_panel.findViewById(R.id.minutesRemaining);
                TextView distanceRemainingPet = view_panel.findViewById(R.id.distanceRemaining);
                TextView arrivalTimeUser = view_panel.findViewById(R.id.arrivalTime);

                distanceRemainingPet.setText(distanceRemainingText);
                minutesRemainingPet.setText(timeRemainingText);
                arrivalTimeUser.setText(String.format("%1$tH:%1$tM", arrival_Time_user));

                closeTripProgess.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setCancelable(false);

                        View view1 = getLayoutInflater().inflate(R.layout.custom_exit_navigation_alert,null);
                        alert.setView(view1);

                        Button yes = view1.findViewById(R.id.btn_exit_navigation);
                        Button no = view1.findViewById(R.id.btn_cancel);

                        AlertDialog alerttt = alert.create();

                        alerttt.show();

                        yes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alerttt.dismiss();
                                mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS);
                                updataCameraToNavView = false;
                                Panel_info_navigation = false;
                                focusLocation = false;
                                petNameArriving = "";
                                arduinoIDArriving = "";

                                //                          proceedAfterPermission();
                                // Handle the user's confirmation to exit navigation1

                                ExitNavigationIcon ();

                                Btn_reCenter.setVisibility(View.GONE);
                                info_panel.setVisibility(View.GONE);
                                btn_map_type.setVisibility(View.VISIBLE);
                                btn_safe_zone.setVisibility(View.VISIBLE);
                                btn_mylocation.setVisibility(View.VISIBLE);
                                searchPet.setVisibility(View.VISIBLE);
                                mapboxManeuverView.setVisibility(View.GONE);
                                mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver);
                                mapboxNavigation.unregisterLocationObserver(locationObserver);
                                mapboxNavigation.unregisterRoutesObserver(routesObserver);
                                showTabLayout();
                                clearRouteLine();

                                //center the map on the user location
                                CameraOptions cameraOptions2 = new CameraOptions.Builder()
                                        .center(petPoint)
                                        .zoom(17.0)
                                        .bearing(0.0) // Optionally reset bearing to 0 (north-up orientation)
                                        .pitch(0.0) // Set pitch to 0 for 2D view
                                        .padding(new EdgeInsets(0.0, 0.0, 0.0, 0.0))
                                        .build();
                                getCamera(mapView).easeTo(cameraOptions2, new MapAnimationOptions.Builder().duration(1000).build());
                            }
                        });

                        no.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alerttt.dismiss();
                            }
                        });
                    }
                });

                info_panel.removeAllViews();
                info_panel.addView(view_panel);
                info_panel.setVisibility(View.VISIBLE);

                if (distanceRemaining < 5) {
                    if (arduinoIDArriving != null && petNameArriving != null) {
                        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS);
                        ExitNavigationIcon ();
                        LayoutInflater inflater6 = getLayoutInflater();

                        View view_arrival = inflater6.inflate(R.layout.custom_arrival_pet, null);

                        ImageView imagePEt = view_arrival.findViewById(R.id.imageView2);
                        TextView petName = view_arrival.findViewById(R.id.petName);

                        // Load pet image with Glide
                        StorageReference petImageRef = storageReference.child("Pet Image/" + Userid + "/" + arduinoIDArriving + "/profile.jpg");
                        petImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Glide.with(getContext())
                                    .load(uri)
                                    .override(100, 100) // Resize image
                                    .centerCrop()
                                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                    .into(imagePEt);
                        }).addOnFailureListener(exception -> {
                            Toast.makeText(getContext(), "Failed to load image: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("Map", "Image load failed: ", exception);
                        });

                        petName.setText(petNameArriving);

                        // Handle the user's confirmation to exit navigation
                        updataCameraToNavView = false;
                        Panel_info_navigation = false;
                        focusLocation = false;
                        Btn_reCenter.setVisibility(View.GONE);
                        info_panel.setVisibility(View.GONE);
                        mapboxManeuverView.setVisibility(View.GONE);
                        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver);
                        mapboxNavigation.unregisterLocationObserver(locationObserver);
                        mapboxNavigation.unregisterRoutesObserver(routesObserver);
                        clearRouteLine();

                        arrival_pet.removeAllViews();
                        arrival_pet.addView(view_arrival);
                        arrival_pet.setVisibility(View.VISIBLE);
                        done_route.setVisibility(View.VISIBLE);

                        done_route.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                ExitNavigationIcon();

                                petNameArriving = "";
                                arduinoIDArriving = "";

                                done_route.setVisibility(View.GONE);
                                arrival_pet.setVisibility(View.GONE);
                                btn_map_type.setVisibility(View.VISIBLE);
                                btn_safe_zone.setVisibility(View.VISIBLE);
                                btn_mylocation.setVisibility(View.VISIBLE);
                                searchPet.setVisibility(View.VISIBLE);
                                showTabLayout();
                                //center the map on the user location
                                CameraOptions cameraOptions2 = new CameraOptions.Builder()
                                        .center(petPoint)
                                        .zoom(17.0)
                                        .bearing(0.0) // Optionally reset bearing to 0 (north-up orientation)
                                        .padding(new EdgeInsets(0.0, 0.0, 0.0, 0.0))
                                        .pitch(0.0) // Set pitch to 0 for 2D view
                                        .build();
                                getCamera(mapView).easeTo(cameraOptions2, new MapAnimationOptions.Builder().duration(1000).build());
                            }
                        });
                    }
                }

            } else {
                timeConsume.setText(timeRemainingText);
                distance.setText(distanceRemainingText);
            }
        }
    };

   /* private boolean isUserOffRoute(RouteProgress routeProgress) {
        // Define the threshold distance in meters
        final double SOME_THRESHOLD = 20.0;

        // Get the current location of the user
        Location userLocation = navigationLocationProvider.getLastLocation(); // Get the user's current location from the navigation provider

        // Get the coordinates of the nearest point on the route
        Point nearestPoint = routeProgress.getCurrentLegProgress().getCurrentStepProgress().getClosestPoint(); // Assuming this method exists

        // Calculate the distance between the user and the nearest point on the route
        if (userLocation != null && nearestPoint != null) {
            Location nearestLocation = new Location(""); // Create a new Location object
            nearestLocation.setLatitude(nearestPoint.latitude()); // Set the latitude
            nearestLocation.setLongitude(nearestPoint.longitude()); // Set the longitude

            float distanceToNearestPoint = userLocation.distanceTo(nearestLocation); // Calculate distance in meters

            return distanceToNearestPoint > SOME_THRESHOLD; // Check if the distance exceeds the threshold
        }

        return false; // Default to false if the user's location or nearest point is not available
    }*/

    private final OnIndicatorPositionChangedListener onIndicatorPositionChangedListener = new OnIndicatorPositionChangedListener() {
        @Override
        public void onIndicatorPositionChanged(@NonNull Point point) {
            CameraOptions cameraOptions = new CameraOptions.Builder()
                    .zoom(18.0)
                    .center(point)
                    .build();
            getCamera(mapView).easeTo(cameraOptions, new MapAnimationOptions.Builder().duration(1000).build());
            //getGestures(mapView).setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(point));
            getLocationComponent(mapView).removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
        }
    };

    private void initializeMapboxNavigation() {
        if (mapboxNavigation == null) {
            Context context = getContext();
            if (context != null) {
                mapboxNavigation = NavigationManager.getInstance(requireContext());
                mapboxNavigation.unregisterRoutesObserver(routesObserver);
                mapboxNavigation.unregisterLocationObserver(locationObserver);
                mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        // Initialize UI elements
        mapView = view.findViewById(R.id.mapView);
        btn_mylocation = view.findViewById(R.id.btn_mylocation);
        map_type = view.findViewById(R.id.map_type);
        info_panel = view.findViewById(R.id.info_panel);
        btn_map_type = view.findViewById(R.id.btn_map_type);
        mapboxManeuverView = view.findViewById(R.id.maneuverView);
        petInformation = view.findViewById(R.id.petInformation);
        viewInformation = view.findViewById(R.id.viewInformation);
        Btn_reCenter = view.findViewById(R.id.reCenter);
        arrival_pet = view.findViewById(R.id.arrival_pet);
        done_route = view.findViewById(R.id.done_route);
        searchPet = view.findViewById(R.id.search_id);
        result_search_on_map_fragment = view.findViewById(R.id.result_search_on_map_fragment);
        pet_direction = view.findViewById(R.id.pet_direction);
        btn_safe_zone = view.findViewById(R.id.btn_safe_zone);

        petInformation.setVisibility(View.GONE); // Hide pet information layout initially
        map_type.setVisibility(View.GONE);
        info_panel.setVisibility(View.GONE);
        arrival_pet.setVisibility(View.GONE);
        Btn_reCenter.setVisibility(View.GONE);
        done_route.setVisibility(View.GONE);

        // Initialize Firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        Userid = firebaseAuth.getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        connectionReceiver = new ConnectionReceiver();

        removeMarker = true;
        if (pointAnnotationManager != null) {
            removeAllMarkers();
            markerMap.clear();  // Clear all entries from markerMap
            pointAnnotationManager.deleteAll();
        }

        // layout alert dialog for show result internet
        AlertDialog.Builder builderInternet = new AlertDialog.Builder(getContext());
        LayoutInflater layoutInflater = getLayoutInflater();
        View custom_no_internet_view = layoutInflater.inflate(R.layout.custom_no_internet, (ViewGroup) view.findViewById(R.id.custom_no_internet));
        builderInternet.setView(custom_no_internet_view);
        showInternet = builderInternet.create();
        btn_ok_on_check_internet = custom_no_internet_view.findViewById(R.id.btn_ok);
        // layout alert dialog for show result internet

        //alert dialog the loading process
        AlertDialog.Builder loadProrcess = new AlertDialog.Builder(getContext());
        loadProrcess.setCancelable(false);
        loadProrcess.setView(R.layout.custom_loading_process);

        showLoadingProcess = loadProrcess.create();

        // Set a custom back press behavior for this fragment
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Prevent back navigation here
                // Do nothing or handle it differently
                // For example, show a Toast
                //               Toast.makeText(requireContext(), "Back navigation is disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // initialize annotation plugin and point annotation manager
        annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
        pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, new AnnotationConfig());

        // Initialize LocationEngine to update location user on realtime
        //  LocationUserRealTime();
        // initialize when AndroidDeviceChanges have longitude and latitude will be update automatically on Pet Information
        if (!removeMarker) {
            fetchPetInformation();
            fetchSafeZone();
        }
        checkLocationServiceIsOn();

        // load the mapbox Style
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                locationComponentPlugin = getLocationComponent(mapView);

                disyplaySafeZone();
                checkInternet();

                // Retrieve SharedPreferences for storing camera state
                SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                // Check if camera state has been saved in SharedPreferences
                boolean hasSavedState = prefs.contains(KEY_LONGITUDE) && prefs.contains(KEY_LATITUDE);
                if (hasSavedState) {
                    // Restore camera state if available
                    restoreCameraState();
                } else {
                    // Center the map on the Philippines with a specific zoom level
                    Point centerPhilippines = Point.fromLngLat(121.7740, 12.8797);
                    CameraOptions cameraOptions = new CameraOptions.Builder()
                            .center(centerPhilippines) // Longitude, Latitude for the center of the Philippines
                            .zoom(5.0) // Adjust the zoom level as needed
                            .build();

                    mapView.getMapboxMap().setCamera(cameraOptions);
                }

                MapboxRouteLineOptions options = new MapboxRouteLineOptions.Builder(getContext()).withRouteLineResources(new RouteLineResources.Builder().build())
                        .withRouteLineBelowLayerId(LocationComponentConstants.LOCATION_INDICATOR_LAYER).build();
                routeLineView = new MapboxRouteLineView(options);
                routeLineApi = new MapboxRouteLineApi(options);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //     checkIfLocationAccessOn();
                        initializeMapboxNavigation();
                    }
                }, 3000);
            }
        });

        // Inflate and add custom view
        LayoutInflater layoutInflater1 = getLayoutInflater();
        View view12 = layoutInflater1.inflate(R.layout.custom_result_search_on_map_fragment,null);

        RecyclerViewPetResult = view12.findViewById(R.id.RecyclerViewPetResult);
        searchPetFromResultLayout = view12.findViewById(R.id.search_idd);
        scrollViewPet = view12.findViewById(R.id.scrollViewPet);

        result_search_on_map_fragment.removeAllViews(); // Remove all views from the container and add the new view
        result_search_on_map_fragment.addView(view12);

        // Initialize RecyclerView and set its layout manager
        RecyclerViewPetResult.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize recyclerview and set its about manager
        petList = new ArrayList<>();
        serchResultPetAdapter = new serchResultPetAdapter(petList, new serchResultPetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Marker petInfor) {
                double longitudePetDouble = petInfor.getLongitude();
                double latitudePetDouble = petInfor.getLatitude();

                String longitudePet = String.valueOf(longitudePetDouble);
                String latitudePet = String.valueOf(latitudePetDouble);

                if (longitudePet.equals("0.0") && latitudePet.equals("0.0")) {
                    Toast.makeText(getContext(),"This pet are not configuration",Toast.LENGTH_SHORT).show();
                }else {
                    pet_direction_and_pet_information(petInfor);
                    searchPetFromResultLayout.onActionViewCollapsed();
                    pet_direction.setVisibility(View.VISIBLE);

                    CameraOptions cameraOptions = new CameraOptions.Builder()
                            .center(Point.fromLngLat(petInfor.getLongitude(),petInfor.getLatitude()))
                            .zoom(17.0)
                            .build();
                    getCamera(mapView).easeTo(cameraOptions, new MapAnimationOptions.Builder().duration(1000).build());
                }
            }
        });
        // Set adapter to RecyclerView
        RecyclerViewPetResult.setAdapter(serchResultPetAdapter);
        searchPet.setQueryHint("Search..."); // Clear the query without triggering a search


        // Optionally, handle initial state if needed
        searchPet.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    result_search_on_map_fragment.setVisibility(View.VISIBLE);
                    hideTablayout();
                    btn_map_type.hide();
                    btn_safe_zone.hide();
                    btn_mylocation.hide();
                    searchPet.setIconified(true); // Collapse SearchView
                    hideKeyboard(searchPet); // hide keyboard SearchView
                    searchPet.setQueryHint("Search..."); // Clear the query without triggering a search
                    searchPetFromResultLayout.onActionViewExpanded(); // Expand the search view to show the keyboard
                }
            }
        });

        searchPetFromResultLayout.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    // Expand SearchView
                    searchPet.setIconified(true);  // Collapse SearchView to did not focus typing
                    searchPetFromResultLayout.onActionViewCollapsed();
                    result_search_on_map_fragment.setVisibility(View.GONE);
                    showTabLayout();
                    btn_map_type.show();
                    btn_safe_zone.show();
                    btn_mylocation.show();
                    searchPet.setQuery("", false); // Clear the query without triggering a new search
                }
            }
        });

        // search the pet
        searchPetFromResultLayout.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()){
                    scrollViewPet.setVisibility(View.GONE);
                }else {
                    scrollViewPet.setVisibility(View.VISIBLE);
                    searchList (newText);

                }
                return true;
            }
        });

        btn_safe_zone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchZone = false;
                fetchSafeZone();
            }
        });

        Btn_reCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (reCenter == null) {
                    Toast.makeText(getContext(),"noITem",Toast.LENGTH_SHORT).show();
                }
                MapAnimationOptions animationOptions3 = new MapAnimationOptions.Builder().duration(1000).build();
                CameraOptions cameraOptions3 = new CameraOptions.Builder()
                        .zoom(17.0)
                        .bearing(reCenterBearing) // Maintain the current bearing
                        .center(reCenter)
                        .pitch(50.0) // The current center point
                        .padding(new EdgeInsets(1000.0, 0.0, 0.0, 0.0)).build(); // Padding (optional)
                getCamera(mapView).easeTo(cameraOptions3, animationOptions3);

                focusLocation = true;
                getGestures(mapView).addOnMoveListener(onMoveListener);
                Btn_reCenter.setVisibility(View.GONE);
            }
        });

        btn_map_type.setOnClickListener(new View.OnClickListener() {
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
                petInformation.setVisibility(View.GONE); // hide the frame layout

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

        btn_ok_on_check_internet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInternet.dismiss();
            }
        });

        btn_mylocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionsAndProceed ();
              /*  if (isFabVisible) {
                    checkPermissionsAndProceed ();
                    btn_mylocation.setImageResource(R.drawable.baseline_my_location_buttn_24);  // Change to new icon
                    Log.d("TAG", "Icon changed to baseline_my_location_buttn_24");
                }else {
                    ExitNavigationIcon();
                    btn_mylocation.setImageResource(R.drawable.baseline_my_location_24); // Change to original icon
                    Log.d("TAG", "Icon changed to baseline_my_location_24");
                }
                isFabVisible = !isFabVisible; // Toggle the visibility state*/
            }
        });

        viewInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewInformation.setVisibility(View.GONE);
                petInformation.setVisibility(View.VISIBLE);

                // Create a bounding box with the user's location and the pet's location
                CoordinateBounds bounds = new CoordinateBounds(userPoint, petPoint);

                // Define the padding using EdgeInsets
                EdgeInsets padding = new EdgeInsets(250, 400, 850, 300); // Top, Left, Bottom, Right padding

                // Set up the camera options to fit the bounding box with padding
                CameraOptions cameraOptions1 = mapView.getMapboxMap().cameraForCoordinateBounds(bounds, padding, null, null);

                // Move the camera to fit the bounding box
                getCamera(mapView).easeTo(cameraOptions1, new MapAnimationOptions.Builder()
                        .duration(1000) // Adjust the duration of the animation as needed
                        .build());
            }
        });

        maneuverApi = new MapboxManeuverApi(new MapboxDistanceFormatter(new DistanceFormatterOptions.Builder(getContext()).build()));
        routeArrowView = new MapboxRouteArrowView(new RouteArrowOptions.Builder(getContext()).build());

        speechApi = new MapboxSpeechApi(getContext(), getString(R.string.mapbox_access_token), Locale.US.toLanguageTag());
        mapboxVoiceInstructionsPlayer = new MapboxVoiceInstructionsPlayer(getContext(), Locale.US.toLanguageTag());


        return view;
    }

    private void disyplaySafeZone() {
        DatabaseReference databaseReference = firebaseDatabase.getReference("User SafeLocation")
                .child(Userid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    double longitude = Double.parseDouble(snapshot.child("longitude").getValue(String.class));
                    double latitude = Double.parseDouble(snapshot.child("latitude").getValue(String.class));
                    float initialRadius = Float.parseFloat(snapshot.child("radius").getValue(String.class));
                    double radiusIncrese = initialRadius * 3.38;

                    Point userSafeLocation = Point.fromLngLat(longitude, latitude);

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.safe_zonee);
                    Bitmap resizeImage = Bitmap.createScaledBitmap(bitmap, 80, 80, false);

                    PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                            .withTextAnchor(TextAnchor.CENTER)
                            .withIconImage(resizeImage)
                            .withPoint(userSafeLocation);

                    pointAnnotationManager.create(pointAnnotationOptions);

                    // Create a CircleAnnotationManager using the correct class
                    CircleAnnotationManager circleAnnotationManager = CircleAnnotationManagerKt.createCircleAnnotationManager(annotationPlugin, (AnnotationConfig) null);

                    // Create CircleAnnotationOptions for the circle around the user
                    CircleAnnotationOptions circleAnnotationOptions = new CircleAnnotationOptions()
                            .withPoint(userSafeLocation)
                          //  .withCircleRadius(initialRadius) // Initial radius in meters
                            .withCircleColor(Color.parseColor("#1E90FF")) // Dodger Blue color
                            .withCircleOpacity(0.1); // 10% opacity

                    // Create the circle annotation
                    CircleAnnotation circleAnnotation = circleAnnotationManager.create(circleAnnotationOptions);

                    // Add a camera change listener to update the circle radius
                    mapView.getMapboxMap().addOnCameraChangeListener(new OnCameraChangeListener() {
                        @Override
                        public void onCameraChanged(@NonNull CameraChangedEventData cameraChangedEventData) {
                            CameraState cameraState = mapView.getMapboxMap().getCameraState();
                            double zoomLevel = cameraState.getZoom();
                            double newRadius = calculateCircleRadius(zoomLevel, radiusIncrese);
                            Log.d("MapFragmenty", "Updated Circle Radius: " + zoomLevel);
                            Log.d("MapFragmenty", "Updated Circle Radius: " + newRadius);
                            circleAnnotation.setCircleRadius(newRadius); // Set the updated radius
                            circleAnnotationManager.update(circleAnnotation); // Update the circle annotation
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });
    }

    private double calculateCircleRadius(double zoomLevel, double baseRadius) {
        double minZoom = 15;  // Adjust this to your desired minimum zoom level
        double maxZoom = 18;  // Adjust this to your desired maximum zoom level

        if  (zoomLevel == maxZoom) {
            return baseRadius;
            // Reduce the radius as zoom level increases, but ensure the radius does not grow
         //   double factor = Math.pow(2, maxZoom - zoomLevel);  // Exponentially decrease as zoom increases
         ///   return baseRadius / factor;  // Shrink the radius based on zoom level
        } else if (zoomLevel > maxZoom) {
            // Reduce the radius as zoom level increases, but ensure the radius does not grow
            double factor = Math.pow(2, maxZoom - zoomLevel);  // Exponentially decrease as zoom increases
            return baseRadius / factor;  // Shrink the radius based on zoom level
          ///  return baseRadius / Math.pow(2, zoomLevel - minZoom);  // Gradual shrinkage based on zoom
        }
        else if (zoomLevel < maxZoom && zoomLevel > minZoom) {
            // Reduce the radius as zoom level increases, but ensure the radius does not grow
            double factor = Math.pow(2, maxZoom - zoomLevel);  // Exponentially decrease as zoom increases
            return baseRadius / factor;  // Shrink the radius based on zoom level
        } else {
            // If the zoom level is below minZoom, the circle shrinks at a faster rate
            return baseRadius = 10.0;
        }
    }


    private void fetchSafeZone () {
        DatabaseReference databaseReference = firebaseDatabase.getReference("User SafeLocation")
                .child(Userid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    double longitude = Double.parseDouble(snapshot.child("longitude").getValue(String.class));
                    double latitude = Double.parseDouble(snapshot.child("latitude").getValue(String.class));

                    Point userSafeLocaiton = Point.fromLngLat(longitude,latitude);

                    CameraOptions cameraOptions = new CameraOptions.Builder()
                            .center(userSafeLocaiton)
                            .zoom(18.0)
                            .build();
                    getCamera(mapView).easeTo(cameraOptions, new MapAnimationOptions
                            .Builder()
                            .duration(1000)
                            .build());

                    Bitmap bitmape = BitmapFactory.decodeResource(getResources(), R.drawable.safe_zonee);
                    Bitmap resizeImage = Bitmap.createScaledBitmap(bitmape, 80,80,false);

                    if (pointAnnotationManager != null) {
                        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                                .withTextAnchor(TextAnchor.CENTER)
                                .withIconImage(resizeImage)
                                .withPoint(userSafeLocaiton);

                        pointAnnotationManager.create(pointAnnotationOptions);
                    } else {
                        Log.e("MapFragment", "PointAnnotationManager is not initialized.");
                    }


                }else{

                    if (fetchZone) {
                        return;
                    }

                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setCancelable(false);

                    LayoutInflater layoutInflater = getLayoutInflater();
                    View view = layoutInflater.inflate(R.layout.custom_safezon_alert,null);
                    alert.setView(view);
                    AlertDialog alertt = alert.create();

                    alertt.show();

                    Button set = view.findViewById(R.id.btn_set_now);
                    Button cancel = view.findViewById(R.id.btn_cancel);

                    set.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(getContext(), User_map_set_location.class);
                            startActivity(i);
                            alertt.dismiss();
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertt.dismiss();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void LocationUserRealTime() {
        if (userPoint == null) {
            locationEnginee = LocationEngineProvider.getBestLocationEngine(getContext());

            // Create a LocationEngineRequest for high accuracy
            LocationEngineRequest request = new LocationEngineRequest.Builder(500) // Update interval in 20 seconds
                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                    .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                    //  .setDisplacement(0f)  // Minimum displacement of 10 meters before triggering an update
                    .build();

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationEnginee.requestLocationUpdates(request, new LocationEngineCallback<LocationEngineResult>() {
                    @Override
                    public void onSuccess(LocationEngineResult result) {
                        Context context = getContext();
                        if (context != null) {
                            Location location = result.getLastLocation();
                            // insert their latest location when user click close navigation


                            if (location != null) {
                                // Convert location to a GeoJSON Point if needed
                                userPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                                // Ensure `userPoint` is valid before calling `changePosition`
                                if (userPoint != null) {
                                    NavigationOptions navigationOptions = new NavigationOptions.Builder(requireContext())
                                            .accessToken(getString(R.string.mapbox_access_token))
                                            .locationEngine(locationEnginee)
                                            .build();

//                                    mapboxNavigation = new MapboxNavigation(navigationOptions);

                                    // Create a new Location object and set its coordinates based on userPoint
                                    Location updatedLocation = new Location("userLocation");
                                    updatedLocation.setLatitude(userPoint.latitude());
                                    updatedLocation.setLongitude(userPoint.longitude());

                                    //             navigationLocationProviderr.changePosition(updatedLocation, result.getLocations(), null, null);
                                } else {
                                    Log.e("LocationError", "User point is null; changePosition not called.");
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getContext(),exception.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }, getMainLooper());
            }
        }else {
            LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(getContext());

            // Create a LocationEngineRequest for high accuracy
            LocationEngineRequest request = new LocationEngineRequest.Builder(700) // Update interval in 20 seconds
                    .setPriority(LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                    .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                    .setDisplacement(0f)  // Minimum displacement of 10 meters before triggering an update
                    .build();

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationEngine.requestLocationUpdates(request, new LocationEngineCallback<LocationEngineResult>() {
                    @Override
                    public void onSuccess(LocationEngineResult result) {
                        Context context = getContext();
                        if (context != null) {
                            Location location = result.getLastLocation();
                            // insert their latest location when user click close navigation


                            if (location != null) {
                                // Convert location to a GeoJSON Point if needed
                                userPoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                                // Ensure `userPoint` is valid before calling `changePosition`
                                if (userPoint != null) {
                                    NavigationOptions navigationOptions = new NavigationOptions.Builder(requireContext())
                                            .accessToken(getString(R.string.mapbox_access_token))
                                            .locationEngine(locationEnginee)
                                            .build();

                                    //   mapboxNavigation = new MapboxNavigation(navigationOptions);
                                    // Create a new Location object and set its coordinates based on userPoint
                                    Location updatedLocation = new Location("userLocation");
                                    updatedLocation.setLatitude(userPoint.latitude());
                                    updatedLocation.setLongitude(userPoint.longitude());

                                    //                      navigationLocationProviderr.changePosition(updatedLocation, result.getLocations(), null, null);
                                } else {
                                    Log.e("LocationError", "User point is null; changePosition not called.");
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getContext(),exception.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }, getMainLooper());
            }
        }
    }

    private void checkLocationServiceIsOn () {
        LocationServiceIsOn();
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (locationComponentPlugin != null) {
                    Context context = getContext();

                    if (context != null) {
                        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            if (!updataCameraToNavView){

                                locationComponentPlugin.setLocationProvider(navigationLocationProviderr);
                                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.userdeviceicon);
                                Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap,65, 65,false);
                                Drawable drawable = new BitmapDrawable(getResources(),resizeBitmap);

                                locationComponentPlugin.updateSettings(new Function1<LocationComponentSettings, Unit>() {
                                    @Override
                                    public Unit invoke(LocationComponentSettings locationComponentSettings) {
                                        locationComponentSettings.setPulsingEnabled(true);
                                        locationComponentSettings.setPulsingMaxRadius(30.0F);
                                        locationComponentSettings.setEnabled(true);
                                        locationComponentSettings.setLocationPuck(new LocationPuck2D(null,drawable,null));
                                        return null;
                                    }
                                });
                            }
                        } else {
                            locationComponentPlugin.updateSettings(new Function1<LocationComponentSettings, Unit>() {
                                @Override
                                public Unit invoke(LocationComponentSettings locationComponentSettings) {
                                    locationComponentSettings.setEnabled(false);
                                    locationComponentSettings.setPulsingEnabled(false);
                                    locationComponentSettings.setLocationPuck(new LocationPuck2D(null,null,null));
                                    return null;
                                }
                            });
                        }
                    }

                    handler.postDelayed(this,1000);
                }
            }
        };
        handler.post(runnable);
    }

    @SuppressLint("InlinedApi")
    private void checkPermissionsAndProceed() {
        String[] permissions = null;
        if (Build.VERSION.SDK_INT == 34) { // check if android 14 or api 14
            permissions = new String[]{
                    Manifest.permission.FOREGROUND_SERVICE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
            };

            if (hasPermissions(permissions)) {
                // Permissions granted, check if location services are enabled
                LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    // Location services enabled, proceed with your logic
                    proceedAfterPermission();

                } else {
                    // Location services disabled, show alert dialog
                    showLocationServicesAlertDialog();
                }
            } else {
                // Permissions not granted, request permissions
                requestLocationPermissions(permissions);
            }
        }else if (Build.VERSION.SDK_INT == 33){
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
            };

            if (hasPermissions(permissions)) {
                // Permissions granted, check if location services are enabled
                LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    // Location services enabled, proceed with your logic
                    proceedAfterPermission();

                } else {
                    // Location services disabled, show alert dialog
                    showLocationServicesAlertDialog();
                }
            } else {
                // Permissions not granted, request permissions
                requestLocationPermissions(permissions);
            }
        } else {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };

            if (hasPermissions(permissions)) {
                // Permissions granted, check if location services are enabled
                LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    // Location services enabled, proceed with your logic
                    proceedAfterPermission();

                } else {
                    // Location services disabled, show alert dialog
                    showLocationServicesAlertDialog();
                }
            } else {
                // Permissions not granted, request permissions
                requestLocationPermissions(permissions);
            }
        }

    }

    private void requestLocationPermissions(String[] permissions) {
        if (shouldShowRequestPermissionRationale(getActivity(), permissions)) {
            // Show a dialog explaining why the permission is needed, if required
            showPermissionRationaleDialog(permissions);
        } else {
            // Directly request permissions
            ActivityCompat.requestPermissions(requireActivity(), permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private void showPermissionRationaleDialog(String[] permissions) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
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
                ActivityCompat.requestPermissions(requireActivity(), permissions, PERMISSION_REQUEST_CODE);
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show.dismiss();
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
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
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void showLocationServicesAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.custom_location_service_alert, null);
        builder.setView(view);

        AlertDialog alert = builder.create();

        alert.show();

        Button btn_confirm = view.findViewById(R.id.btn_confirm);
        Button btn_cancell = view.findViewById(R.id.btn_cancel);


        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        btn_cancell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                Toast.makeText(getContext(), "Location services are disabled. The app may not work as expected.", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void LocationServiceIsOn() {
        //get the user accuracy using google play service location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // Use high accuracy

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        for (Location location : locationResult.getLocations()) {
                            // Do something with the new location
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            float accuracy = location.getAccuracy();  // User's accuracy in meters

                            // Wrap the location into a list (required by changePosition)
                            List<Location> locationList = Collections.singletonList(location);

                            // Define animation callbacks (onStart and onEnd for smooth animation)
                            Function1<ValueAnimator, Unit> onStart = animator -> {
                                // Logic for the start of animation
                                animator.setDuration(5000);  // Set duration of animation
                                return Unit.INSTANCE;
                            };

                            Function1<ValueAnimator, Unit> onEnd = animator -> {
                                // Logic for the end of animation (optional)
                                return Unit.INSTANCE;
                            };

                            // Call changePosition with the correct arguments (Location, List<Location>, onStart, onEnd)
                            if (navigationLocationProviderr != null) {
                                navigationLocationProviderr.changePosition(location, locationList, onStart, onEnd);
                            }

                            // Display latitude, longitude, and accuracy in a Toast
                            //  Toast.makeText(requireContext(), "Lat: " + latitude + " Lon: " + longitude + " Accuracy: " + accuracy, Toast.LENGTH_SHORT).show();

                            // Log the location data for debugging
                            Log.d("LocationUser", "Latitude: " + latitude + ", Longitude: " + longitude + ", Accuracy: " + accuracy);
                        }
                    }
                }
            }, Looper.getMainLooper());  // Ensure location updates are handled on the main thread
        }
    }

    private void proceedAfterPermission() {
        //   LocationUserRealTime();
        LocationServiceIsOn();
        checkLocationServiceIsOn();
        getGestures(mapView).addOnMoveListener(onMoveListener);
        // Revert the location puck and settings to the default state after canceling navigation
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.userdeviceicon);
        Bitmap resizeImage = Bitmap.createScaledBitmap(bitmap,65,65, false);
        Drawable drawable = new BitmapDrawable(getResources(),resizeImage);
        locationComponentPlugin = getLocationComponent(mapView);
        //     locationComponentPlugin.setLocationProvider(navigationLocationProviderr);

        if (userPoint == null) {
            locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
            btn_mylocation.setImageResource(R.drawable.baseline_my_location_24);
        } else {
            btn_mylocation.setImageResource(R.drawable.baseline_my_location_24);
            CameraOptions cameraOptions = new CameraOptions.Builder()
                    .zoom(18.0)
                    .center(userPoint)
                    .build();
            getCamera(mapView).easeTo(cameraOptions, new MapAnimationOptions.Builder().duration(1000).build());
        }
    }

    // search the pet
    private void searchList(String text) {
        ArrayList<Marker> searchList = new ArrayList<>();

        for (Marker Marker : petList) {
            if (Marker.getArduinoId().toLowerCase().contains(text.toLowerCase())){
                searchList.add(Marker);
            }else if (Marker.getPetName().toLowerCase().contains(text.toLowerCase())){
                searchList.add(Marker);
            }
        }
        serchResultPetAdapter.searchAdapter(searchList);
    }

    //fetch information of pet from firebase
    private void fetchPetInformation() {
        //showLoadingProcess.show();

        //fetch pet data on firebase
        fetchList = firebaseDatabase.getReference("Pet Information").child(Userid);
        fetchList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    petList.clear();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        marker = snapshot1.getValue(Marker.class);

                        if (marker != null) {
                            //fetch pet data on firebase Pet Information
                            updateOrCreateMarker (marker);
                            petList.add(marker);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoadingProcess.dismiss();
                Toast.makeText(getContext(), "Cannot load your data", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Check if a marker exists and update or create it
    private void updateOrCreateMarker(Marker marker) {
        double longitude = marker.getLongitude();  // Get the new longitude of the marker
        double latitude = marker.getLatitude();  // Get the new latitude of the marker
        String arduinoId = marker.getArduinoId();  // Get the unique Arduino ID of the pet

        // Check if the marker already exists in the markerMap (by Arduino ID)
        existingMarker = markerMap.get(arduinoId);

        if (existingMarker != null) {
// If the marker already exists, delete it
            //         deleteMarker(arduinoId);

            // If the marker already exists
            double existingLongitude = existingMarker.getPoint().longitude();
            double existingLatitude = existingMarker.getPoint().latitude();
            // Check if the new position is different from the existing position
            if (existingLongitude != longitude || existingLatitude != latitude) {
                // Animate the movement of the existing marker to the new position
                animateMarkerMovement(existingMarker, arduinoId, existingLongitude, existingLatitude, longitude, latitude);
            }
            // Optionally, you can also update the marker's other properties (e.g., image) if they change

        } else {
            // If the marker doesn't exist, download the image and add a new marker to the map
            downloadImageAndAddMarker(marker);
        }
    }


    // Method to animate marker movement from old to new position
    private void animateMarkerMovement(PointAnnotation marker, String arduinoiD, double fromlng, double fromlat, double tolng, double tolat) {

        // Create a ValueAnimator that interpolates from 0 to 1 over a set duration
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(5000);  // Set animation duration to 1 second

        // Add an update listener to animate the marker's movement over time
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        removeMarker(fromlng, fromlat);
                    }
                },900);

                float fraction = valueAnimator.getAnimatedFraction();  // Get the current progress of the animation
                newLng = fromlng + (tolng - fromlng) * fraction;  // Calculate the intermediate longitude
                newLat = fromlat + (tolat - fromlat) * fraction;  // Calculate the intermediate latitude

                // Update the marker's position
                marker.setPoint(Point.fromLngLat(newLng, newLat));
                pointAnnotationManager.update(marker);  // Notify the point annotation manager about the update

            }
        });

        Handler handlerr = new Handler();
        handlerr.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Panel_info_navigation) { // if navigation is true
                    if (arduinoiD.equals(arduinoIDArriving)){ // check if arduinoId equals arduinoIdArriving
                        fetchRoute(arduinoIDArriving, petNameArriving, newLng, newLat); // refresh the longitude and latitude of pet
                    }
                } else if (DirectionIsTrue) { // when user click direction
                    if (arduinoiD.equals(arduinoIDArriving)){  // check if arduinoId equals arduinoIdArriving
                        fetchRoute(arduinoIDArriving, petNameArriving, newLng, newLat); // refresh the longitude and latitude of pet
                    }
                }
            }
        },5400);


        // Add a listener to be called when the animation ends
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

            }
        });
        valueAnimator.start();  // Start the animation
    }

  /*  private void deleteMarker(String arduinoId) {
        PointAnnotation existingMarker = markerMap.get(arduinoId);

        if (existingMarker != null) {
            pointAnnotationManager.deleteAll();
            disyplaySafeZone();
            markerMap.remove(arduinoId);
            Log.d("MarkerRemoval", "Marker removed: " + arduinoId);
        } else {
            Log.d("MarkerRemoval", "No marker found with ID: " + arduinoId);
        }
    }*/

    // Method to remove a specific marker by its longitude and latitude
    public void removeMarker(double longitude, double latitude) {
        Iterator<PointAnnotation> iterator = annotations.iterator();
        while (iterator.hasNext()) {
            PointAnnotation annotation = iterator.next();
            // Check if the coordinates match using the correct method
            if (annotation.getPoint().longitude() == longitude &&
                    annotation.getPoint().latitude() == latitude) {
                Log.d("MarkerRemoval", "Marker removed: " + longitude);
                pointAnnotationManager.delete(annotation); // Remove from the map
                iterator.remove(); // Safely remove from the list
                break; // Exit loop after removing the first match
            }
        }
    }



    private void downloadImageAndAddMarker(Marker markerr) {
        double longitude = markerr.getLongitude();  // Get marker's longitude
        double latitude = markerr.getLatitude();  // Get marker's latitude
        String arduinoId = markerr.getArduinoId();  // Get Arduino ID of the pet

        // Proceed to download the image and add the new marker
        if (!String.valueOf(longitude).equals("0.0") && !String.valueOf(latitude).equals("0.0")) {
            // Reference to the pet's image stored in Firebase Storage
            StorageReference profileImageRef = storageReference.child("Pet Image/" + Userid + "/" + arduinoId + "/profile.jpg");

            // Try downloading the image
            profileImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);  // Decode the image bytes to a bitmap
                if (bitmap != null) {  // If image was successfully decoded
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 130, 130, false);
                    Bitmap circularBitmap = createCircularBitmapWithBorder(resizedBitmap, 130);

                    // Create a new PointAnnotationOptions for the marker
                    PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                            .withTextAnchor(TextAnchor.CENTER)
                            .withIconSize(0.7)
                            .withIconImage(circularBitmap)  // Set the custom circular image as the icon
                            .withPoint(Point.fromLngLat(longitude, latitude));  // Set the marker's location

                    // Create and add the new marker to the map
                    PointAnnotation newMarker = pointAnnotationManager.create(pointAnnotationOptions);
                    annotations.add(newMarker); // Store reference
                    markerMap.put(arduinoId, newMarker);  // Store the newly created marker in the markerMap

                    // Set up a click listener for the new marker
                    // Set up click listener for point annotations
                    pointAnnotationManager.addClickListener(new OnPointAnnotationClickListener() {
                        @Override
                        public boolean onAnnotationClick(@NonNull PointAnnotation pointAnnotation) {
                            // check if the clicked annotation is already the current one
                            // fetch again the information of pet to check of geo point are same on longitude of pet and latitude of pet are same on geo point
                            fetchList.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    // Iterate through the snapshot children
                                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                                        //check if longitude and latitude are existing in Pet information
                                        if (snapshot1.child("longitude").exists() && snapshot1.child("latitude").exists()) {
                                            Marker marker1 = snapshot1.getValue(Marker.class);
                                            // Check if the marker's coordinates match the clicked annotation
                                            if (marker1 != null && pointAnnotation.getPoint().longitude() == marker1.getLongitude()
                                                    && pointAnnotation.getPoint().latitude() == marker1.getLatitude()){
                                                // Only update the view if pet direction is not already visible

                                                map_type.setVisibility(View.GONE);
                                                viewInformation.setVisibility(View.GONE);
                                                petInformation.setVisibility(View.GONE);

                                                // Update the view with pet information and direction
                                                pet_direction_and_pet_information(marker1);

                                                // Set pointAnnotation coordinates to 0.0
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            return true;
                        }
                    });
                }
            }).addOnFailureListener(exception -> {
                // Show an error message if image download fails
                Log.e("log","Failed to download image: " + exception.getMessage());
            });
        }
    }

    // Method to remove all markers
    public void removeAllMarkers() {
        for (PointAnnotation annotation : new ArrayList<>(annotations)) {
            pointAnnotationManager.delete(annotation); // Remove from the map
        }
        annotations.clear(); // Clear the list
    }


    // when pointAnnotation click the  will display the information
    private void pet_direction_and_pet_information(Marker markerr) {
        // Inflate custom layout for pet information
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View customInfoView = layoutInflater.inflate(R.layout.custom_info_pet, null);
        View customPetDirection = layoutInflater.inflate(R.layout.custom_pet_direction,null);

        // Find and update the TextViews pet direction
        TextView petNamee = customPetDirection.findViewById(R.id.petnamee);
        TextView petIdd = customPetDirection.findViewById(R.id.petArduinoidd);
        petImageeee = customPetDirection.findViewById(R.id.imagePett);
        distance = customPetDirection.findViewById(R.id.distancee);
        timeConsume = customPetDirection.findViewById(R.id.timeConsumee);

        // Handle the close button click pet direction
        Button closee = customPetDirection.findViewById(R.id.closee);
        Button startNavigation = customPetDirection.findViewById(R.id.StartNavigationn);
        Button viewRoute = customPetDirection.findViewById(R.id.viewRoutee);

        // Find and update the TextViews pet information
        petId = customInfoView.findViewById(R.id.petArduinoid);
        petName = customInfoView.findViewById(R.id.petname);
        petCategory = customInfoView.findViewById(R.id.petcategory);
        petStatus = customInfoView.findViewById(R.id.petstatus);
        TextView batteryPercentage = customInfoView.findViewById(R.id.batteryPercentage);
        petImagee = customInfoView.findViewById(R.id.imagePet);
        ImageView battPercentageImg = customInfoView.findViewById(R.id.battPercentageImg);
        ProgressBar progressBar = customInfoView.findViewById(R.id.progressBar);
        ProgressBar prog = customInfoView.findViewById(R.id.progresssss);

        // Handle the close button click pet information
        close = customInfoView.findViewById(R.id.close);
        Direction = customInfoView.findViewById(R.id.Direction);

        // Load pet image with Glide
        StorageReference petImageRef = storageReference.child("Pet Image/" + Userid + "/" + markerr.getArduinoId() + "/profile.jpg");
        petImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(requireContext())
                    .load(uri)
                    //.override(100, 100) // Resize image
                    .centerCrop()
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(petImagee);
            prog.setVisibility(View.GONE);

            Glide.with(requireContext())
                    .load(uri)
                    // .override(100, 100) // Resize image
                    .centerCrop()
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(petImageeee);
        }).addOnFailureListener(exception -> {
            Toast.makeText(getContext(), "Failed to load image: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Map", "Image load failed: ", exception);
        });
        String arduinoId = markerr.getArduinoId();
        String petNameee = markerr.getPetName();

        // Set pet details in the view pet Information
        petId.setText("Id: " + arduinoId);
        petName.setText("Name: " + petNameee);
        petCategory.setText("Category: " + markerr.getPetCategory());
        petStatus.setText("Status: " + markerr.getStatus());
        if (!batteryPercentage.equals("101")) {
            batteryPercentage.setText(markerr.getBattery() + "%");

            int batteryPertage = Integer.parseInt(markerr.getBattery());

            // battery percentege
            if (batteryPertage > 80 && batteryPertage <= 100) {
                battPercentageImg.setImageResource(R.drawable.tenbattery); // 100 to 81 battery
            }else if (batteryPertage > 60 && batteryPertage <= 80) {
                battPercentageImg.setImageResource(R.drawable.eightbattery); // 80 to 61 battery
            }else if (batteryPertage > 50 && batteryPertage <= 60) {
                battPercentageImg.setImageResource(R.drawable.sixbattery); // 60 to 51 batteryx
            }else if (batteryPertage > 35 && batteryPertage <= 50) {
                battPercentageImg.setImageResource(R.drawable.fivebattery); //50 to 36 battery
            }else if (batteryPertage > 15 && batteryPertage <= 35) {
                battPercentageImg.setImageResource(R.drawable.threebattery); // 35 to 16 battery
            }else if (batteryPertage > 5 && batteryPertage <= 15) {
                battPercentageImg.setImageResource(R.drawable.onebattery); // 15 to 5 battery
            }else if (batteryPertage> 0 && batteryPertage <= 5) {
                battPercentageImg.setImageResource(R.drawable.onefivebattery); // 5 to 1 battery
            }
            else {
                battPercentageImg.setImageResource(R.drawable.zerobattery); //zero battery
            }
        }

        // Set pet details in the view pet Direction
        petIdd.setText("Id: " + markerr.getArduinoId());
        petNamee.setText("Name: " + markerr.getPetName());

        petPoint = Point.fromLngLat(markerr.getLongitude(), markerr.getLatitude());

        petLongitudeChange = markerr.getLongitude();
        petLatitudeChange = markerr.getLatitude();

        CameraOptions cameraOptions1 = new CameraOptions.Builder().center(petPoint).zoom(17.0).build();

        getCamera(mapView).easeTo(cameraOptions1, new MapAnimationOptions.Builder().duration(1000).build());

        // Set the updated view to the FrameLayout on pet direction
        //     pet_direction.removeAllViews(); // Clear existing views
        pet_direction.addView(customInfoView); // Add new view
        pet_direction.setVisibility(View.VISIBLE);



        // Set the updated view to the FrameLayout on pet information
        petInformation.removeAllViews();
        petInformation.addView(customPetDirection);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pet_direction.setVisibility(View.GONE);
                ExitNavigationIcon();
                petNameArriving = "";
                arduinoIDArriving = "";
            }
        });

        closee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                petInformation.setVisibility(View.GONE);
                clearRouteLine();
                petNameArriving = "";
                arduinoIDArriving = "";
                ExitNavigationIcon ();
                searchPet.setVisibility(View.VISIBLE);
                //remove the padding and back the center when close the pet information
                CameraOptions cameraOptions = new CameraOptions.Builder()
                        .padding(new EdgeInsets(0.0, 0.0,0.0, 0.0))
                        .build();
                getCamera(mapView).easeTo(cameraOptions, new MapAnimationOptions.Builder().build());
            }
        });

        Direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 34) {
                    // check if permission has granted
                    if (ActivityCompat.checkSelfPermission(requireActivity(),Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(requireActivity(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(requireActivity(),Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(requireActivity(),Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        // Permissions granted, check if location services are enabled
                        LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            turnOnAfterPermissionWhenIsOff = true;
                            DirectionIsTrue = true;
                            progressBar.setVisibility(View.VISIBLE);
                            Direction.setText("");
                            Direction.setEnabled(false);
                            LocationUserRealTime();
                            //               proceedAfterPermission();
                            locationEngineUser = LocationEngineProvider.getBestLocationEngine(requireActivity());
                            // center the camera on both the user's location and the pet's location so that both points are visible on the map simultaneously.
                            locationEngineUser.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
                                @Override
                                public void onSuccess(LocationEngineResult result) {
                                    Handler handler = new Handler();
                                    Runnable runnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            if (userPoint != null) {
                                                // If userPoint is not null, stop checking
                                                handler.removeCallbacks(this);

                                                if (mapboxNavigation == null) {
                                                    initializeMapboxNavigation();
                                                }else {
                                                    fetchRoute(arduinoId, petNameee,petLongitudeChange, petLatitudeChange);
                                                }


                                                // Create a bounding box with the user's location and the pet's location
                                                CoordinateBounds bounds = new CoordinateBounds(userPoint, petPoint);

                                                // Define the padding using EdgeInsets
                                                EdgeInsets padding = new EdgeInsets(250, 400, 900, 300); // Top, Left, Bottom, Right padding

                                                // Set up the camera options to fit the bounding box with padding
                                                CameraOptions cameraOptions1 = mapView.getMapboxMap().cameraForCoordinateBounds(bounds, padding, null, null);

                                                // Move the camera to fit the bounding box
                                                getCamera(mapView).easeTo(cameraOptions1, new MapAnimationOptions.Builder()
                                                        .duration(1000) // Adjust the duration of the animation as needed
                                                        .build());

                                                Handler handler1 = new Handler();
                                                Runnable runnable1 = new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (routeLineView != null) {
                                                            handler1.removeCallbacks(this);
                                                            displayNavigation ();
                                                            searchPet.setVisibility(View.GONE);
                                                            pet_direction.removeAllViews(); // Clear existing views
                                                            pet_direction.setVisibility(View.GONE);
                                                            petInformation.setVisibility(View.VISIBLE);
                                                            progressBar.setVisibility(View.GONE);
                                                            Direction.setEnabled(true);
                                                            Direction.setText("Direction");

                                                        }else {
                                                            handler1.postDelayed(this,500);
                                                        }
                                                    }
                                                };
                                                handler1.post(runnable1);

                                            }else {
                                                // If userPoint is null, check again after 1 second
                                                handler.postDelayed(this,1000);
                                            }
                                        }
                                    };
                                    handler.post(runnable);
                                }

                                @Override
                                public void onFailure(@NonNull Exception exception) {

                                }
                            });
                        }else {
                            //when location service are not enabled will be set location service dialog
                            showLocationServicesAlertDialog();
                        }
                    }else {
                        // when permission are no enabled will request permission
                        String [] permissionRequest = {
                                Manifest.permission.FOREGROUND_SERVICE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.POST_NOTIFICATIONS
                        };
                        ActivityCompat.requestPermissions(requireActivity(),permissionRequest,PERMISSION_REQUEST_CODE);
                    }
                } else {
                    // check if permission has granted
                    if (ActivityCompat.checkSelfPermission(requireActivity(),Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(requireActivity(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // Permissions granted, check if location services are enabled
                        LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            turnOnAfterPermissionWhenIsOff = true;
                            DirectionIsTrue = true;
                            progressBar.setVisibility(View.VISIBLE);
                            Direction.setText("");
                            Direction.setEnabled(false);
                            LocationUserRealTime();
                            //               proceedAfterPermission();
                            locationEngineUser = LocationEngineProvider.getBestLocationEngine(requireActivity());
                            // center the camera on both the user's location and the pet's location so that both points are visible on the map simultaneously.
                            locationEngineUser.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
                                @Override
                                public void onSuccess(LocationEngineResult result) {
                                    Handler handler = new Handler();
                                    Runnable runnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            if (userPoint != null) {
                                                // If userPoint is not null, stop checking
                                                handler.removeCallbacks(this);

                                                if (mapboxNavigation == null) {
                                                    initializeMapboxNavigation();
                                                }else {
                                                    fetchRoute(arduinoId, petNameee,petLongitudeChange, petLatitudeChange);
                                                }

                                                // Create a bounding box with the user's location and the pet's location
                                                CoordinateBounds bounds = new CoordinateBounds(userPoint, petPoint);

                                                // Define the padding using EdgeInsets
                                                EdgeInsets padding = new EdgeInsets(250, 400, 850, 300); // Top, Left, Bottom, Right padding

                                                // Set up the camera options to fit the bounding box with padding
                                                CameraOptions cameraOptions1 = mapView.getMapboxMap().cameraForCoordinateBounds(bounds, padding, null, null);

                                                // Move the camera to fit the bounding box
                                                getCamera(mapView).easeTo(cameraOptions1, new MapAnimationOptions.Builder()
                                                        .duration(1000) // Adjust the duration of the animation as needed
                                                        .build());

                                                Handler handler1 = new Handler();
                                                Runnable runnable1 = new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (routeLineView != null) {
                                                            handler1.removeCallbacks(this);
                                                            displayNavigation ();
                                                            searchPet.setVisibility(View.GONE);
                                                            pet_direction.removeAllViews(); // Clear existing views
                                                            pet_direction.setVisibility(View.GONE);
                                                            petInformation.setVisibility(View.VISIBLE);
                                                            progressBar.setVisibility(View.GONE);
                                                            Direction.setEnabled(true);
                                                            Direction.setText("Direction");

                                                        }else {
                                                            handler1.postDelayed(this,500);
                                                        }
                                                    }
                                                };
                                                handler1.post(runnable1);

                                            }else {
                                                // If userPoint is null, check again after 1 second
                                                handler.postDelayed(this,1000);
                                            }
                                        }
                                    };
                                    handler.post(runnable);
                                }

                                @Override
                                public void onFailure(@NonNull Exception exception) {

                                }
                            });
                        }else {
                            //when location service are not enabled will be set location service dialog
                            showLocationServicesAlertDialog();
                        }
                    }else {
                        // when permission are no enabled will request permission
                        String [] permissionRequest = {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        };
                        ActivityCompat.requestPermissions(requireActivity(),permissionRequest,PERMISSION_REQUEST_CODE);
                    }
                }
            }
        });

        startNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setCancelable(false);
                View view1 = getLayoutInflater().inflate(R.layout.custom_loading_process,null);
                alert.setView(view1);

                AlertDialog aler = alert.create();

                aler.show();

                if (userPoint != null) {
                    mapboxNavigation.registerLocationObserver(locationObserver);
                    //    fetchRoute(arduinoId, petNameee,petLongitudeChange, petLatitudeChange);
                    // location service enabled, proceed with your logic
                    Panel_info_navigation = true;
                    updataCameraToNavView = true;
                    focusLocation = true;
                    petInformation.setVisibility(View.GONE);
                    getGestures(mapView).addOnMoveListener(onMoveListener);

                    Handler handler1 = new Handler();
                    handler1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            searchPet.setVisibility(View.GONE);
                            btn_map_type.setVisibility(View.GONE);
                            btn_safe_zone.setVisibility(View.GONE);
                            btn_mylocation.setVisibility(View.GONE);
                            // hide tablayout from home.class
                            hideTablayout();
                            aler.dismiss();
                        }
                    },1500);

                    // Revert the location puck and settings to the default state after canceling navigation
                    locationComponentPlugin.setLocationProvider(navigationLocationProvider);

                    locationComponentPlugin.updateSettings(settings -> {
                        // Load the bitmap from resources
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_navigation_puck_icon);

                        // Resize the bitmap
                        int width = 150;  // Set the desired width
                        int height = 150; // Set the desired height
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

                        // Convert the resized bitmap to Drawable
                        Drawable drawable = new BitmapDrawable(getResources(), resizedBitmap);

                        // Set the location puck to a navigation arrow
                        settings.setLocationPuck(
                                new LocationPuck2D(
                                        null, // Use default GPS icon
                                        drawable, // Resized navigation arrow icon as a Drawable
                                        null // Optional shadow icon
                                )
                        );
                        settings.setEnabled(true);
                        settings.setPulsingEnabled(true);
                        return null;
                    });
                }
            }
        });

        viewRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                petInformation.setVisibility(View.GONE);
                viewInformation.setVisibility(View.VISIBLE);
                CameraOptions cameraOptions2 = new CameraOptions.Builder().center(petPoint).zoom(18.0).build();
                getCamera(mapView).easeTo(cameraOptions2,new MapAnimationOptions.Builder().duration(1000).build());
            }
        });
    }


    // Method to create a circular bitmap with a border
    private Bitmap createCircularBitmapWithBorder (Bitmap bitmap, int diameter) {
        Bitmap circularBitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circularBitmap);

        // Draw a circle background
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(diameter / 2f, diameter / 2f, diameter / 2f, paint);

        // Draw the orginal bitmap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, new Rect(0, 0, diameter, diameter), paint);

        // Draw a border around the circular bitmap
        paint.setXfermode(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4); // Border width
        paint.setColor(Color.BLACK); // border black
        canvas.drawCircle(diameter / 2f, diameter / 2f , diameter /2f - paint.getStrokeWidth(), paint);

        return circularBitmap;
    }

    // Method to hide the TabLayout from home.java
    private void hideTablayout () {
        if (getActivity() instanceof Home) {
            ((Home) getActivity()).hideTabLayout();
        }
    }

    // Method to show the TabLayout
    private void showTabLayout() {
        if (getActivity() instanceof Home) {
            ((Home) getActivity()).showTabLayout();
        }
    }

    private void displayNavigation () {
        checkLocationServiceIsOn();
    }

    private void displayFetchWhenTrue () {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Clear all markers from the map when leaving the activity
                if (annotations.isEmpty()) { // Check if annotations list is empty
                    fetchPetInformation();  // Fetch pet information if no markers are present
                }else {
                    handler.removeCallbacks(this); // Stop the runnable if annotations are not empty
                }
                handler.postDelayed(this,500); // Repeat every 5 seconds
            }
        };
        handler.post(runnable);
    }

    private void ExitNavigationIcon () {
        checkLocationServiceIsOn ();
    }

    private void clearRouteLine() {
        // Unregister route observers before clearing the route line
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver);
        mapboxNavigation.unregisterRoutesObserver(routesObserver);
        mapboxNavigation.stopTripSession();

        arduinoIDArriving = "";
        petNameArriving = "";

        routeLineApi.clearRouteLine(new MapboxNavigationConsumer<Expected<RouteLineError, RouteLineClearValue>>() {
            @Override
            public void accept(Expected<RouteLineError, RouteLineClearValue> result) {
                mapView.getMapboxMap().getStyle(new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        // Use the loaded style to clear the route line
                        routeLineView.renderClearRouteLineValue(style, result);
                    }
                });
            }
        });

        // Check if a route request was made and cancel it
        if (routeRequestId != -1) {
            mapboxNavigation.cancelRouteRequest(routeRequestId);  // Pass the request ID to cancel it
            routeRequestId = -1;  // Reset the request ID after canceling
        }
        // Optionally reset the map and UI elements
        mapboxNavigation.setNavigationRoutes(Collections.emptyList());  // Clear any routes

    }

    @SuppressLint("MissingPermission")
    private void fetchRoute(String arduinoId, String petName, double petLongitudeChange, double petLatitudeChange) {
        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver);
        mapboxNavigation.registerRoutesObserver(routesObserver);

        arduinoIDArriving = arduinoId;
        petNameArriving = petName;

        Point pet = Point.fromLngLat(petLongitudeChange,petLatitudeChange);
        petPoint = Point.fromLngLat(petLongitudeChange, petLatitudeChange);
        routeRequestId = 1234;

        // Start the trip session to get route progress updates without notifications
        mapboxNavigation.startTripSession();

        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext());
        locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                Location location = result.getLastLocation();

                RouteOptions.Builder builder = RouteOptions.builder();
                Point userPoint = Point.fromLngLat(Objects.requireNonNull(location).getLongitude(), location.getLatitude());
                builder.coordinatesList(Arrays.asList(userPoint, pet));
                builder.alternatives(true);
                builder.profile(DirectionsCriteria.PROFILE_DRIVING);
                builder.bearingsList(Arrays.asList(Bearing.builder().angle(location.getBearing()).degrees(45.0).build(), null));
                applyDefaultNavigationOptions(builder);

                mapboxNavigation.requestRoutes(builder.build(), new NavigationRouterCallback() {
                    @Override
                    public void onRoutesReady(@NonNull List<NavigationRoute> list, @NonNull RouterOrigin routerOrigin) {
                        mapboxNavigation.setNavigationRoutes(list);
                    }

                    @Override
                    public void onFailure(@NonNull List<RouterFailure> list, @NonNull RouteOptions routeOptions) {
                        Toast.makeText(getContext(), "Route request failed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCanceled(@NonNull RouteOptions routeOptions, @NonNull RouterOrigin routerOrigin) {
                        // Handle cancellation if necessary
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("FetchRoute", "Location Engine failure: " + exception.getMessage());
            }
        });
    }

    private void saveCameraState() {
        // Get the MapboxMap instance from the map view
        MapboxMap mapboxMap = mapView.getMapboxMap();
        // Retrieve the current camera state
        CameraState cameraState = mapboxMap.getCameraState(); // Get the current camera state

        // Extract camera parameters from the current camera state
        double centerLongitude = cameraState.getCenter().longitude();
        double centerLatitude = cameraState.getCenter().latitude();
        float zoom = (float) cameraState.getZoom();
        float bearing = (float) cameraState.getBearing();
        // float pitch = (float) cameraState.getPitch();
        float pitch = (float) 0.0;
        // Retrieve SharedPreferences for saving camera state
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Save camera parameters in SharedPreferences
        editor.putFloat(KEY_LONGITUDE, (float) centerLongitude); // Save longitude
        editor.putFloat(KEY_LATITUDE, (float) centerLatitude); // Save latitude
        editor.putFloat(KEY_ZOOM, zoom); // Save zoom level
        editor.putFloat(KEY_BEARING, bearing); // Save bearing
        editor.putFloat(KEY_PITCH, pitch); // Save pitch
        editor.apply(); // Apply changes to SharedPreferences

        // Show a toast message indicating that the camera state has been saved
        //Toast.makeText(getContext(), "Camera state saved", Toast.LENGTH_SHORT).show();
    }

    private void restoreCameraState() {
        if (!updataCameraToNavView) {
            // Retrieve SharedPreferences for loading camera state
            SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            // Load camera parameters from SharedPreferences, with default values
            double centerLongitude = prefs.getFloat(KEY_LONGITUDE, 0.0F); // Default value if not found
            double centerLatitude = prefs.getFloat(KEY_LATITUDE, 0.0F); // Default value if not found
            float zoom = prefs.getFloat(KEY_ZOOM, 10.0f); // Default zoom level
            float bearing = prefs.getFloat(KEY_BEARING, 0.0f); // Default bearing
            float pitch = prefs.getFloat(KEY_PITCH, 0.0f); // Default pitch

            // Get the MapboxMap instance from the map view
            MapboxMap mapboxMap = mapView.getMapboxMap();
            // Build CameraOptions with the loaded camera parameters
            CameraOptions cameraOptions = new CameraOptions.Builder()
                    .center(Point.fromLngLat(centerLongitude, centerLatitude)) // Set camera center
                    .zoom((double) zoom)  // Convert float to Double for zoom level
                    .bearing((double) bearing)  // Convert float to Double for bearing
                    .pitch((double) pitch) // Convert float to Double for pitch
                    .build();

            // Apply the camera options to the MapboxMap
            mapboxMap.setCamera(cameraOptions);

            // Show a toast message indicating that the camera state has been restored
            // Toast.makeText(getContext(), "Camera state restored", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkInternet () {
        Context context = getContext();

        if (context != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            context.registerReceiver (new ConnectionReceiver(), intentFilter);
            ConnectivityManager connectionManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
            boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            showResultInternet (isConnected);
        }
    }

    private void showResultInternet (boolean isConnected) {
        if (showInternet != null) {
            if(isConnected) {
                haveInternet = true;
                showInternet.dismiss();
            }else {
                showInternet.show();
                haveInternet = false;
            }
        }
    }

    @Override
    public void onNetworkChange(boolean isConnected) {
        showResultInternet(isConnected);
    }

    @Override
    public void onResume() {
        super.onResume();
        restoreCameraState();

        if (removeMarker) {
            if (pointAnnotationManager != null) {
                removeAllMarkers();
                markerMap.clear();  // Clear all entries from markerMap
                removeMarker = false;
                pointAnnotationManager.deleteAll();
                fetchPetInformation();
                disyplaySafeZone();
            }
        }

        // Clear all markers from the map when leaving the activity
        /* if (removeMarker) {
            fetchPetInformation();
            checkLocationServiceIsOn ();
            removeMarker = false;
            //   removeAllMarkers();
            //       markerMap.clear();  // Clear all entries from markerMap
        } */

    }

    @Override
    public void onStop() {
        super.onStop();
        // Logic to handle stopping of the fragment
        // Stop receiving location updates when the activity is stopped
        // fusedLocationClient.removeLocationUpdates(new LocationCallback());
        // Clear all markers from the map when leaving the activity
        // Clear all markers from the map when leaving the activity
        if (removeMarker) {
            if (pointAnnotationManager != null) {
                removeAllMarkers();
                markerMap.clear();  // Clear all entries from markerMap
                removeMarker = false;
                pointAnnotationManager.deleteAll();
                fetchPetInformation();
                disyplaySafeZone();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveCameraState();

        // Clear all markers from the map when leaving the activity

        if (removeMarker) {
            if (pointAnnotationManager != null) {
                removeAllMarkers();
                markerMap.clear();  // Clear all entries from markerMap
                pointAnnotationManager.deleteAll();
                fetchPetInformation();
                fetchZone = true;
                disyplaySafeZone();
                removeMarker = false;
            }
        }
        // Clear all markers from the map when leaving the activity
   /*     if (pointAnnotationManager != null) {
            pointAnnotationManager.deleteAll();  // Remove all markers from the map
        }*/


    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        removeAllMarkers();
        markerMap.clear();  // Clear all entries from markerMap
        removeMarker = true;

        if (pointAnnotationManager != null) {
            pointAnnotationManager.deleteAll();
        }

        if (mapboxNavigation != null) {
            mapboxNavigation.onDestroy();
            mapboxNavigation.unregisterRoutesObserver(routesObserver);
            mapboxNavigation.unregisterLocationObserver(locationObserver);
            mapboxNavigation = null;
        }
        NavigationManager.destroyInstance(); // Ensure the MapboxNavigation instance is destroyed
    }

}

        /*
        * this code is use for delete the previous camera view
        * SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.remove(KEY_LONGITUDE);
    editor.remove(KEY_LATITUDE);
    editor.remove(KEY_ZOOM);
    editor.remove(KEY_BEARING);
    editor.remove(KEY_PITCH);
    editor.apply();
    *
    *
    *
    *     private void permissionAccessLocation () {
        // After two denials, prompt the user to go to settings
        new AlertDialog.Builder(getContext())
                .setTitle("Location Permission Needed")
                .setMessage("This app needs location permissions to track your location accurately. Please grant the necessary permissions.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Open the app settings if the user denied with "Don't ask again"
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                })
                .create()
                .show();
    }
    *
    * mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS);
    *    private void clearRouteLine () {
        // unregister the route observer before cleaning the line
        // Unregister route observers before clearing the route line
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver);
        mapboxNavigation.unregisterRoutesObserver(routesObserver);

        routeLineApi.clearRouteLine(new MapboxNavigationConsumer<Expected<RouteLineError, RouteLineClearValue>>() {
            @Override
            public void accept(Expected<RouteLineError, RouteLineClearValue> routeLineErrorRouteLineClearValueExpected) {
                Style style = mapView.getMapboxMap().getStyle();
                if (!updataCameraToNavView) {
                    mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS);
                    routeLineView.renderClearRouteLineValue(style, routeLineErrorRouteLineClearValueExpected);
                }
                else if (style != null) {
                    mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style loadedStyle) {
                            // Use the loaded style to clear the route line
                            routeLineView.renderClearRouteLineValue(loadedStyle, routeLineErrorRouteLineClearValueExpected);
                        }
                    });
                }
            }
        });
    }
        * */