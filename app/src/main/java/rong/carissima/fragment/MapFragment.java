/*Copyright ©2015 TommyLemon(https://github.com/TommyLemon)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package rong.carissima.fragment;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import java.io.InputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import rong.carissima.R;
import zuo.biao.library.base.BaseFragment;
import zuo.biao.library.model.Entry;
import zuo.biao.library.util.Log;
// classes needed to add a marker
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
// classes to calculate a route
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.api.directions.v5.DirectionsCriteria.PROFILE_WALKING;
import static com.mapbox.mapboxsdk.style.expressions.Expression.heatmapDensity;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapIntensity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapRadius;


/** 使用方法：复制>粘贴>改名>改代码 */

/**fragment示例
 * @author Lemon
 * @use new DemoFragment(),具体参考.DemoFragmentActivity(initData方法内)
 */
public class MapFragment extends BaseFragment implements
		OnMapReadyCallback, PermissionsListener, LifecycleOwner, LocationEngineListener {
	private static final String TAG = "MapFragment";

	//与Activity通信<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<


	/**创建一个Fragment实例
	 * @return
	 */
	public static MapFragment createInstance() {
		MapFragment fragment = new MapFragment();

		return fragment;
	}


	//与Activity通信>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


    private LifecycleRegistry mLifecycleRegistry;
	private boolean mServiceState = false;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		Mapbox.getInstance(container.getContext(), this.getString(R.string.mapbox_token));

		//TODO demo_fragment改为你所需要的layout文件
		setContentView(R.layout.map_fragment);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
//        mapView.onStart();
		mapView.getMapAsync(this);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMarkersDatabaseReference = mFirebaseDatabase.getReference().child("markers");

		//功能归类分区方法，必须调用<<<<<<<<<<
		initView();
		initData();
		initEvent();
		//功能归类分区方法，必须调用>>>>>>>>>>

        Log.w(TAG, "On create view");
		return view;//返回值必须为view
	}


	//UI显示区(操作UI，但不存在数据获取或处理代码，也不存在事件监听代码)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	//示例代码<<<<<<<<
    private MapView mapView;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private LocationLayerPlugin locationPlugin;
    private LocationEngine locationEngine;
    private Location originLocation;


    // variables for adding a marker
    private Marker destinationMarker;
    private LatLng originCoord;
    private LatLng destinationCoord;

    // variables for calculating and drawing a route
    private Point originPosition;
    private Point destinationPosition;
    private DirectionsRoute currentRoute;
    private NavigationMapRoute navigationMapRoute;

    private static final String HEATMAP_SOURCE_ID = "HEATMAP_SOURCE_ID";
    private static final String HEATMAP_LAYER_ID = "HEATMAP_LAYER_ID";

    private Expression[] listOfHeatmapColors;
    private Expression[] listOfHeatmapRadiusStops;
    private Float[] listOfHeatmapIntensityStops;
    private int index;

    private FeatureCollection mGeoFeatures;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMarkersDatabaseReference;
    private ChildEventListener mChildEventListener;

    //示例代码>>>>>>>>
	@Override
	public void initView() {//必须在onCreateView方法内调用

		//示例代码<<<<<<<<<<<<<<
 		//示例代码>>>>>>>>>>>>>>
	}

	//UI显示区(操作UI，但不存在数据获取或处理代码，也不存在事件监听代码)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>










	//Data数据区(存在数据获取或处理代码，但不存在事件监听代码)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	//示例代码<<<<<<<<
	private List<Entry<String, String>> list;
	//示例代码>>>>>>>>>
	@Override
	public void initData() {//必须在onCreateView方法内调用

		//示例代码<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		showShortToast(TAG + ": serviceState = " + mServiceState);

		showProgressDialog(R.string.loading);

		runThread(TAG + "initData", new Runnable() {
			@Override
			public void run() {
				runUiThread(new Runnable() {
					@Override
					public void run() {
                        mGeoFeatures = new FeatureCollection();

						dismissProgressDialog();
					}
				});
			}
		});

		//示例代码>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	}


	//Data数据区(存在数据获取或处理代码，但不存在事件监听代码)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>








	//Event事件区(只要存在事件监听代码就是)<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	@Override
	public void initEvent() {//必须在onCreateView方法内调用
		//示例代码<<<<<<<<<<<<<<<<<<<
        mLifecycleRegistry = new LifecycleRegistry(this);
        mLifecycleRegistry.markState(Lifecycle.State.CREATED);

        attachDatabaseReadListener();
//        setOnMapLongClick();
		//示例代码>>>>>>>>>>>>>>>>>>>
	}

	//系统自带监听<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<



	//类相关监听<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        enableLocationPlugin();

////        refreshHeatLayer();
//        TimerTask task = new TimerTask(){
//         public void run(){
//             refreshHeatLayer(); //execute the task
//             timer.purge();
//              }
//         };
//        Timer timer = new Timer();
//        timer.schedule(task, 1000);
        new Handler().postDelayed(new Runnable(){
            public void run() {
                //execute the task
                refreshHeatLayer();
            }
        }, 1500);
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationPlugin() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getActivity().getApplicationContext())) {
            initializeLocationEngine();
            // Create an instance of the plugin. Adding in LocationLayerOptions is also an optional
            // parameter
            locationPlugin = new LocationLayerPlugin(mapView, mapboxMap, locationEngine);
            locationPlugin.setRenderMode(RenderMode.COMPASS);
            // Set the plugin's camera mode
//            locationPlugin.setCameraMode(CameraMode.TRACKING);
            mLifecycleRegistry.markState(Lifecycle.State.CREATED);
            getLifecycle().addObserver(locationPlugin);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }
    @SuppressWarnings( {"MissingPermission"})
    private void initializeLocationEngine() {
        LocationEngineProvider locationEngineProvider = new LocationEngineProvider(getActivity().getApplicationContext());
        locationEngine = locationEngineProvider.obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            Log.i(TAG, lastLocation.toString());
            originLocation = lastLocation;
            setCameraPosition(lastLocation);

            setOnMapClick();
            setOnMapLongClick();
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }
    private void setCameraPosition(Location location) {
        Log.i(TAG, "Latitude: " + location.getLatitude() + " Longitude: " + location.getLongitude()
                + " Altitude: " + location.getAltitude());
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 14));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(getActivity(), "user_location_permission_explanation", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationPlugin();
        } else {
            Toast.makeText(getActivity(), "user_location_permission_not_granted", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            originLocation = location;
            Log.i(TAG, location.toString());
            setCameraPosition(location);
            locationEngine.removeLocationEngineListener(this);
            setOnMapClick();
            setOnMapLongClick();

        }
    }
	//类相关监听>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	//系统自带监听方法>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }

	//类相关监听>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	//系统自带监听>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    @Override
    public void onStart() {
        super.onStart();

        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStart();
        }
        mapView.onStart();
        mLifecycleRegistry.markState(Lifecycle.State.STARTED);
//        Log.w(TAG, "On start");
    }
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        mLifecycleRegistry.markState(Lifecycle.State.RESUMED);
//        Log.w(TAG, "On resume");
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
//        Log.w(TAG, "On Pause");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStop();
        }
        mapView.onStop();
//        Log.w(TAG, "On stop");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
        mLifecycleRegistry.markState(Lifecycle.State.DESTROYED);
//        Log.w(TAG, "On destroy");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
//        Log.w(TAG, "On save instance state");
    }

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(getActivity()).profile(PROFILE_WALKING)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }


    private void addHeatmapDataSource() {
//        mapboxMap.addSource(new GeoJsonSource(HEATMAP_SOURCE_ID,
//                loadGeoJsonFromAsset("la_heatmap_styling_points.geojson")));
        if(mapboxMap.getLayer(HEATMAP_LAYER_ID) != null){
            mapboxMap.removeLayer(HEATMAP_LAYER_ID);
        }
        if(mapboxMap.getSource(HEATMAP_SOURCE_ID) != null){
            mapboxMap.removeSource(HEATMAP_SOURCE_ID);
        }
        try {
            mapboxMap.addSource(new GeoJsonSource(HEATMAP_SOURCE_ID, mGeoFeatures.toJSON().toString()));

        } catch (Exception exception) {
            Log.e("TAG", "Exception loading GeoJSON: " + exception.toString());
         }

    }

    private void addHeatmapLayer() {
        // Create the heatmap layer
        HeatmapLayer layer = new HeatmapLayer(HEATMAP_LAYER_ID, HEATMAP_SOURCE_ID);

        // Heatmap layer disappears at whatever zoom level is set as the maximum
        layer.setMaxZoom(18);

        layer.setProperties(
                // Color ramp for heatmap.  Domain is 0 (low) to 1 (high).
                // Begin color ramp at 0-stop with a 0-transparency color to create a blur-like effect.
                heatmapColor(listOfHeatmapColors[index]),

                // Increase the heatmap color weight weight by zoom level
                // heatmap-intensity is a multiplier on top of heatmap-weight
                heatmapIntensity(listOfHeatmapIntensityStops[index]),

                // Adjust the heatmap radius by zoom level
                heatmapRadius(listOfHeatmapRadiusStops[index]
                ),

                heatmapOpacity(1f)
        );

        // Add the heatmap layer to the map and above the "water-label" layer
        mapboxMap.addLayerAbove(layer, "waterway-label");
    }


    private void initHeatmapColors() {
        listOfHeatmapColors = new Expression[] {
                // 0
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(0, 0, 0, 0.01),
                        literal(0.25), rgba(224, 176, 63, 0.5),
                        literal(0.5), rgb(247, 252, 84),
                        literal(0.75), rgb(186, 59, 30),
                        literal(0.9), rgb(255, 0, 0)
                ),
                // 1
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(255, 255, 255, 0.4),
                        literal(0.25), rgba(4, 179, 183, 1.0),
                        literal(0.5), rgba(204, 211, 61, 1.0),
                        literal(0.75), rgba(252, 167, 55, 1.0),
                        literal(1), rgba(255, 78, 70, 1.0)
                ),
                // 2
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(12, 182, 253, 0.0),
                        literal(0.25), rgba(87, 17, 229, 0.5),
                        literal(0.5), rgba(255, 0, 0, 1.0),
                        literal(0.75), rgba(229, 134, 15, 0.5),
                        literal(1), rgba(230, 255, 55, 0.6)
                ),
                // 3
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(135, 255, 135, 0.2),
                        literal(0.5), rgba(255, 99, 0, 0.5),
                        literal(1), rgba(47, 21, 197, 0.2)
                ),
                // 4
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(4, 0, 0, 0.2),
                        literal(0.25), rgba(229, 12, 1, 1.0),
                        literal(0.30), rgba(244, 114, 1, 1.0),
                        literal(0.40), rgba(255, 205, 12, 1.0),
                        literal(0.50), rgba(255, 229, 121, 1.0),
                        literal(1), rgba(255, 253, 244, 1.0)
                ),
                // 5
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(0, 0, 0, 0.01),
                        literal(0.05), rgba(0, 0, 0, 0.05),
                        literal(0.4), rgba(254, 142, 2, 0.7),
                        literal(0.5), rgba(255, 165, 5, 0.8),
                        literal(0.8), rgba(255, 187, 4, 0.9),
                        literal(0.95), rgba(255, 228, 173, 0.8),
                        literal(1), rgba(255, 253, 244, .8)
                ),
                //6
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(0, 0, 0, 0.01),
                        literal(0.3), rgba(82, 72, 151, 0.4),
                        literal(0.4), rgba(138, 202, 160, 1.0),
                        literal(0.5), rgba(246, 139, 76, 0.9),
                        literal(0.9), rgba(252, 246, 182, 0.8),
                        literal(1), rgba(255, 255, 255, 0.8)
                ),

                //7
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(0, 0, 0, 0.01),
                        literal(0.1), rgba(0, 2, 114, .1),
                        literal(0.2), rgba(0, 6, 219, .15),
                        literal(0.3), rgba(0, 74, 255, .2),
                        literal(0.4), rgba(0, 202, 255, .25),
                        literal(0.5), rgba(73, 255, 154, .3),
                        literal(0.6), rgba(171, 255, 59, .35),
                        literal(0.7), rgba(255, 197, 3, .4),
                        literal(0.8), rgba(255, 82, 1, 0.7),
                        literal(0.9), rgba(196, 0, 1, 0.8),
                        literal(0.95), rgba(121, 0, 0, 0.8)
                ),
                // 8
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(0, 0, 0, 0.01),
                        literal(0.1), rgba(0, 2, 114, .1),
                        literal(0.2), rgba(0, 6, 219, .15),
                        literal(0.3), rgba(0, 74, 255, .2),
                        literal(0.4), rgba(0, 202, 255, .25),
                        literal(0.5), rgba(73, 255, 154, .3),
                        literal(0.6), rgba(171, 255, 59, .35),
                        literal(0.7), rgba(255, 197, 3, .4),
                        literal(0.8), rgba(255, 82, 1, 0.7),
                        literal(0.9), rgba(196, 0, 1, 0.8),
                        literal(0.95), rgba(121, 0, 0, 0.8)
                ),
                // 9
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(0, 0, 0, 0.01),
                        literal(0.1), rgba(0, 2, 114, .1),
                        literal(0.2), rgba(0, 6, 219, .15),
                        literal(0.3), rgba(0, 74, 255, .2),
                        literal(0.4), rgba(0, 202, 255, .25),
                        literal(0.5), rgba(73, 255, 154, .3),
                        literal(0.6), rgba(171, 255, 59, .35),
                        literal(0.7), rgba(255, 197, 3, .4),
                        literal(0.8), rgba(255, 82, 1, 0.7),
                        literal(0.9), rgba(196, 0, 1, 0.8),
                        literal(0.95), rgba(121, 0, 0, 0.8)
                ),
                // 10
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(0, 0, 0, 0.01),
                        literal(0.1), rgba(0, 2, 114, .1),
                        literal(0.2), rgba(0, 6, 219, .15),
                        literal(0.3), rgba(0, 74, 255, .2),
                        literal(0.4), rgba(0, 202, 255, .25),
                        literal(0.5), rgba(73, 255, 154, .3),
                        literal(0.6), rgba(171, 255, 59, .35),
                        literal(0.7), rgba(255, 197, 3, .4),
                        literal(0.8), rgba(255, 82, 1, 0.7),
                        literal(0.9), rgba(196, 0, 1, 0.8),
                        literal(0.95), rgba(121, 0, 0, 0.8)
                ),
                // 11
                interpolate(
                        linear(), heatmapDensity(),
                        literal(0.01), rgba(0, 0, 0, 0.25),
                        literal(0.25), rgba(229, 12, 1, .7),
                        literal(0.30), rgba(244, 114, 1, .7),
                        literal(0.40), rgba(255, 205, 12, .7),
                        literal(0.50), rgba(255, 229, 121, .8),
                        literal(1), rgba(255, 253, 244, .8)
                )
        };
    }

    private void initHeatmapRadiusStops() {
        listOfHeatmapRadiusStops = new Expression[] {
                // 0
                interpolate(
                        linear(), zoom(),
                        literal(6), literal(50),
                        literal(20), literal(100)
                ),
                // 1
                interpolate(
                        linear(), zoom(),
                        literal(12), literal(70),
                        literal(20), literal(100)
                ),
                // 2
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(7),
                        literal(5), literal(50)
                ),
                // 3
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(7),
                        literal(5), literal(50)
                ),
                // 4
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(7),
                        literal(5), literal(50)
                ),
                // 5
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(7),
                        literal(15), literal(200)
                ),
                // 6
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(10),
                        literal(8), literal(70)
                ),
                // 7
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(10),
                        literal(8), literal(200)
                ),
                // 8
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(10),
                        literal(8), literal(200)
                ),
                // 9
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(10),
                        literal(8), literal(200)
                ),
                // 10
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(10),
                        literal(8), literal(200)
                ),
                // 11
                interpolate(
                        linear(), zoom(),
                        literal(1), literal(10),
                        literal(8), literal(200)
                ),
        };
    }

    private void initHeatmapIntensityStops() {
        listOfHeatmapIntensityStops = new Float[] {
                // 0
                0.6f,
                // 1
                0.3f,
                // 2
                1f,
                // 3
                1f,
                // 4
                1f,
                // 5
                1f,
                // 6
                1.5f,
                // 7
                0.8f,
                // 8
                0.25f,
                // 9
                0.8f,
                // 10
                0.25f,
                // 11
                0.5f
        };
    }

    private String loadGeoJsonFromAsset(String filename) {
        try {
            // Load GeoJSON file
            InputStream is = getActivity().getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
//            Log.i(TAG, new String(buffer, "UTF-8"));
            return new String(buffer, "UTF-8");

        } catch (Exception exception) {
            Log.e("MultipleHeatmapStyling", "Exception loading GeoJSON: " + exception.toString());
            exception.printStackTrace();
            return null;
        }
    }
    //Event事件区(只要存在事件监听代码就是)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    void setOnMapClick(){
        mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng point) {
                if (destinationMarker != null) {
                    mapboxMap.removeMarker(destinationMarker);
                }
                destinationCoord = point;
                destinationMarker = mapboxMap.addMarker(new MarkerOptions()
                        .position(destinationCoord)
                );

                originCoord = new LatLng(originLocation.getLatitude(), originLocation.getLongitude());
                destinationPosition = Point.fromLngLat(destinationCoord.getLongitude(), destinationCoord.getLatitude());
                originPosition = Point.fromLngLat(originCoord.getLongitude(), originCoord.getLatitude());
                getRoute(originPosition, destinationPosition);
            }
        });
    }
    //Event事件区(只要存在事件监听代码就是)>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    void setOnMapLongClick(){
        mapboxMap.addOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng point) {
                Log.i(TAG, "onMapLongClick");
                addNewMarker(point.getLatitude(), point.getLongitude());
                firebasePoint fpoint = new firebasePoint(point.getLatitude(), point.getLongitude());
                mMarkersDatabaseReference.push().setValue(fpoint);
                refreshHeatLayer();
            }
        });
    }


    private void addNewMarker(double latitude, double longitude){
        // Create geometry
        myPoint geoPoint = new myPoint(latitude,longitude);

// Create feature with geometry
        Feature feature = new Feature(geoPoint);

// Set optional feature properties
        feature.setProperties(new JSONObject());

// Convert to formatted JSONObject
        try {
            JSONObject geoFeature = feature.toJSON();
            Log.i(TAG, geoFeature.toString());
        }catch(org.json.JSONException e){
            Log.w(TAG, e.toString());
        }
        mGeoFeatures.addFeature(feature);
        try {
            JSONObject geoFeatures = mGeoFeatures.toJSON();
            Log.i(TAG, geoFeatures.toString());
        }catch(org.json.JSONException e){
            Log.w(TAG, e.toString());
        }
    }




	//内部类,尽量少用<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    firebasePoint newpoint =  dataSnapshot.getValue(firebasePoint.class);
                    addNewMarker(newpoint.getLatitude(), newpoint.getLongitude());
//                    refreshHeatLayer();
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                public void onCancelled(DatabaseError databaseError) {}
            };
            mMarkersDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void refreshHeatLayer(){
        addHeatmapDataSource();
        initHeatmapColors();
        initHeatmapRadiusStops();
        initHeatmapIntensityStops();
        addHeatmapLayer();
    }
	//内部类,尽量少用>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

}