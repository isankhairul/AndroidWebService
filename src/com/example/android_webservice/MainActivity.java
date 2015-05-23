package com.example.android_webservice;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.view.Menu;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Created by
 * NIM : 41514120034
 * NAMA : Khairul Ihksan
 */

public class MainActivity extends Activity {
	
	private ArrayList<Barang> barangList ;
	private boolean success = false;
	//private
	MyCustomAdapter dataAdapter;
	private ProgressDialog pDialog;

	//from php
	private static final String TAG_SUKSES = "sukses";
	private static final String TAG_ERROR = "error";
	private static final String TAG_DATA = "data";
	private static final String TAG_ROWS = "total";
	private static final String TAG_ID = "barangId";
	private static final String TAG_NAMA = "nama";
	private static final String TAG_HARGA = "harga";

	// JSONArray data
	JSONArray data = null;
	JSONParser jParser = new JSONParser();
	
	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();
	
	// Session Manager Class
	SessionManager session;
	
	// Button Logout
	Button btnLogout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Session class instance
        session = new SessionManager(getApplicationContext());
        
		/**
         * Call this function whenever you want to check user login
         * This will redirect user to LoginActivity is he is not
         * logged in
         * */
        session.checkLogin();
		
        if(session.isLoggedIn() && isNetworkConnected()){
        	// Button logout
            btnLogout = (Button) findViewById(R.id.btnLogout);
            
            /**
             * Logout button click event
             * */
            btnLogout.setOnClickListener(new View.OnClickListener() {
    			
    			@Override
    			public void onClick(View arg0) {
    				// Clear the session data
    				// This will clear all session data and 
    				// redirect user to LoginActivity
    				session.logoutUser();
    			}
    		});
            
            Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();
           
    		barangList = new ArrayList<Barang>();
    		new AmbilDataJson().execute();
    		//create an ArrayAdaptar from the String Array
    		
    		dataAdapter = new MyCustomAdapter(this,
    		   R.layout.barang_row, barangList);
    		ListView listView = (ListView) findViewById(R.id.baranglistView);
    		// Assign adapter to ListView
    		listView.setAdapter(dataAdapter);
        }
        
	}
	
	private void displayBarang(){
	 //if the request was successful then notify the adapter to display the data
	 if(success){
		 dataAdapter.notifyDataSetChanged();
	 }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//custom array adapter to display our custom row layout for the listview
	class MyCustomAdapter extends ArrayAdapter<Barang> {

		 public MyCustomAdapter(Context context, int textViewResourceId, 
				 ArrayList<Barang> barangList) {
			 super(context, textViewResourceId, barangList);
		 }
	
		 
		private class ViewHolder {
			TextView barangId;
			TextView nama;
			TextView harga;
		 }
	
		 @Override
		 public View getView(int position, View convertView, ViewGroup parent) {
		
			  ViewHolder holder = null;
			  if (convertView == null) {
			
			   LayoutInflater vi = (LayoutInflater)getSystemService(
			     Context.LAYOUT_INFLATER_SERVICE);
			   convertView = vi.inflate(R.layout.barang_row, null);
			
			   holder = new ViewHolder();
			   holder.barangId = (TextView) convertView.findViewById(R.id.barangId);
			   holder.nama = (TextView) convertView.findViewById(R.id.nama);
			   holder.harga = (TextView) convertView.findViewById(R.id.harga);
			   convertView.setTag(holder);
		
		  } 
		  else {
			  holder = (ViewHolder) convertView.getTag();
		  }
	
		  Barang barang = barangList.get(position);
		  holder.barangId.setText(String.valueOf( barang.getBarangId() ));
		  holder.nama.setText(barang.getNama());
		  holder.harga.setText(String.valueOf( barang.getHarga() ));
	
		  return convertView;
	
		 }
	}
	
	class AmbilDataJson extends AsyncTask<String, String, String> {

		// inisialisasi url contact.php 
		private String url= "http://api-mobile.byethost12.com/barang/getBarang";
		
		int total;
	   	
		/**
		 * sebelum memulai background thread tampilkan Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Read data of barang...Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * mengambil semua data JSON barang dari url
		 * dan memasukan ke dalam list barang
		 * dilakukan secara background
		 * */
		protected String doInBackground(String... args) {
			// membangun Parameter
			
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();	
			
			//params.add(new BasicNameValuePair("param1", "nilai1"));
			//params.add(new BasicNameValuePair("param2", "nilai2"));
			params.add(new BasicNameValuePair("barangId", "1"));
			Log.d("params: ", params.toString());
			
			// ambil JSON string dari URL
			JSONObject json = jParser.makeHttpRequest(url, "GET", params);
			
			// cek log cat untuk JSON reponse
			Log.d("barang: ", json.toString());
			
			
			try {
				// mengecek untuk TAG SUKSES
				Log.d("total Barang: ",""+ json.getInt(TAG_ERROR));
				
				int error = json.getInt(TAG_ERROR);
				total = json.getInt(TAG_ROWS);
		
				
				if (error == 0) {
					Log.d("Barang: ",TAG_SUKSES);
					// data ditemukan
					// mengambil  Array dari barang
					
					data = json.getJSONArray(TAG_DATA);
					
					// looping data semua member/anggota
					for (int i = 0; i < data.length(); i++) {
						JSONObject b = data.getJSONObject(i);

						// tempatkan setiap item json di variabel
						int barangId	= Integer.valueOf( b.getString(TAG_ID) );
						String nama 	= b.getString(TAG_NAMA);
						int harga 		= Integer.valueOf( b.getString(TAG_HARGA) );

						barangList.add(new Barang(barangId,nama,harga));
					}
				} 
				else {
					Toast.makeText(getApplicationContext(), "No data", Toast.LENGTH_LONG).show();	
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return null;
		}

		/**
		 * setelah menyelesaikan background task hilangkan the progress dialog
		 * resfresh List view setelah data JSON diambil
		 * **/
		protected void onPostExecute(String file_url) {
			// hilangkan dialog setelah mendapatkan semua data member
			pDialog.dismiss();
			// update UI dari Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * update hasil parsing JSON ke ListView
					 * */
					
					if (total>1)
					{	
						success=true;
						displayBarang();
						//Toast.makeText(getApplicationContext(), "Successfully download JSON "+total+" Record(s)", Toast.LENGTH_LONG).show();
					}
					else {
						//Toast.makeText(getApplicationContext(), "Unsuccessfully download", Toast.LENGTH_LONG).show();
					}
				}
			});
		}
	}//endjson
	
	private boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
		    // There are no active networks.
			return false;
		} else
			return true;
	}
}

