package com.example.android_webservice;

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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/*
 * Created by
 * NIM : 41514120034
 * NAMA : Khairul Ihksan
 */

public class LoginActivity extends Activity {

	private static final String TAG_ERROR = "error";
	private static final String TAG_DATA = "data";
	private static final String TAG_ROWS = "total";
	
	private boolean success = false;
	private ProgressDialog pDialog;
	
	// JSONArray data
	JSONArray data = null;
	JSONParser jParser = new JSONParser();
	
	// Email, password edittext
	EditText txtUsername, txtPassword;
	
	// login button
	Button btnLogin;
	
	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();
	
	// Session Manager Class
	SessionManager session;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		// Session Manager
        session = new SessionManager(getApplicationContext());                
        
        // Email, Password input text
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword); 
        
        Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();
        
        
        // Login button
        btnLogin = (Button) findViewById(R.id.btnLogin);
        
        
        // Login button click event
        btnLogin.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// Get username, password from EditText
				String username = txtUsername.getText().toString();
				String password = txtPassword.getText().toString();
				
				// Check if username, password is filled				
				if(username.trim().length() > 0 && password.trim().length() > 0){
					// For testing puspose username, password is checked with sample data
					
					if(isNetworkConnected()){
						// validate login
						new ValidateLoginAsyncTask().execute();
					}
					else{
						alert.showAlertDialog(LoginActivity.this, "No Internet connection.", "You have no internet connection", false);
					}
					
				}else{
					// user didn't entered username or password
					// Show alert asking him to enter the details
					alert.showAlertDialog(LoginActivity.this, "Login failed..", "Please enter username and password", false);
				}
				
			}
		});
		
	}
	
	class ValidateLoginAsyncTask extends AsyncTask<String, String, String> {

		// inisialisasi url
		private String url= "http://api-mobile.byethost12.com/backend/login";
		int error;
		/**
		 * sebelum memulai background thread tampilkan Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(LoginActivity.this);
			pDialog.setMessage("Please wait...");
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
			
			// Get username, password from EditText
			String username = txtUsername.getText().toString();
			String password = txtPassword.getText().toString();
			
			params.add(new BasicNameValuePair("username", username));
			params.add(new BasicNameValuePair("password", password));
			Log.d("params: ", params.toString());
			
			try {
				// ambil JSON string dari URL
				JSONObject json = jParser.makeHttpRequest(url, "POST", params);
				
				// cek log cat untuk JSON reponse
				Log.d("data: ", json.toString());
			
				// mengecek untuk TAG SUKSES
				Log.d("error data: ",""+ json.getInt(TAG_ERROR));
				
				error = json.getInt(TAG_ERROR);
		
				if (error == 0) {
					// data ditemukan
					// mengambil  Array dari barang
					
					JSONObject data = json.getJSONObject(TAG_DATA);
					
					Log.d("data: ",""+ data);
					Log.d("get data element: ",""+ data.getString("username") + " " + data.getString("email"));
					
					// tempatkan setiap item json di variabel
					int userId			= Integer.valueOf( data.getString("userId") );
					String username1	= data.getString("username");
					String email 		= data.getString("email");
					new Users(userId, username1, email);
					
					// Creating user login session
					// For testing i am stroing name, email as follow
					// Use user real data
					session.createLoginSession(data.getString("username"), data.getString("email"));
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
					 * update hasil parsing JSON
					 * */
					if (error == 0)
					{	
						// username ok
						alert.showAlertDialog(LoginActivity.this, "Login success..", "Username OK", true);
						
						// Startring MainActivity
						Intent i = new Intent(getApplicationContext(), MainActivity.class);
						startActivity(i);
						finish();
					}
					else{
						// username / password doesn't match
						alert.showAlertDialog(LoginActivity.this, "Login failed..", "Username/Password is incorrect", false);
					}
				}
			});
		}
	}//endjson
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
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
