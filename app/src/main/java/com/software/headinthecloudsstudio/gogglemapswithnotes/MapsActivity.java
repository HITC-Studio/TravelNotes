package com.software.headinthecloudsstudio.gogglemapswithnotes;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener,
        GalleryAdapter.GalleryAdapterCallBacks
{

    private GoogleMap mMap;
    public static final int RequestPermissionCode = 1;
    GoogleApiClient mGoogleApiClient;

    //Google ApiClient
    private GoogleApiClient googleApiClient;
    private String TAG = "gps";
    public static final int REQUEST_CHECK_SETTINGS = 123;
    LocationRequest mLocationRequest;
    int INTERVAL = 1000;
    int FASTEST_INTERVAL = 500;
    FloatingActionButton floatingActionButton;
    private BottomSheetBehavior mBottomSheetBehavior1;
    LinearLayout tapactionlayout;
    View bottomSheet;
    PlaceAutocompleteFragment autocompleteFragment;
    EditText generalSearch;

    // Wait alert
    AlertDialog.Builder adbGeneralSearch;
    AlertDialog adGeneralSearch;

    // Markers
    Map<Marker, JSONObject> savedMarkers;
    Map<Marker, JSONObject> randomMarkers;
    List<LatLng> allMarkers;
    Marker currentSelectedMarker;
    Marker currentLocation;

    // Lists
    String currentList = "None";

    // Bottom area
    TextView markerName;
    EditText notesBox;
    Spinner spinnerFavList;
    Button createList;
    Button deleteList;
    Button saveMarker;
    Button removeMarker;
    Button clearMarkers;

    // Images
    ImageButton takePic;
    RecyclerView imageGallery;
    GalleryAdapter mGalleryAdapter;
    public List<GalleryItem> galleryItems;

    Context myActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Setup the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        myActivity = this;

        // Variables init
        savedMarkers = new HashMap<>();
        randomMarkers = new HashMap<>();
        allMarkers = new ArrayList<>();

        // Setup the on screen items
        HandleMap();
        HandleCurrentLocationButton();
        HandleBottomSheet();
        HandleSearchBar();
        SetupSpinner();
        HandleCamera();
        HandleImageGallery();
    }

    // Everything to do with the bottom sheet (lists, notes, etc)
    private void HandleBottomSheet()
    {
        tapactionlayout = (LinearLayout) findViewById(R.id.tap_action_layout);
        bottomSheet = findViewById(R.id.bottom_sheet1);
        mBottomSheetBehavior1 = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior1.setPeekHeight(120);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior1.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback()
        {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState)
            {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED)
                {
                    tapactionlayout.setVisibility(View.VISIBLE);
                }

                if (newState == BottomSheetBehavior.STATE_EXPANDED)
                {
                    tapactionlayout.setVisibility(View.GONE);
                }

                if (newState == BottomSheetBehavior.STATE_DRAGGING)
                {
                    tapactionlayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset)
            {

            }
        });

        tapactionlayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mBottomSheetBehavior1.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                {
                    mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        // Text boxes and buttons
        spinnerFavList = (Spinner) findViewById(R.id.spinnerFavList);

        createList = (Button) findViewById(R.id.createList);
        createList.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder alert = new AlertDialog.Builder(myActivity);

                alert.setTitle("New Favorite List");
                alert.setMessage("Name that list");

                // Set an EditText view to get user input
                final EditText input = new EditText(myActivity);
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        if (input.getText().toString().toLowerCase().compareTo("none") != 0 &&
                                input.getText().toString().toLowerCase().compareTo("all markers") != 0)
                        {
                            // Save the current list
                            SaveData(currentList);

                            // GRab current list and add the new name to it
                            ArrayAdapter adapter = (ArrayAdapter) spinnerFavList.getAdapter();

                            // Make sure this list name does not already exists
                            if (adapter.getPosition(input.getText().toString()) == -1)
                            {
                                // Remove the saved markers
                                for (Marker marker : savedMarkers.keySet())
                                    marker.remove();
                                savedMarkers.clear();

                                // Chnage the list
                                currentList = input.getText().toString();

                                // Remove none
                                adapter.remove("None");
                                // Remove All Markers
                                adapter.remove("All Markers");
                                // Add the new list name
                                adapter.add(input.getText().toString());
                                // Sort
                                adapter.sort(ALPHABETICAL_ORDER);
                                // Re add none and All Markers
                                adapter.insert("All Markers", 0);
                                adapter.insert("None", 0);
                                adapter.notifyDataSetChanged();

                                // Change to that list
                                spinnerFavList.setSelection(adapter.getPosition(input.getText().toString()));

                                if (randomMarkers.containsKey(currentSelectedMarker) == false)
                                {
                                    currentSelectedMarker = null;
                                    // Update the bottom sheet text
                                    ClearBottomSheet();
                                }
                            }
                        }
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        // Canceled.
                    }
                });

                alert.show();
            }
        });

        deleteList = (Button) findViewById(R.id.deleteList);
        deleteList.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Get the currently selected list
                if (currentList.compareTo("None") != 0 &&
                        currentList.compareTo("All Markers") != 0)
                {
                    // Remove all the markers from the map
                    for (Marker marker : savedMarkers.keySet())
                        marker.remove();

                    if (savedMarkers.containsKey(currentSelectedMarker) == true)
                    {
                        currentSelectedMarker = null;
                        // Update the bottom sheet text
                        ClearBottomSheet();
                    }
                    savedMarkers.clear();

                    // Remove the file
                    File file = new File(getFilesDir() + "/AllLists", currentList + ".txt");
                    file.delete();

                    // remove it from the spinner
                    ArrayAdapter adapter = (ArrayAdapter) spinnerFavList.getAdapter();
                    adapter.remove(currentList);
                    adapter.notifyDataSetChanged();

                    // Make sure None is selected
                    spinnerFavList.setSelection(0);
                    currentList = "None";
                }
            }
        });

        // Save Marker to current chosen list
        saveMarker = (Button) findViewById(R.id.saveMarker);
        saveMarker.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String currentList = spinnerFavList.getSelectedItem().toString();
                // MAke sure not on None or All Markers
                if (currentList.compareTo("None") != 0 &&
                        currentList.compareTo("All Markers") != 0)
                {
                    // Get the current info about this marker
                    JSONObject markerInfo = randomMarkers.get(currentSelectedMarker);

                    // Remove from random markers
                    randomMarkers.remove(currentSelectedMarker);

                    if (randomMarkers.size() == 0)
                        clearMarkers.setEnabled(false);

                    // Add to the save markers
                    savedMarkers.put(currentSelectedMarker, markerInfo);

                    // Update its color
                    currentSelectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                    saveMarker.setEnabled(false);
                    removeMarker.setEnabled(true);
                }
            }
        });

        // Remove marker
        removeMarker = (Button) findViewById(R.id.removeMarker);
        removeMarker.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String currentList = spinnerFavList.getSelectedItem().toString();
                // MAke sure not on None or All Markers
                if (currentList.compareTo("None") != 0 &&
                        currentList.compareTo("All Markers") != 0)
                {
                    // Get the current info about this marker
                    JSONObject markerInfo = savedMarkers.get(currentSelectedMarker);

                    // Remove from save markers
                    savedMarkers.remove(currentSelectedMarker);

                    // Add to the random markers
                    randomMarkers.put(currentSelectedMarker, markerInfo);

                    clearMarkers.setEnabled(true);

                    // Update its color
                    currentSelectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                    removeMarker.setEnabled(false);
                }
            }
        });

        // Clear the random markers
        clearMarkers = (Button) findViewById(R.id.clearMarkers);
        clearMarkers.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Clear all random markers from the map
                for (Marker marker : randomMarkers.keySet())
                {
                    marker.remove();
                    allMarkers.remove(marker.getPosition());
                }
                randomMarkers.clear();

                if (savedMarkers.containsKey(currentSelectedMarker) == false)
                {
                    currentSelectedMarker = null;
                    // Update the bottom sheet text
                    ClearBottomSheet();
                }

                clearMarkers.setEnabled(false);
            }
        });

        // The marker name
        markerName = (TextView) findViewById(R.id.markerName);
        markerName.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                // Update the marker
                if (currentSelectedMarker != null)
                {
                    currentSelectedMarker.setTitle(markerName.getText().toString());
                    currentSelectedMarker.hideInfoWindow();
                    currentSelectedMarker.showInfoWindow();

                    // Update the JSON data for the name
                    if (randomMarkers.containsKey(currentSelectedMarker) == true)
                    {
                        JSONObject info = randomMarkers.get(currentSelectedMarker);
                        try
                        {
                            info.put("name", markerName.getText().toString());
                        }
                        catch (JSONException e)
                        {
                            Log.e("Error", "markerName.afterTextChange, randomMarkers: " + e.getMessage());
                        }
                    }
                    else
                    {
                        JSONObject info = savedMarkers.get(currentSelectedMarker);
                        try
                        {
                            info.put("name", markerName.getText().toString());
                        }
                        catch (JSONException e)
                        {
                            Log.e("Error", "markerName.afterTextChange, savedMarkers: " + e.getMessage());
                        }
                    }
                }
            }
        });

        // Handle the notes box
        notesBox = (EditText) findViewById(R.id.notesBox);
        notesBox.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                // Update the marker
                if (currentSelectedMarker != null)
                {
                    // Update the JSON data for the name
                    if (randomMarkers.containsKey(currentSelectedMarker) == true)
                    {
                        JSONObject info = randomMarkers.get(currentSelectedMarker);
                        try
                        {
                            info.put("notes", notesBox.getText().toString());
                        }
                        catch (JSONException e)
                        {
                            Log.e("Error", "notesBox.afterTextChange, randomMarkers: " + e.getMessage());
                        }
                    }
                    else
                    {
                        JSONObject info = savedMarkers.get(currentSelectedMarker);
                        try
                        {
                            info.put("notes", notesBox.getText().toString());
                        }
                        catch (JSONException e)
                        {
                            Log.e("Error", "notesBox.afterTextChange, savedMarkers: " + e.getMessage());
                        }
                    }
                }
            }
        });
    }

    Comparator<String> ALPHABETICAL_ORDER = new Comparator<String>()
    {
        public int compare(String object1, String object2)
        {
            int res = String.CASE_INSENSITIVE_ORDER.compare(object1.toString(), object2.toString());
            return res;
        }
    };

    // Setup the spinner
    private void SetupSpinner()
    {
        ArrayAdapter adapter = (ArrayAdapter) spinnerFavList.getAdapter();
        List<String> list = new ArrayList<String>();

        // Look for all files (Favorite lists)
        File fileDir = new File(getFilesDir() + "/AllLists");
        if (fileDir.isDirectory() == true)
        {
            for (File file : fileDir.listFiles())
            {
                // Just be sure it is a file and not a directory
                if (file.isFile() == true)
                {
                    // Add the name to the spinner list
                    String name = file.getName();
                    name = name.substring(0, name.length() - 4);
                    list.add(name);
                }
            }
            // Sort the names
            Collections.sort(list);
        }

        // Add all markers to the top
        list.add(0, "All Markers");

        // Add none to the top
        list.add(0, "None");

        // Update the spinner with the lists
        ArrayAdapter<String> newAdapter = new ArrayAdapter<String>(myActivity,
                android.R.layout.simple_spinner_dropdown_item, list);
        spinnerFavList.setAdapter(newAdapter);

        spinnerFavList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // Save the current list
                SaveData(currentList);

                String selectedList = parent.getItemAtPosition(position).toString();
                if (selectedList.compareTo("None") == 0)
                {
                    // For all of the current saved markers on the screen, remove them
                    for (Marker marker : savedMarkers.keySet())
                    {
                        marker.remove();
                    }
                    // Clear the saved markers on the map
                    savedMarkers.clear();

                    if (randomMarkers.containsKey(currentSelectedMarker) == false)
                    {
                        currentSelectedMarker = null;
                        // Update the bottom sheet text
                        ClearBottomSheet();
                    }

                    deleteList.setEnabled(false);
                    removeMarker.setEnabled(false);
                    saveMarker.setEnabled(false);
                }
                else if (selectedList.compareTo("All Markers") == 0)
                {
                    // For all of the current saved markers on the screen, remove them
                    for (Marker marker : savedMarkers.keySet())
                    {
                        marker.remove();
                    }
                    // Clear the saved markers on the map
                    savedMarkers.clear();

                    // Load every list file
                    File listDir = new File(getFilesDir() + "/AllLists");
                    if (listDir.isDirectory() == true)
                    {
                        for (File listFile : listDir.listFiles())
                        {
                            if (listFile.isFile())
                                LoadData(listFile.getName().toString());
                        }
                    }

                    deleteList.setEnabled(false);
                    removeMarker.setEnabled(false);
                    saveMarker.setEnabled(false);
                }
                else
                {
                    // For all of the current saved markers on the screen, remove them
                    for (Marker marker : savedMarkers.keySet())
                    {
                        marker.remove();
                    }
                    if (savedMarkers.containsKey(currentSelectedMarker) == true)
                    {
                        currentSelectedMarker = null;
                        ClearBottomSheet();
                    }

                    // Clear the saved markers on the map
                    savedMarkers.clear();

                    // Load only this list
                    LoadData(selectedList + ".txt");

                    deleteList.setEnabled(true);

                    if (currentSelectedMarker == null)
                    {
                        ClearBottomSheet();
                    }
                    else if (randomMarkers.containsKey(currentSelectedMarker) == false)
                    {
                        ClearBottomSheet();
                    }
                    else
                    {
                        saveMarker.setEnabled(true);
                    }
                }

                currentList = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
    }

    // Handle the current location button
    private void HandleCurrentLocationButton()
    {
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getCurrentLocation();
            }
        });
    }

    // Handle the map
    private void HandleMap()
    {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Initializing googleapi client
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void HandleCamera()
    {
        // Find the camera button
        takePic = (ImageButton) findViewById(R.id.takePic);
        takePic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Ensure proper logic
                if (currentSelectedMarker != null)
                {
                    // Allow multiple images to be selected
                    Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    photoPickerIntent.setType("image/*");
                    photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    startActivityForResult(photoPickerIntent, 1);
                }
            }
        });
    }

    // Upon completion of the image taken, add it to json of the current marker and image gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1 && resultCode == RESULT_OK && data != null)
        {
            if (checkPermissionREAD_EXTERNAL_STORAGE(this))
            {
                try
                {
                    ArrayList<JSONObject> allImages = new ArrayList<>();

                    if (data.getClipData() != null)
                    {
                        int count = data.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
                        for (int i = 0; i < count; i++)
                        {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                            cursor.moveToFirst();
                            String document_id = cursor.getString(0);
                            document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
                            cursor.close();

                            cursor = getContentResolver().query(
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
                            cursor.moveToFirst();
                            String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));


                            // ADd the image returned, but make sure its not already in the shown images
                            Boolean isThere = false;
                            for (GalleryItem GI : galleryItems)
                            {
                                if (GI.imageUri.compareTo(imagePath) == 0)
                                {
                                    isThere = true;
                                    break;
                                }
                            }
                            if (isThere == false)
                            {
                                // For the gallery
                                galleryItems.add(new GalleryItem(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)),
                                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))));
                                JSONObject object = new JSONObject();
                                object.put("uriPath", cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                                object.put("imagePath", cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)));
                                allImages.add(object);
                            }

                            cursor.close();
                        }
                    }
                    else if (data.getData() != null)
                    {
                        Uri uri = data.getData();
                        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                        cursor.moveToFirst();
                        String document_id = cursor.getString(0);
                        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
                        cursor.close();

                        cursor = getContentResolver().query(
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
                        cursor.moveToFirst();
                        String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                        // ADd the image returned, but make sure its not already in the shown images
                        Boolean isThere = false;
                        for (GalleryItem GI : galleryItems)
                        {
                            if (GI.imageUri.compareTo(imagePath) == 0)
                            {
                                isThere = true;
                                break;
                            }
                        }
                        if (isThere == false)
                        {
                            // For the gallery
                            galleryItems.add(new GalleryItem(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)),
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))));
                            JSONObject object = new JSONObject();
                            object.put("uriPath", cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                            object.put("imagePath", cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)));
                            allImages.add(object);
                        }

                        cursor.close();
                    }

                    // Add to the currently selected marker
                    if (randomMarkers.containsKey(currentSelectedMarker) == true)
                    {
                        JSONObject object = randomMarkers.get(currentSelectedMarker);
                        JSONArray array = object.getJSONArray("images");
                        // Transfer the images over
                        for (JSONObject o : allImages)
                            array.put(o);
                    }
                    else
                    {
                        JSONObject object = savedMarkers.get(currentSelectedMarker);
                        JSONArray array = object.getJSONArray("images");
                        // Transfer the images over
                        for (JSONObject o : allImages)
                            array.put(o);
                    }

                    // Add the thumbnails to the gallery adapter
                    mGalleryAdapter.addGalleryItems(galleryItems);
                    mGalleryAdapter.notifyDataSetChanged();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }


    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(final Context context)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE))
                {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage" + " permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions((Activity) context,
                                            new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                                }
                            });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                }
                else
                {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return true;
        }
    }

    // Setup the gallery
    private void HandleImageGallery()
    {
        imageGallery = (RecyclerView) findViewById(R.id.imageGallery);
        //imageGallery.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(myActivity, 2);
        imageGallery.setLayoutManager(layoutManager);

        mGalleryAdapter = new GalleryAdapter(this);
        imageGallery.setAdapter(mGalleryAdapter);

        galleryItems = new ArrayList<>();
    }

    // When an image is selected
    @Override
    public void onItemSelected(int position) {
        //create fullscreen SlideShowFragment dialog
        SlideShowFragment slideShowFragment = SlideShowFragment.newInstance(position);
        //setUp style for slide show fragment
        slideShowFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogFragmentTheme);
        //finally show dialogue
        slideShowFragment.show(getFragmentManager(), "");
    }

    // Clear the bottom area
    private void ClearBottomSheet()
    {
        // Update the bottom sheet text
        markerName.setText("");
        markerName.setEnabled(false);
        notesBox.setText("");
        notesBox.setEnabled(false);
        galleryItems.clear();
        mGalleryAdapter.clearGalleryItems();
        mGalleryAdapter.notifyDataSetChanged();
        saveMarker.setEnabled(false);
        removeMarker.setEnabled(false);
    }

    //region Saving and Loading

    private void SaveData(String list)
    {
        if (list.compareTo("None") != 0 &&
                list.compareTo("All Markers") != 0)
        {
            File file = new File(getFilesDir() + "/AllLists");
            if (file.exists() == false)
                file.mkdirs();
            file = new File(file, list + ".txt");
            try
            {
                // The file to write into
                OutputStream outputStream = new FileOutputStream(file);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);

                // The JSON array to hold everything
                JSONArray dataArray = new JSONArray();
                for (Marker key : savedMarkers.keySet())
                {
                    // Insert
                    dataArray.put(savedMarkers.get(key));
                }

                try
                {
                    // Write it!
                    JSONObject data = new JSONObject();
                    try
                    {
                        data.put("entries", dataArray);
                    } catch (JSONException e)
                    {
                        Log.e("Error", "SaveData, adding to entries: " + e.getMessage());
                    }

                    outputStreamWriter.write(data.toString());
                    outputStreamWriter.flush();
                    outputStreamWriter.close();
                } catch (IOException e)
                {
                    Log.e("Error", "SaveData, writing to the file: " + e.getMessage());
                }
            } catch (FileNotFoundException e)
            {
                Log.e("Error", "SaveData, could not find the file: " + e.getMessage());
            }
        }
    }

    private void LoadData(String fileName)
    {
        // Try to "open" the file
        File file = new File(getFilesDir() + "/AllLists", fileName);
        if (file.exists() == true && file.isFile() == true)
        {
            try
            {
                InputStream inputStream = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                boolean done = false;
                try
                {
                    while (!done)
                    {
                        String line = reader.readLine();
                        done = (line == null);

                        if (line != null)
                        {
                            stringBuilder.append(line);
                        }
                    }

                    reader.close();
                    inputStream.close();

                    ParseAndMakeSavedMarkers(stringBuilder.toString());
                } catch (IOException e)
                {
                    Log.e("Error", "LoadData, reading from the file: " + e.getMessage());
                }
            } catch (FileNotFoundException e)
            {
                Log.e("Error", "LoadData, unable to open the file: " + e.getMessage());
            }
        }
    }

    //endregion

    //region Search Bar

    // Handle the search bar
    private void HandleSearchBar()
    {
        // Geo stuff
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // Location selected, send to the map
                moveMap(false, place.getName().toString(), place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.e("Error", "HandleSearchBar, autocompleteFragment error: " + status.getStatusMessage());
            }
        });
        autocompleteFragment.setHint("Specific Places Search");

        generalSearch = (EditText) findViewById(R.id.generalSearch);
        generalSearch.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                // Grab the key up for enter
                if (event.getAction() == KeyEvent.ACTION_UP &&
                        keyCode == KeyEvent.KEYCODE_ENTER)
                {
                    // Popup the wait screen
                    adbGeneralSearch = new AlertDialog.Builder(MapsActivity.this)
                            .setMessage("Please wait...")
                            .setCancelable(false);
                    adGeneralSearch = adbGeneralSearch.create();
                    adGeneralSearch.show();

                    // Start an async-task to send an http request
                    GeneralSearch generalSearchAsyncTask = new GeneralSearch();

                    // Get the radius of the visible map
                    VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
                    double visibleRadius = calculateVisibleRadius(visibleRegion);

                    // LAT, LONG, RADIUS, KEYWORD
                    generalSearchAsyncTask.execute(String.valueOf(mMap.getCameraPosition().target.latitude),
                            String.valueOf(mMap.getCameraPosition().target.longitude),
                            String.valueOf(visibleRadius),
                            generalSearch.getText().toString());
                }

                return false;
            }
        });
    }

    // Get the radius of the visible region of the map in meters
    private double calculateVisibleRadius(VisibleRegion visibleRegion) {
        float[] distanceWidth = new float[1];
        float[] distanceHeight = new float[1];

        LatLng farRight = visibleRegion.farRight;
        LatLng farLeft = visibleRegion.farLeft;
        LatLng nearRight = visibleRegion.nearRight;
        LatLng nearLeft = visibleRegion.nearLeft;

        //calculate the distance width (left <-> right of map on screen)
        Location.distanceBetween(
                (farLeft.latitude + nearLeft.latitude) / 2,
                farLeft.longitude,
                (farRight.latitude + nearRight.latitude) / 2,
                farRight.longitude,
                distanceWidth
        );

        //calculate the distance height (top <-> bottom of map on screen)
        Location.distanceBetween(
                farRight.latitude,
                (farRight.longitude + farLeft.longitude) / 2,
                nearRight.latitude,
                (nearRight.longitude + nearLeft.longitude) / 2,
                distanceHeight
        );

        //visible radius is (smaller distance) / 2:
        return (distanceWidth[0] < distanceHeight[0]) ? distanceWidth[0] / 2 : distanceHeight[0] / 2;
    }

    //endregion

    //region Map Code

    // Parse the returned json string and make markers for each spot
    private void ParseAndMakeMarkers(String jsonData)
    {
        try
        {
            // Parse and create a marker for each spot, also, store details about each marker
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray resultArray = jsonObject.getJSONArray("results");
            for (int i=0; i< resultArray.length(); i++)
            {
                // Location and name
                String lat = resultArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lat");
                String lng = resultArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lng");
                String name = resultArray.getJSONObject(i).getString("name");
                LatLng loc = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                if (allMarkers.contains(loc) == false)
                {
                    // Add this marker
                    Marker newMarker = mMap.addMarker(new MarkerOptions()
                            .position(loc) //setting position
                            .draggable(true) //Making the marker draggable
                            .title(name)); //Adding a title
                    JSONObject newJSONObject = new JSONObject();
                    newJSONObject.put("name", name);
                    newJSONObject.put("lat", lat);
                    newJSONObject.put("lng", lng);
                    newJSONObject.put("notes", "");
                    JSONArray imageArray = new JSONArray();
                    newJSONObject.put("images", imageArray);

                    randomMarkers.put(newMarker, newJSONObject);

                    allMarkers.add(loc);
                }
            }

            if (randomMarkers.size() != 0)
                clearMarkers.setEnabled(true);
        }
        catch (JSONException e)
        {
            Log.e("Error", "ParseAndMakeMarkers: " + e.getMessage());
        }

        // Hide the wait message
        adGeneralSearch.cancel();
    }

    // Parse and make saved markers
    private void ParseAndMakeSavedMarkers(String jsonData)
    {
        try
        {
            // Parse and create a marker for each spot, also, store details about each marker
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray resultArray = jsonObject.getJSONArray("entries");
            for (int i=0; i< resultArray.length(); i++)
            {
                // Location and name
                String lat = resultArray.getJSONObject(i).getString("lat");
                String lng = resultArray.getJSONObject(i).getString("lng");
                String name = resultArray.getJSONObject(i).getString("name");
                LatLng loc = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                // Add this marker
                if (mMap != null)
                {
                    Marker newMarker = mMap.addMarker(new MarkerOptions()
                            .position(loc) //setting position
                            .draggable(true) //Making the marker draggable
                            .title(name) //Adding a title
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))); // Change the color

                    // Create the JSON Object that will be saved
                    JSONObject newJSONObject = new JSONObject();
                    newJSONObject.put("name", name);
                    newJSONObject.put("lat", lat);
                    newJSONObject.put("lng", lng);
                    newJSONObject.put("notes", resultArray.getJSONObject(i).getString("notes"));
                    newJSONObject.put("images", resultArray.getJSONObject(i).getJSONArray("images"));

                    // Save it
                    savedMarkers.put(newMarker, newJSONObject);

                    if (allMarkers.contains(loc) == false)
                        allMarkers.add(loc);
                }
            }
        }
        catch (JSONException e)
        {
            Log.e("Error", "ParseAndMakeSavedMarkers: " + e.getMessage());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        // Set the listeners
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);

        if (checkPermission())
        {
            buildGoogleApiClient();
// Check the location settings of the user and create the callback to react to the different possibilities
            LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, locationSettingsRequestBuilder.build());
            result.setResultCallback(mResultCallbackFromSettings);

            // Go the users current location
            getCurrentLocation();
        }
        else
        {
            requestPermission();
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng)
    {
        String msg = String.format("Lat: %.02f, Long: %.02f", latLng.latitude, latLng.longitude);
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng) //setting position
                .draggable(false) //Making the marker draggable
                .title(msg)); //Adding a title

        JSONObject info = new JSONObject();
        try
        {
            info.put("name", msg);
            info.put("lat", String.valueOf(latLng.latitude));
            info.put("lng", String.valueOf(latLng.longitude));
            info.put("notes", "");
            JSONArray imageArray = new JSONArray();
            info.put("images", imageArray);
        }
        catch (JSONException e)
        {
            Log.e("Error", "onMapLongClick: " + e.getMessage());
        }

        randomMarkers.put(marker, info);
        clearMarkers.setEnabled(true);
    }

    @Override
    public void onMarkerDragStart(Marker marker)
    {

    }

    @Override
    public void onMarkerDrag(Marker marker)
    {

    }

    @Override
    public void onMarkerDragEnd(Marker marker)
    {

    }

    //Getting current location
    private void getCurrentLocation()
    {
        Location location = null;
        if (checkPermission())
        {
            location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        }

        if (location != null)
        {
            // moving the map to location
            moveMap(true, "", new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    //Function to move the map
    private void moveMap(Boolean currentLoc, String name, LatLng location)
    {
        //String to display current latitude and longitude
        String msg = "";
        if (name != "")
            msg = name;
        else
            String.format("Lat: %.02f, Long: %.02f", location.latitude, location.longitude);

        //Creating a LatLng Object to store Coordinates
        LatLng latLng = new LatLng(location.latitude, location.longitude);

        if (currentLoc == false)
        {
            if (allMarkers.contains(latLng) == false)
            {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng) //setting position
                        .draggable(false) //Making the marker draggable
                        .title(msg)); //Adding a title

                JSONObject info = new JSONObject();
                try
                {
                    info.put("name", msg);
                    info.put("lat", String.valueOf(latLng.latitude));
                    info.put("lng", String.valueOf(latLng.longitude));
                    info.put("notes", "");
                    JSONArray imageArray = new JSONArray();
                    info.put("images", imageArray);
                } catch (JSONException e)
                {
                    Log.e("Error", "moveMap: " + e.getMessage());
                }

                randomMarkers.put(marker, info);

                allMarkers.add(latLng);
            }
        }
        else
        {
            if (currentLocation == null)
            {
                currentLocation = mMap.addMarker(new MarkerOptions()
                        .position(latLng) //setting position
                        .draggable(false) //Making the marker draggable
                        .flat(false)
                        .icon(bitmapDescriptorFromVector(myActivity, R.drawable.ic_map_marker)));
            }
        }

        //Moving the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        //Animating the camera
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if (currentLocation.getPosition().toString().compareTo(marker.getPosition().toString()) != 0)
        {
            // Save the currently selected marker
            currentSelectedMarker = marker;

            // Update the bottom sheet
            if (randomMarkers.containsKey(currentSelectedMarker) == true)
            {
                try
                {
                    // Update the bottom sheet
                    markerName.setText(randomMarkers.get(currentSelectedMarker).getString("name"));
                    markerName.setEnabled(true);
                    notesBox.setText(randomMarkers.get(currentSelectedMarker).getString("notes"));
                    notesBox.setEnabled(true);

                    JSONArray imagesArray = randomMarkers.get(currentSelectedMarker).getJSONArray("images");
                    for (int i = 0; i < imagesArray.length(); i++)
                    {
                        String uriPath = imagesArray.getJSONObject(i).getString("uriPath");
                        String imagePath = imagesArray.getJSONObject(i).getString("imagePath");
                        galleryItems.add(new GalleryItem(uriPath, imagePath));
                    }

                    if (currentList.compareTo("All Markers") != 0)
                    {
                        saveMarker.setEnabled(true);
                        removeMarker.setEnabled(false);
                    }
                    else
                    {
                        saveMarker.setEnabled(false);
                        removeMarker.setEnabled(false);
                    }
                } catch (JSONException e)
                {
                    Log.e("Error", "onMarkerClick, getting from randomMarkers: " + e.getMessage());
                }
            }
            else
            {
                try
                {
                    // Update the bottom sheet
                    markerName.setText(savedMarkers.get(currentSelectedMarker).get("name").toString());
                    markerName.setEnabled(true);
                    notesBox.setText(savedMarkers.get(currentSelectedMarker).get("notes").toString());
                    notesBox.setEnabled(true);

                    JSONArray imagesArray = savedMarkers.get(currentSelectedMarker).getJSONArray("images");
                    for (int i = 0; i < imagesArray.length(); i++)
                    {
                        String uriPath = imagesArray.getJSONObject(i).getString("uriPath");
                        String imagePath = imagesArray.getJSONObject(i).getString("imagePath");
                        galleryItems.add(new GalleryItem(uriPath, imagePath));
                    }

                    if (currentList.compareTo("All Markers") != 0)
                    {
                        saveMarker.setEnabled(false);
                        removeMarker.setEnabled(true);
                    }
                    else
                    {
                        saveMarker.setEnabled(false);
                        removeMarker.setEnabled(false);
                    }
                } catch (JSONException e)
                {
                    Log.e("Error", "onMarkerClick, getting from savedMarkers: " + e.getMessage());
                }
            }

            // Update the gallery
            mGalleryAdapter.addGalleryItems(galleryItems);
            mGalleryAdapter.notifyDataSetChanged();
        }

        return false;
    }

    @Override
    public void onMapClick(LatLng point)
    {
        // Clicked off the marker
        currentSelectedMarker = null;

        // Update the bottom sheet text
        ClearBottomSheet();
    }

    @Override
    public void onLocationChanged(Location location)
    {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        currentLocation.setPosition(latLng);
    }

    //endregion

    //region Ignore

    @Override
    protected void onStart()
    {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        // Save the current list
        SaveData(currentList);

        googleApiClient.disconnect();
        super.onStop();
    }

    // The callback for the management of the user settings regarding location
    private ResultCallback<LocationSettingsResult> mResultCallbackFromSettings = new ResultCallback<LocationSettingsResult>()
    {
        @Override
        public void onResult(LocationSettingsResult result)
        {
            final Status status = result.getStatus();
            switch (status.getStatusCode())
            {
                case LocationSettingsStatusCodes.SUCCESS:
// All location settings are satisfied. The client can initialize location
// requests here.
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
// Location settings are not satisfied. But could be fixed by showing the user
// a dialog.
                    try
                    {
// Show the dialog by calling startResolutionForResult(),
// and check the result in onActivityResult().
                        status.startResolutionForResult(
                                MapsActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e)
                    {
// Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.e(TAG, "Settings change unavailable. We have no way to fix the settings so we won't show the dialog.");
                    break;
            }
        }
    };

    private void requestPermission()
    {
        ActivityCompat.requestPermissions(MapsActivity.this, new String[]
                {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case RequestPermissionCode:
                if (grantResults.length > 0)
                {
                    boolean finelocation = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean coarselocation = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (finelocation && coarselocation)
                    {
                        if (checkPermission())
                            buildGoogleApiClient();
                        Toast.makeText(MapsActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(MapsActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission()
    {
        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    protected synchronized void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    //endregion

    //region General Search Async-Task

    private class GeneralSearch extends AsyncTask<String, Integer, String>
    {
        Boolean errorOccured = false;
        String urlRequest = "";

        // 0=LAT, 1=LONG, 2=RADIUS, 3=KEYWORD
        protected String doInBackground(String... params)
        {
            // Start a new Async task
            String result = "";
            errorOccured = false;

            // Setup the url request
            try
            {
                urlRequest = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                        "location=" + params[0] + "," + params[1] +
                        "&radius=" + params[2] +
                        "&language=en&keyword=" + URLEncoder.encode(params[3], "UTF-8") +
                        "&key=" + getString(R.string.placekey);
                try
                {
                    URL url = new URL(urlRequest);
                    try
                    {
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setConnectTimeout(5000);
                        urlConnection.setRequestMethod("GET");

                        // Response from the URL
                        urlConnection.connect();
                        // Ensure proper response
                        if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK)
                        {
                            errorOccured = false;

                            // Grab all of the data
                            BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                            String line;
                            while ((line = rd.readLine()) != null)
                            {
                                result += line;
                            }
                            rd.close();
                        }

                        // Close the connection
                        urlConnection.disconnect();
                    }
                    catch (IOException e)
                    {
                        errorOccured = true;
                        result = e.getMessage();
                        Log.e("Error", "GeneralSearch.doInBackground, unable to read: " + e.getMessage());
                    }
                }
                catch (MalformedURLException e)
                {
                    errorOccured = true;
                    result = e.getMessage();
                    Log.e("Error", "GeneralSearch.doInBackground, bad URL: " + e.getMessage());
                }
            }
            catch (UnsupportedEncodingException e)
            {
                errorOccured = true;
                result = e.getMessage();
                Log.e("Error", "GeneralSearch.doInBackground, bad encoding: " + e.getMessage());
            }

            return result;
        }

        protected void onPostExecute(String result)
        {
            // Pass the json back
            if (errorOccured == false)
            {
                ParseAndMakeMarkers(result);
            }
            else
            {
                Toast.makeText(MapsActivity.this, result, Toast.LENGTH_LONG).show();
            }
        }
    }

    //endregion

}