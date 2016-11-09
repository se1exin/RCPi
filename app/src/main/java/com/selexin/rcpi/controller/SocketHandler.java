package com.selexin.rcpi.controller;

import java.io.*;
import java.net.*;

import com.selexin.rcpi.controller.RCSocket;
import com.selexin.rcpi.model.Motor;


import android.util.Log;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
public class SocketHandler extends AsyncTask<String, Integer, String> {
	private final static String TAG = "SocketHandler";
	private Motor motorFirst, motorSecond;
	private Context ctx;
	private String ipAddress;
	private int port;
	Handler toastHandler = new Handler();
	
	public SocketHandler(Context ctx, Motor motorFirst, Motor motorSecond) {
		this.ctx = ctx;
		this.motorFirst = motorFirst;
		this.motorSecond = motorSecond;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		prefs.registerOnSharedPreferenceChangeListener(
				new OnSharedPreferenceChangeListener() {
					
					@Override
					public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
							String key) {
						 if (key.equals("prefPort")) {
							 port = Integer.parseInt(sharedPreferences.getString("prefPort", "8888"));
				        }else if(key.equals("prefIpAddress")) {
				        	ipAddress = sharedPreferences.getString("prefIpAddress", "10.1.1.109");
				        }
						
					}
				}
			);
		this.ipAddress = prefs.getString("prefIpAddress", "10.1.1.109");
        this.port = Integer.parseInt(prefs.getString("prefPort", "8888"));
        
	}
	
	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}
	
	@Override
	protected String doInBackground(String... arg0) {
		// TODO Auto-generated method stub

		int lastMotorFirstSpeed = 0;
		int lastMotorSecondSpeed = 0;
		int lastMotorFirstDirection = 0;
		int lastMotorSecondDirection = 0;
		
		boolean on = true;
		try {
			RCSocket rcSocket = null;
			while(on) {
				Log.d(TAG, Integer.toString(motorFirst.getSpeed()));

				if(lastMotorFirstSpeed != motorFirst.getSpeed() ||
						lastMotorSecondSpeed != motorSecond.getSpeed() ||
						lastMotorFirstDirection != motorFirst.getIntDirection() ||
						lastMotorSecondDirection != motorSecond.getIntDirection()) {

					rcSocket = new RCSocket(ipAddress, port);
					Log.d(TAG, "M1 Speed Changed: " + Integer.toString(motorFirst.getSpeed()));

					rcSocket.sendValues(motorFirst.getSpeed(), motorSecond.getSpeed(), motorFirst.getIntDirection(), motorSecond.getIntDirection());

					lastMotorFirstSpeed = motorFirst.getSpeed();
					lastMotorSecondSpeed = motorSecond.getSpeed();
					lastMotorFirstDirection = motorFirst.getIntDirection();
					lastMotorSecondDirection = motorSecond.getIntDirection();
					rcSocket.close();
				}


				Thread.sleep(250);

			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block

			//Toast.makeText(rccontroller.this, "Unkown Host", Toast.LENGTH_LONG).show();
			ToastRunnable toastRunnable = new ToastRunnable(ctx, "Unknown Host!", Toast.LENGTH_LONG);
			toastHandler.post(toastRunnable);
			on = false;
			e.printStackTrace();
		} catch (IOException e) {
			//Toast.makeText(rccontroller.this, "IOException", Toast.LENGTH_LONG).show();
			// TODO Auto-generated catch block
			ToastRunnable toastRunnable = new ToastRunnable(ctx, "Could not connect to server!", Toast.LENGTH_LONG);
			toastHandler.post(toastRunnable);
			on = false;
			e.printStackTrace();
		} catch (Exception e) {
			ToastRunnable toastRunnable = new ToastRunnable(ctx, "Unknown Connection Failure!", Toast.LENGTH_LONG);
			toastHandler.post(toastRunnable);
			on = false;
			e.printStackTrace();
			//Toast.makeText(rccontroller.this, "Exception", Toast.LENGTH_LONG).show();
		}
		
		
		
		return null;
	}
	
	class ToastRunnable implements Runnable {
		private Context ctx;
		private String text;
		private int duration;
		
		public ToastRunnable(Context ctx, String text, int duration) {
			this.ctx = ctx;
			this.text = text;
			this.duration = duration;
		}
		@Override
		public void run() {
			Toast.makeText(ctx, text, duration).show();
		}
		
	}
	
}
