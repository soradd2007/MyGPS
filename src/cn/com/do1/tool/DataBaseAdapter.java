package cn.com.do1.tool;

import cn.com.do1.entity.FootPrint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class DataBaseAdapter {
	private final static String TABLE_NAME_LOCATION_INFO = "LocationInfo";
	private final static String LOCATION_ID = "_id";
	private final static String USER_ID = "userID";
	private final static String LOCATION_LATITUDE = "latitude";
	private final static String LOCATION_LONGITUDE = "longitude";
	private final static String LOCATION_ALTITUDE = "altitude";
	private final static String TIME = "time";
	private final static String COUNTRY_NAME = "countryName";
	private final static String CITY_NAME = "cityName";
	private final static String TOWN_NAME = "townName";
	private final static String VILLAGE_NAME = "villageName";
	private final static String MOBILE_TYPE = "mobileType";
	private final static String USER_NAME = "userName";
	private final static String DESCRIPTION = "description";
	private final static String PHOTO = "photo";
	
	private final static int DATABASE_VERSION = 1;
	private final static String DATABASE_NAME = "footprint";
	private final Context mContext;
	private DataBaseHelper mDBHelper;
	private SQLiteDatabase db;

	public String CREATE_TABLE_LOCATON_INFO = "create table "
			+ TABLE_NAME_LOCATION_INFO + "(" + LOCATION_ID
			+ " integer primary key autoincrement," + LOCATION_LATITUDE
			+ " varchar(50)," + LOCATION_LONGITUDE + " varchar(50));";
	
	public DataBaseAdapter(Context context) {
		super();
		this.mContext = context;
	}
	
	/**
	 * 数据处理工具类
	 * @author Administrator
	 *
	 */
	private class DataBaseHelper extends SQLiteOpenHelper{

		public DataBaseHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TABLE_LOCATON_INFO);
			Log.v("databaseAdapter", "create  finish");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_LOCATION_INFO);
			onCreate(db);
		}

	}
	
	
	/**
	 * create or open a database
	 */
	public DataBaseAdapter open() {
		try {
			mDBHelper = new DataBaseHelper(mContext, DATABASE_NAME, null, DATABASE_VERSION);
			db = mDBHelper.getWritableDatabase();
		} catch (SQLException e) {
			Log.e("new_DB", "open error");
		}
		return this;
	}
	
	/**
	 * close database
	 */
	public void close() {
		mDBHelper.close();
	}
	
	/**
	 * query LocationInfo
	 */
	public Cursor queryLocationInfo() {
		Cursor mCursor = null;
		try {
			mCursor = db.query(TABLE_NAME_LOCATION_INFO, null, null, null, null,
					null, null);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		// if the cursor is empty. its moveToFirst will return false
		if (mCursor != null) {
			if (!mCursor.moveToFirst()) {
				return null;
			}
		}
		return mCursor;
	}
	
	/**
	 * save locationInfo
	 */
	public boolean insertLocationInfo(FootPrint mFootPrint) {
		boolean flag = false;
		ContentValues values = new ContentValues();

 		values.put(LOCATION_LATITUDE, mFootPrint.getLatitude());
		values.put(LOCATION_LONGITUDE, mFootPrint.getLongitude());
//		values.put(LOCATION_ALTITUDE, mFootPrint.getAltitude());


		if (db.insert(TABLE_NAME_LOCATION_INFO, null, values) != -1) {
			flag = true;
			Log.v("insert", "ok");
		}
		return flag;
	}
	
	/**
	 * update locationInfo
	 */
	public boolean updateLocationInfo(FootPrint mFootPrint) {
		boolean flag = false;
		ContentValues values = new ContentValues();

		values.put(LOCATION_LATITUDE, mFootPrint.getLatitude());
		values.put(LOCATION_LONGITUDE, mFootPrint.getLongitude());
//		values.put(LOCATION_ALTITUDE, mFootPrint.getAltitude());

		if (db.update(TABLE_NAME_LOCATION_INFO, values, LOCATION_ID + "="
				+ "1", null) > 0) {
			flag = true;
		}
		return flag;
	}
	
	
	
}
