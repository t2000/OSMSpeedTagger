/**
 * 
 */
package de.stefantriller.OSMSpeedTagger;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;

/**
 * @author stefan
 *
 */
public class LocationLoggerService extends Service {

	private LocationManager lm;
	private String GPS_FILTER = "de.stefantriller.GPS_LOCATION";
	private Thread triggerService;
	private GPSListener listener;
	private boolean isRunning = true;
	
	public LocationLoggerService() {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		isRunning = false;
	}
	
	@Override
	public void onCreate() {	
		super.onCreate();
		isRunning = true;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		isRunning = true;
		
		triggerService = new Thread(new Runnable() {
			
			public void run() {
				try
				{
					if(! isRunning) return;
					Looper.prepare();
					
					lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
					listener = new GPSListener();
					lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
					
					Looper.loop();
				}
				catch (Exception e) {
					System.out.println(e.toString());
				}				
			}
		});
		
		triggerService.start();	
	}
	
	private class GPSListener implements LocationListener {

		public GPSListener() {
		}

		@Override
		public void onLocationChanged(Location loc) {
			double lon = loc.getLongitude();
			double lat = loc.getLatitude();
			
			Intent response = new Intent(GPS_FILTER);
			response.putExtra("longitude", lon);
			response.putExtra("latitude", lat);
			
			sendBroadcast(response);
		}

		@Override
		public void onProviderDisabled(String arg0) {
		}

		@Override
		public void onProviderEnabled(String arg0) {
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		}
	}

}
