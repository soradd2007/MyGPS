package cn.com.do1.activity;

import java.util.ArrayList;
import java.util.List;

import cn.com.do1.entity.FootPrint;
import cn.com.do1.tool.DataBaseAdapter;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MyGPSActivity extends MapActivity {
	/** Called when the activity is first created. */

	private LocationManager locationManager = null;
	private LocationListener locationListener = null;
	private GeoPoint geoPoint = null;
	private Location latestLocation = null;
	private MapView mapView = null;
	private MapController mMapController = null;
	private MyLocationOverlay myLocationOverlay = null;
	private List<Overlay> list = null;
	private DataBaseAdapter mDBAdapter;
	private FootPrint mFootPrint;

	private Drawable drawableHand = null;

	protected final static int MENU_PASSING = Menu.FIRST;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		view_init();
		locationManager_init();
		map_init();
	}

	/**
	 * 创建menu选项
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, MENU_PASSING, 0, R.string.PASSING);
		return true;
	}

	/**
	 * menu选项点击事件
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Toast toast = Toast.makeText(getApplicationContext(), "脚印已记录",
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			// 保存当前经纬度信息到sqlite中，并在地图上显示一个脚印 (1)
			if (latestLocation != null) {
				double latitude = latestLocation.getLatitude() * 1000000;
				double Longitude = latestLocation.getLongitude() * 1000000;
				String latitudeStr = String.valueOf(latitude);
				String LongitudeStr = String.valueOf(Longitude);
				mFootPrint = new FootPrint();
				mFootPrint.setLatitude(latitudeStr);
				mFootPrint.setLongitude(LongitudeStr);
				if (mDBAdapter.insertLocationInfo(mFootPrint)) {
					System.out.println("insert ok!");
				}
				// Cursor mCursor = mDBAdapter.queryLocationInfo();
				// Log.v("test", mCursor.getString(2));
				myLocationOverlay = addPoint();

				// 在当前经纬度显示一个手指指示
				geoPoint = new GeoPoint(
						(int) ((latestLocation.getLatitude() + 0.001) * 1000000),
						(int) ((latestLocation.getLongitude() + 0.003) * 1000000));
				OverlayItem overlayitem0 = new OverlayItem(geoPoint, "("
						+ latestLocation.getLatitude() + ","
						+ latestLocation.getLongitude() + ")", "");
				drawableHand.setBounds(0, 0, drawableHand.getIntrinsicWidth(),
						drawableHand.getIntrinsicHeight());
				overlayitem0.setMarker(drawableHand);

				myLocationOverlay.addOverlay(overlayitem0);
				showMapView();
				break;
			}
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart() {
		// 打开数据库
		mDBAdapter = new DataBaseAdapter(this);
		mDBAdapter.open();

		super.onStart();
	}

	@Override
	protected void onDestroy() {
		// 关闭数据库
		if (mDBAdapter != null) {
			mDBAdapter.close();
		}
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		// 关闭数据库
		if (mDBAdapter != null) {
			mDBAdapter.close();
		}
		super.onStop();
	}

	/**
	 * 初始化view
	 */
	private void view_init() {
		mapView = (MapView) findViewById(R.id.mapview);
		drawableHand = this.getResources().getDrawable(R.drawable.hand_point); // 手指图标图片
	}

	/**
	 * locationManager初始化
	 */
	private void locationManager_init() {
		locationManager = (LocationManager) this
				.getSystemService(MyGPSActivity.LOCATION_SERVICE);
		locationListener_init();

		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
		} else if (locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locationManager
					.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
							1000, 0, locationListener);
		} else {
			Toast toast = Toast.makeText(getApplicationContext(),
					"NO SERVICE ENABLED", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
			startActivityForResult(intent, 0);
		}
	}

	/**
	 * locatonListener初始化
	 */
	private void locationListener_init() {
		locationListener = new LocationListener() {
			// 位置变化时触发
			public void onLocationChanged(Location location) {
				System.out.println("onLocationChanged");
				latestLocation = location;
				if (latestLocation != null) {
					myLocationOverlay = addPoint();

					// 在当前经纬度显示一个手指指示
					geoPoint = new GeoPoint(
							(int) (latestLocation.getLatitude() * 1000000),
							(int) (latestLocation.getLongitude() * 1000000));
					OverlayItem overlayitem0 = new OverlayItem(geoPoint, "("
							+ latestLocation.getLatitude() + ","
							+ latestLocation.getLongitude() + ")", "");
					drawableHand.setBounds(0, 0,
							drawableHand.getIntrinsicWidth(),
							drawableHand.getIntrinsicHeight());
					overlayitem0.setMarker(drawableHand);

					myLocationOverlay.addOverlay(overlayitem0);
					showMapView();
				} else {
					Toast toast = Toast.makeText(getApplicationContext(),
							"取不到位置，请确定GPS服务已开启!", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			}

			// gps禁用时触发
			public void onProviderDisabled(String provider) {
				System.out.println("onProviderDisabled");
				Toast.makeText(MyGPSActivity.this, "请开启GPS！",
						Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
				startActivityForResult(intent, 0); // 此为设置完成后返回到获取界面
			}

			// gps开启时触发
			public void onProviderEnabled(String provider) {
				Toast.makeText(MyGPSActivity.this, "GPS正常", Toast.LENGTH_SHORT)
						.show();
			}

			// gps状态变化时触发
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				System.out.println("onStatusChanged");
				if (status == LocationProvider.AVAILABLE) {
					Toast.makeText(MyGPSActivity.this, "当前GPS状态：可见的！",
							Toast.LENGTH_SHORT).show();
				} else if (status == LocationProvider.OUT_OF_SERVICE) {
					Toast.makeText(MyGPSActivity.this, "当前GPS状态：服务区外！",
							Toast.LENGTH_SHORT).show();
				} else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
					Toast.makeText(MyGPSActivity.this, "当前GPS状态：暂停服务！",
							Toast.LENGTH_SHORT).show();
				}
			}
		};
	}

	/**
	 * 显示当前位置
	 */
	private void showMapView() {
		// 添加Overlay，用于显示标注信息
		list.clear();
		list.add(myLocationOverlay);
	}

	/**
	 * 打开程序时初始化地图
	 */
	private void map_init() {
		mapView.setTraffic(true);
		mapView.setBuiltInZoomControls(true);

		mMapController = mapView.getController();
		mMapController.setZoom(17);

		list = mapView.getOverlays();

		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			latestLocation = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER); // 通过GPS获取位置
			if (latestLocation == null) {
				if (locationManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
					latestLocation = locationManager
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); // 通过NETWORK获取位置
				}
			}
		} else if (locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			latestLocation = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); // 通过NETWORK获取位置
		} else {
			Toast toast = Toast.makeText(getApplicationContext(),
					"NO SERVICE ENABLED", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
			startActivityForResult(intent, 0);
		}

		if (latestLocation == null) {
			Toast toast = Toast.makeText(getApplicationContext(),
					"LOCATION IS NULL EXCEPTION", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			myLocationOverlay = addPoint();
			showMapView();
		} else {

			myLocationOverlay = addPoint();

			// 在当前经纬度显示一个手指指示
			geoPoint = new GeoPoint(
					(int) (latestLocation.getLatitude() * 1000000),
					(int) (latestLocation.getLongitude() * 1000000));
			OverlayItem overlayitem0 = new OverlayItem(geoPoint, "("
					+ latestLocation.getLatitude() + ","
					+ latestLocation.getLongitude() + ")", "");
			drawableHand.setBounds(0, 0, drawableHand.getIntrinsicWidth(),
					drawableHand.getIntrinsicHeight());
			overlayitem0.setMarker(drawableHand);

			myLocationOverlay.addOverlay(overlayitem0);

			mMapController.setCenter(geoPoint);
			showMapView();
		}
	}

	/**
	 * 把数据库中的点取到图层中
	 * 
	 * @return
	 */
	private MyLocationOverlay addPoint() {
		Drawable drawable = this.getResources().getDrawable(
				R.drawable.small_footprint); // 默认图标图片
		MyLocationOverlay locationOverlay = new MyLocationOverlay(drawable,
				this);

		// 取出sqlite中的数据，并在地图中显示 (2)
		GeoPoint point = new GeoPoint(
				(int) ((latestLocation.getLatitude() + 0.00001) * 1000000),
				(int) ((latestLocation.getLongitude() + 0.00003) * 1000000));
		OverlayItem overlayitem1 = new OverlayItem(point, "", "");

		locationOverlay.addOverlay(overlayitem1);
		return locationOverlay;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/**
	 * 用于在地图上显示标记
	 * 
	 * @author Administrator
	 * 
	 */
	class MyLocationOverlay extends ItemizedOverlay {

		private List<OverlayItem> items = new ArrayList<OverlayItem>();// 保存要画的点
		private Drawable marker;// 标注的图标
		private Context mContext;
		private GeoPoint in = null;

		public MyLocationOverlay(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
		}

		public MyLocationOverlay(Drawable defaultMarker, Context context) {
			this(defaultMarker);
			this.mContext = context;
		}

		@Override
		public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
				long when) {
			super.draw(canvas, mapView, shadow);

			// 第一个item为当前位置
			OverlayItem overlayitem0 = new OverlayItem(geoPoint, "("
					+ latestLocation.getLatitude() + ","
					+ latestLocation.getLongitude() + ")", "");
			;
			// drawableHand.setBounds(0, 0, drawableHand.getIntrinsicWidth(),
			// drawableHand.getIntrinsicHeight());
			// overlayitem0.setMarker(drawableHand);
			items.set(0, overlayitem0);

			// 将经纬度转换成实际屏幕坐标
			Projection projection = mapView.getProjection(); // 用于屏幕像素点坐标系统和地球表面经纬度点坐标系统之间的变换

			for (int index = 0; index < items.size(); index++) {
				// 得到给定索引的item
				OverlayItem overLayItem = this.getItem(index);
				Point point = projection.toPixels(overLayItem.getPoint(), null);
				Paint paint = new Paint();

				paint.setStrokeWidth(1);
				paint.setARGB(255, 255, 0, 0);
				paint.setStyle(Paint.Style.STROKE);

				canvas.drawText(overLayItem.getTitle(), point.x, point.y, paint);
			}
			return true;
		}

		public void addOverlay(OverlayItem overlayItem) {
			items.add(overlayItem);
			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return items.get(i);
		}

		@Override
		public int size() {
			return items.size();
		}
	}
}
