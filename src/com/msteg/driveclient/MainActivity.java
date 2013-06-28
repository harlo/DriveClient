package com.msteg.driveclient;

import java.io.IOException;
import java.util.Arrays;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	String file_name = "fordrive.jpg";
	String path_to_file = Environment.getExternalStorageDirectory().getAbsolutePath() + "/InformaCam/" + file_name;
	String drive_folder = "0B07iVinFhZgqa2tTZXhQdXNXYlk";
	String mime_type = "image/jpeg";
	
	Button send;
	TextView readout, conf;
	
	Drive service;
	GoogleAccountCredential credentials;
		
	private static final int REQUEST_ACCOUNT_PICKER = 1;
	private static final int REQUEST_AUTHORIZATION = 2;
	
	private final static String LOG = "************************** DriveClient Test **************************";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.mainactivity);
		
		send = (Button) findViewById(R.id.send);
		send.setOnClickListener(this);
		
		conf = (TextView) findViewById(R.id.conf);
		conf.setText("Conf:\npath to file: " + path_to_file + "\ndrive folder:" + drive_folder);
		
		readout = (TextView) findViewById(R.id.readout);
		readout.setText("READOUT:");
		
		init();
	}
	
	private void init() {
		credentials = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE));
		startActivityForResult(credentials.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
	}
	
	private void sendToDrive() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				File file = new File();
				file.setTitle(file_name);
				file.setMimeType(mime_type);
				file.setParents(Arrays.asList(new ParentReference().setId(drive_folder)));
				
				java.io.File file_content = new java.io.File(path_to_file);
				FileContent media_content = new FileContent(mime_type, file_content);
				
				try {
					File resulting_file = service.files().insert(file, media_content).execute();
					Log.d(LOG, "AWESOME SAUCE:\n" + resulting_file.toPrettyString());
					
				} catch(UserRecoverableAuthIOException e) {
					Log.e(LOG, "AUTH IO ExCEPTION!");
					Log.e(LOG, e.toString());
					e.printStackTrace();
					
					Log.d(LOG, e.getIntent().toUri(0));
					startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
				} catch (IOException e) {
					Log.e(LOG, "REGULAR IO Exception");
					Log.e(LOG, e.toString());
					e.printStackTrace();
				}
			}
		}).start();
		
	}
	
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch(requestCode) {
		case REQUEST_ACCOUNT_PICKER:
			if(resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
				String account_name = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				if(account_name != null) {
					credentials.setSelectedAccountName(account_name);
					service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credentials).build();
					Log.d(LOG, "SERVICE INITIATED!");
				}
			}
			break;
		case REQUEST_AUTHORIZATION:
			if(resultCode == Activity.RESULT_OK) {
				Log.d(LOG, "REQUEST AUTH OK");
			} else {
				Log.d(LOG, "BAD AUTH. REQUESTING AUTH...");
				init();
			}
			
		}
	}

	@Override
	public void onClick(View v) {
		sendToDrive();
		
	}
}
