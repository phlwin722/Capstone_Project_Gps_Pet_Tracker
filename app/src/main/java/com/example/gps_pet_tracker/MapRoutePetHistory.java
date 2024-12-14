package com.example.gps_pet_tracker;

// Import necessary classes and libraries

import static com.mapbox.maps.plugin.animation.CameraAnimationsUtils.getCamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MapRoutePetHistory extends AppCompatActivity {

    // Declare member variables
    MapView mapView;
    HistoryFetch pet;
    FirebaseStorage firebaseStorage;
    FirebaseDatabase firebaseDatabase;
    LinearLayout viewInformation, list_history;
    MapRoutePetAdapter mapRoutePetAdapter;
    FirebaseAuth firebaseAuth;
    String userId;
    Point point1, point2;
    Handler handler;
    List<Point> points = new ArrayList<>();
    TextView textView, textHistoryPet;
    RecyclerView recyclerview;
    List<HistoryDisplayOnFirebase> petHistory;
    FloatingActionButton loadAgain;
    PointAnnotation pointAnnotation;
    PointAnnotationManager pointAnnotationManager;
    ImageButton deleteButton;
    PointAnnotationOptions pointAnnotationOptions;
    LinearLayout have_data_found;
    FrameLayout no_data_found;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the activity_map_route_pet_history layout
        setContentView(R.layout.activity_map_route_pet_history);

        // Initialize Firebase instances
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        // Get the currently signed-in user's ID
        userId = firebaseAuth.getCurrentUser().getUid();

        // Retrieve the 'pet' object passed from the previous activity
        pet = (HistoryFetch) getIntent().getSerializableExtra("pet");

        // Find the MapView component from the layout
        mapView = findViewById(R.id.mapVieww);
        viewInformation = findViewById(R.id.viewInformation);
        //loadAgain = findViewById(R.id.loadAgain);
        textView = findViewById(R.id.textView);
        list_history = findViewById(R.id.list_history);
        textHistoryPet = findViewById(R.id.textHistoryPet);
        recyclerview = findViewById(R.id.recyclerview);
        deleteButton = findViewById(R.id.deleteButton);
        no_data_found = findViewById(R.id.no_data_found);
        have_data_found = findViewById(R.id.have_data_found);

        deleteButton.setEnabled(false);
        have_data_found.setVisibility(View.GONE);
        no_data_found.setVisibility(View.VISIBLE);

        // Initialize pet list and adapter
        petHistory = new ArrayList<>();
        // Initialize RecyclerView and set its layout manager
        recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        // when user click each of recycler view
        mapRoutePetAdapter = new MapRoutePetAdapter(petHistory, new MapRoutePetAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(HistoryDisplayOnFirebase historyOfPet) {

                if (pointAnnotationManager != null) {
                    // Get a reference to the pet's profile image in Firebase Storage
                    StorageReference Image = firebaseStorage.getReference("Pet Image")
                            .child(userId)
                            .child(historyOfPet.getArduinoId())
                            .child("profile.jpg");

                    Image.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                            if (bitmap != null) {
                                // Resize the bitmap
                                Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap,130, 130, false);

                                if (resizeBitmap != null && resizeBitmap.getWidth() > 0 && resizeBitmap.getHeight() > 0) {
                                    // Delete previous pet Image on map
                                    pointAnnotationManager.deleteAll();

                                    // Create a circular bitmap with border
                                    Bitmap circularBitmap = createCircularBitmapWithBorder(resizeBitmap,140);

                                    String iconImage = "icon-" + historyOfPet.getArduinoId();

                                    mapView.getMapboxMap().getStyle(new Style.OnStyleLoaded() {
                                        @Override
                                        public void onStyleLoaded(@NonNull Style style) {
                                            style.addImage(iconImage, circularBitmap);
                                            // Create and add point Marker to the map
                                            pointAnnotationOptions = new PointAnnotationOptions()
                                                    .withTextAnchor(TextAnchor.CENTER)
                                                    .withIconSize(0.7)
                                                    .withIconImage(circularBitmap)
                                                    .withPoint(Point.fromLngLat(historyOfPet.getLongitude(),historyOfPet.getLatitude()));

                                            // Create a PointAnnotation with the options and add it to the map
                                            pointAnnotationManager.create(pointAnnotationOptions);

                                            Point petHistoryLocation = Point.fromLngLat(historyOfPet.getLongitude(),historyOfPet.getLatitude());
                                            CameraOptions cameraOptions = new CameraOptions.Builder()
                                                    .center(petHistoryLocation)
                                                    .zoom(18.0)
                                                    .build();
                                            getCamera(mapView).easeTo(cameraOptions, new MapAnimationOptions.Builder().duration(1000).build());
                                        }
                                    });
                                }
                            }
                        }
                    });

                }
                }
        });

        // Set adapter to RecyclerView
        recyclerview.setAdapter(mapRoutePetAdapter);

        // Load the Mapbox map style and configure map settings
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                // Set the map's camera to center on the Philippines with a specific zoom level
                Point centerPhilippines = Point.fromLngLat(121.7740, 12.8797);
                CameraOptions cameraOptions = new CameraOptions.Builder()
                        .center(centerPhilippines) // Set the map center
                        .zoom(5.0) // Set the zoom level
                        .build();

                // Fetch the pet image and historical data
                fetchPetImage();
                fetchHistoryData();

                // Apply the camera options to the map
                mapView.getMapboxMap().setCamera(cameraOptions);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapRoutePetHistory.this);
                View view1 = getLayoutInflater().inflate(R.layout.custom_delete_history_alert,null);
                builder.setView(view1);
                AlertDialog alertDialog = builder.create();

                alertDialog.show();

                Button btn_logout_confirm = view1.findViewById(R.id.btn_logout_confirm);
                Button btn_logout_cancel = view1.findViewById(R.id.btn_logout_cancel);

                btn_logout_confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                        DatabaseReference databaseReference = firebaseDatabase.getReference("HistoryOfPet")
                                .child(pet.getArduinoId());

                        databaseReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Intent i = new Intent(MapRoutePetHistory.this,Sucessfull_delete_information.class);
                                // Put the selected 'pet' object into the Intent as extra data
                                // Cast 'pet' to Serializable to make it transferable between activities
                                i.putExtra("pet", (Serializable) pet);
                                startActivity(i);
                            }
                        });
                    }
                });

                btn_logout_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

            }
        });

       /* loadAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraOptions cameraOptions = new CameraOptions.Builder()
                        .padding(new EdgeInsets(0,0,0,0))
                        .center(point2)
                        .build();
                getCamera(mapView).easeTo(cameraOptions, new MapAnimationOptions.Builder().build());
                
                loadAgain.setEnabled(false);
                list_history.setVisibility(View.GONE);
                viewInformation.setVisibility(View.VISIBLE);
                viewInformation.setEnabled(false);
                startAnimation(pointAnnotationManager, pointAnnotation);
                textView.setTextColor(Color.parseColor("#878080"));
            }
        }); */

        viewInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list_history.setVisibility(View.VISIBLE);
                viewInformation.setVisibility(View.GONE);

                //Set padding when user click history information
                CameraOptions cameraOptions = new CameraOptions.Builder()
                        .padding(new EdgeInsets(0,0,650,0))
                        .build();
                getCamera(mapView).easeTo(cameraOptions,new MapAnimationOptions.Builder().build());
            }
        });

        textHistoryPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list_history.setVisibility(View.GONE);
                viewInformation.setVisibility(View.VISIBLE);

                CameraOptions cameraOptions = new CameraOptions.Builder()
                        .padding(new EdgeInsets(0,0,0,0))
                        .build();
                getCamera(mapView).easeTo(cameraOptions, new MapAnimationOptions.Builder().build());
            }
        });
    }

    public void fetchHistoryData() {
        // Get a reference to the pet's history data in the Firebase Realtime Database
        DatabaseReference FetchHistory = firebaseDatabase.getReference("HistoryOfPet")
                .child(pet.getArduinoId());
        FetchHistory.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    petHistory.clear(); // Clear the list before adding new items
                    // Iterate through the children of the snapshot
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        // Retrieve each child's data as a HistoryOfPetInsert object
                        HistoryDisplayOnFirebase historyOfPetInsert = snapshot1.getValue(HistoryDisplayOnFirebase.class);
                        Log.d("HistoryData", "Fetched entry: " + historyOfPetInsert); // Log fetched entry
                        if (historyOfPetInsert != null) {
                            deleteButton.setEnabled(true);
                            no_data_found.setVisibility(View.GONE);
                            have_data_found.setVisibility(View.VISIBLE);
                            petHistory.add(historyOfPetInsert);

                            // Create a Point object from the retrieved longitude and latitude
                            point1 = Point.fromLngLat(historyOfPetInsert.getLongitude(), historyOfPetInsert.getLatitude());
                            points.add(point1); // Add the point to the list of points
                            // Check if the list is not empty before accessing the first element
                            if (!points.isEmpty()) {
                                // Retrieve the first Point in the list
                                point2 = points.get(0);
                            }
                        }else {

                        }
                    }

                    // Sort the list in descending order based on the timestamp
                    Collections.sort(petHistory, new Comparator<HistoryDisplayOnFirebase>() {
                        @Override
                        public int compare(HistoryDisplayOnFirebase n1, HistoryDisplayOnFirebase n2) {
                            return Long.compare(n2.getTimestamp(),n1.getTimestamp());
                        }
                    });

                    Log.d("HistoryData", "Total entries fetched: " + petHistory.size()); // Log total entries
                    mapRoutePetAdapter.notifyDataSetChanged(); // Notify adapter of data change
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database read errors
            }
        });
    }

    public void fetchPetImage() {
        // Get a reference to the pet's profile image in Firebase Storage
        StorageReference storageReferenceImagePet = firebaseStorage.getReference("Pet Image")
                .child(userId)
                .child(pet.getArduinoId())
                .child("profile.jpg");

        // Retrieve the image as a byte array
        storageReferenceImagePet.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Decode the byte array into a Bitmap object
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    // Resize the bitmap
                    Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, 130, 130, false);
                    if (resizeBitmap != null && resizeBitmap.getWidth() > 0 && resizeBitmap.getHeight() > 0) {
                        // Create a circular bitmap with a border
                        Bitmap circulatBitmap = createCircularBitmapWithBorder(resizeBitmap, 140);

                        // Define a unique ID for the icon image
                        String iconImageId = "icon-" + pet.getArduinoId();
                        mapView.getMapboxMap().getStyle(new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {
                                try {
                                    // Get the annotation plugin and create a PointAnnotationManager
                                    AnnotationPlugin annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
                                    pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, new AnnotationConfig());
                                    if (pointAnnotationManager != null) {
                                        pointAnnotationManager.deleteAll(); // Clear existing annotations
                                    }

                                    // Add the circular bitmap to the map as an image
                                    style.addImage(iconImageId, circulatBitmap);
                                    pointAnnotationOptions = new PointAnnotationOptions()
                                            .withTextAnchor(TextAnchor.CENTER)
                                            .withIconSize(0.7)
                                            .withIconImage(iconImageId) // Use iconImageId instead of Bitmap
                                            .withPoint(point2);

                                    // Create a PointAnnotation with the options and add it to the map
                                    pointAnnotation = pointAnnotationManager.create(pointAnnotationOptions);

                                    // Initialize a handler for delayed tasks
                                    handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Animate the camera to focus on the pet's location
                                            CameraOptions cameraOptions = new CameraOptions.Builder()
                                                    .center(point2)
                                                    .zoom(18.0)
                                                    .build();
                                            getCamera(mapView).easeTo(cameraOptions, new MapAnimationOptions.Builder().build());

                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    // Start the animation of the point annotation
                                             //       startAnimation(pointAnnotationManager, pointAnnotation);
                                                }
                                            },2000);
                                        }
                                    }, 1000); // Delay before starting animation
                                } catch (Exception e) {
                                    // Handle any exceptions that occur during the style loading
                                }
                            }
                        });
                    }
                }
            }
        }).addOnFailureListener(exception -> {
            // Show a toast message if image download fails
            Toast.makeText(MapRoutePetHistory.this, "Failed to download image: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // Method to create a circular bitmap with a border
    private Bitmap createCircularBitmapWithBorder(Bitmap bitmap, int diameter) {
        Bitmap circularBitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circularBitmap);

        // Draw a circle background
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(diameter / 2f, diameter / 2f, diameter / 2f - paint.getStrokeWidth(), paint);

        // Draw the original bitmap inside the circle
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, new Rect(0, 0, diameter, diameter), paint);

        // Draw a border around the circular bitmap
        paint.setXfermode(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4); // Border width
        paint.setColor(Color.BLACK);
        canvas.drawCircle(diameter / 2f, diameter / 2f, diameter / 2f - paint.getStrokeWidth(), paint);

        return circularBitmap;
    }

    // Method to start the animation of the point annotation along the points
    private void startAnimation(PointAnnotationManager pointAnnotationManager, PointAnnotation pointAnnotation) {
        // Call animateImage to animate the image along the list of points
        animateImage(pointAnnotationManager, pointAnnotation, points, 0);
    }

    // Method to animate the image along a list of points
    private void animateImage(PointAnnotationManager pointAnnotationManager, PointAnnotation pointAnnotation, List<Point> points, int index) {
        if (index >= points.size() - 1) {
            // Zoom in on the pet's final location after reaching the last point
            CameraOptions cameraOptions = new CameraOptions.Builder()
                    .center(points.get(points.size() - 1))
                    .zoom(18.0)
                    .build();
            getCamera(mapView).easeTo(cameraOptions, new MapAnimationOptions.Builder().duration(1000).build());
            textView.setTextColor(Color.parseColor("#000000"));
            viewInformation.setEnabled(true);
            loadAgain.setEnabled(true);
            textHistoryPet.setEnabled(true);
            return;
        }

        Point startPoint = points.get(index);
        Point endPoint = points.get(index + 1);

        // Animate smoothly by interpolating positions between startPoint and endPoint
        long duration = 4000; // Duration of the animation
        int frames = 100; // Number of frames for smooth animation
        long frameDuration = duration / frames;

        handler.post(new Runnable() {
            int currentFrame = 0;

            @Override
            public void run() {
                if (currentFrame <= frames) {
                    // Calculate the fraction of progress between startPoint and endPoint
                    float fraction = (float) currentFrame / frames;

                    // Interpolate latitude and longitude between startPoint and endPoint
                    double interpolatedLat = startPoint.latitude() + fraction * (endPoint.latitude() - startPoint.latitude());
                    double interpolatedLng = startPoint.longitude() + fraction * (endPoint.longitude() - startPoint.longitude());

                    // Update the annotation's position
                    pointAnnotation.setPoint(Point.fromLngLat(interpolatedLng, interpolatedLat));
                    pointAnnotationManager.update(pointAnnotation);

                    // Only update the camera on the first frame or the last frame of the animation
                        CameraOptions cameraOptions = new CameraOptions.Builder()
                                .center(Point.fromLngLat(interpolatedLng, interpolatedLat))
                                .zoom(18.0)
                                .build();
                        getCamera(mapView).easeTo(cameraOptions, new MapAnimationOptions.Builder().duration(100).build());

                    // Increment frame and schedule the next update
                    currentFrame++;
                    handler.postDelayed(this, frameDuration);
                } else {
                    // Move to the next segment when the current one finishes
                    animateImage(pointAnnotationManager, pointAnnotation, points, index + 1);
                }
            }
        });
    }

    @Override
    public void onBackPressed () {
        super.onBackPressed();
        Intent intent = new Intent(MapRoutePetHistory.this,Home.class);
        intent.putExtra("maproute","maproute");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
        startActivity(intent);
        finish();
    }
}




