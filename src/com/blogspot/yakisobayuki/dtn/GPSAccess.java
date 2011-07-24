package com.blogspot.yakisobayuki.dtn;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * GPSで現在地を取得するためのクラス
 * 
 */
public class GPSAccess {
	String CurrentAddress = null;
	double latitude;
	double longitude;
	final String TAG = "TrainTransfer";
	final String TAGtest = "TrainTransferTest";

	/**
	 * 現在地を取得する
	 * 
	 * @return 現在地　取得失敗時はnullを返す
	 */
	public String getCurrentAddress(Context context) {
		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = locationManager.getProviders(true);
		LocationListener listener = new LocationListener() {

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onLocationChanged(Location location) {
				if (location != null) {
					latitude = location.getLatitude();
					longitude = location.getLongitude();
				}
			}
		};

		for (String provider : providers) {
			locationManager.requestLocationUpdates(provider, 1000, 0, listener);
			Location location = locationManager.getLastKnownLocation(provider);
			if (location != null) {
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				break;
			} else {
				return null;
			}
		}

		Geocoder geocoder = new Geocoder(context);
		try {
			List<Address> a = geocoder.getFromLocation(latitude, longitude, 1);
			CurrentAddress = a.get(0).getAddressLine(1);
			Log.d(TAG, CurrentAddress);
		} catch (IOException e) {
			Log.d(TAG, e.toString());
			e.printStackTrace();
		}
		return CurrentAddress;
	}

	@Override
	public String toString() {
		return CurrentAddress;
	}
}
