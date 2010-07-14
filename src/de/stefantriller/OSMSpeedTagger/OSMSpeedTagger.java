package de.stefantriller.OSMSpeedTagger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class OSMSpeedTagger extends Activity {
	
	private TextView debugView;
	
	private BufferedWriter bw;
	private FileWriter fw;
	private File logFile;
	
	private Document doc;
	private int idCounter = 1;
	
	private double lat = -1000;
	private double lon = -1000;
	
	private double oldLat = -1000;
	private double oldLon = -1000;
	
	private Intent locService;
	private BroadcastReceiver receiver;
	ComponentName service;
	
	private String GPS_FILTER = "de.stefantriller.GPS_LOCATION"; 
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setRequestedOrientation(0);
        
        debugView = (TextView) findViewById(R.id.StatusView);
        
        //ComponentName comp = new ComponentName(getPackageName(), LocationLoggerService.class.getName());        
        //locService = new Intent().setComponent(comp);
        
        locService = new Intent(this, LocationLoggerService.class);
        service = startService(locService);
        
        IntentFilter filter = new IntentFilter(GPS_FILTER);
        receiver = new GPSReceiver();
        registerReceiver(receiver, filter);
        
    }
    
    @Override
    protected void onDestroy()
    {
    	super.onDestroy();
    	closeFile();
    	try
    	{
    		stopService(locService);
    		unregisterReceiver(receiver);
    	}
    	catch (Exception e) {
    		System.out.println(e.toString());
		}
    }
    
    @Override
    protected void onPause() {    	
    	super.onPause();
    	stopService(locService);
    	unregisterReceiver(receiver);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	
    	super.onCreateOptionsMenu(menu);
    	
    	menu.add("Start");
    	menu.add("Stop");
    	
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	if("Start".equals(item.getTitle()))
    	{    		
    		debugView.setText("Start pressed");
    		openFile();
    	}
    	else if("Stop".equals(item.getTitle()))
    	{
    		debugView.setText("Stop pressed");
			closeFile();
    	}
    	return true;
    }
    
    public void buttonHandler(View view)
    {
    	switch (view.getId()) {
		case R.id.Speed7:
			writeSpeed(7);
			break;
		case R.id.Speed30:
			writeSpeed(30);
			break;
		case R.id.Speed40:
			writeSpeed(40);
			break;
		case R.id.Speed50:
			writeSpeed(50);
			break;
		case R.id.Speed60:
			writeSpeed(60);
			break;
		case R.id.Speed70:
			writeSpeed(70);
			break;
		case R.id.Speed80:
			writeSpeed(80);
			break;
		case R.id.Speed100:
			writeSpeed(100);
			break;
		case R.id.Speed120:
			writeSpeed(120);
			break;
		case R.id.Speed130:
			writeSpeed(130);
			break;
		default:
			return;
    	}
    }
    
    private void writeSpeed(int spd)
    {    	
    	if(doc == null)
    	{
    		debugView.setText("Please press start first");
    		return;
    	}
    	
    	if(this.lat < -999.9999999999 && this.lat > -1000.0000000001 ||
    		this.lon < -999.9999999999 && this.lon > -1000.00000000001)
    	{
    		debugView.setText("No position received yet!");
    		return; //no pos received yet
    	}
    	
    	double latDiff = Math.abs(this.oldLat - this.lat);
    	double lonDiff = Math.abs(this.oldLon - this.lon);
    	
    	if( latDiff < 0.0000001 && lonDiff < 0.0000001)
    	{
    		debugView.setText("Position is SAME as before");
    		return; //old position
    	}
    	
    	Element root = doc.getDocumentElement();    	
    	Element node = XMLHelper.createXmlElement(doc, root, "node");
    	
    	XMLHelper.setXmlAttributeValue(doc, node, "id", Integer.toString(idCounter++));
    	XMLHelper.setXmlAttributeValue(doc, node, "version", "1");
    	XMLHelper.setXmlAttributeValue(doc, node, "visible", "true");    	
    	
    	XMLHelper.setXmlAttributeValue(doc, node, "lat", Double.toString(lat));
    	XMLHelper.setXmlAttributeValue(doc, node, "lon", Double.toString(lon));
    	
    	Element speedTag = XMLHelper.createXmlElement(doc, node, "tag");
    	XMLHelper.setXmlAttributeValue(doc, speedTag, "k", "maxspeed");
    	XMLHelper.setXmlAttributeValue(doc, speedTag, "v", Integer.toString(spd));
    	
    	debugView.setText("Set speed to: " + spd + " for pos " + lat + " " + lon);
    	
    	oldLat = lat;
    	oldLon = lon;
    }
    
    private void openFile()
    {
    	if(bw != null || fw != null) //a file is already open
			return;
    	
    	idCounter = 1;
    	
        try{
        	File root = Environment.getExternalStorageDirectory();
        	
        	if(root.canWrite())
        	{
        		Date d = new Date();
        		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");
        		
        		logFile = new File(root, "speed-"+sdf.format(d)+".osm");
        		
        		fw = new FileWriter(logFile);
        		
        		bw = new BufferedWriter(fw);        		
        	}
        	
        }
        catch (Exception e) {
        	debugView.setText("Open Exception: " + e.getMessage());
		}
        
        try {
			doc = XMLHelper.newDocument("osm");
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
        
    }
    
    private void closeFile()
    {
    	try
    	{
	    	if(bw != null)
	    	{
	    		if(doc != null)
	    			bw.write(XMLHelper.getXml(doc));
	    		
	    		bw.flush();
	    		bw.close();
	    	}
	    	
	    	if(fw != null)
	    	{	
	    		//fw.flush();
	    		fw.close();
	    	}
	    	
	    	bw = null;
	    	fw = null;
	    	idCounter = 1;
    	}
    	catch (Exception e)
    	{
    		debugView.setText("Close Exception: " + e.getMessage());
    	}
    }
    
    public void printValues(double lat, double lon)
    {
    	Log.e("Main:", lat + " " + lon);
    }
    
    public void storeGPSData(double lat, double lon)
    {
    	this.lat = lat;
    	this.lon = lon;
    }
    
    private class GPSReceiver extends BroadcastReceiver{

    	@Override
    	public void onReceive(Context ctx, Intent intent) {
    		double lat = intent.getDoubleExtra("latitude", -1);
    		double lon = intent.getDoubleExtra("longitude", -1);
    		
    		//Log.e("Rec", lat + " " + lon);    		
    		//printValues(lat, lon);
    		storeGPSData(lat, lon);
    	}	
    }
}