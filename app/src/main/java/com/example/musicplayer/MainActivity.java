package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    Button logoutBtn;
    FirebaseAuth auth;
    private boolean doubleBackToExitPressedOnce = false; // Flag to track double back press
    private static final int DOUBLE_BACK_PRESS_INTERVAL = 2000; // Time interval in milliseconds
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);

        logoutBtn = findViewById(R.id.logoutBtn);
        auth = FirebaseAuth.getInstance();

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

//      Asking for permission
        Dexter.withContext(this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

//                Toast.makeText(MainActivity.this, "Runtime permission given", Toast.LENGTH_SHORT).show();

//                Displaying the songs on the listview
                ArrayList<File> mySongs = fetchSongs(Environment.getExternalStorageDirectory());
                String [] items = new String[mySongs.size()];
                for(int i = 0;i<mySongs.size();i++){
                    items[i] = mySongs.get(i).getName().replace(".mp3", "");
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, items);
                listView.setAdapter(adapter);

//                Displaying the particular song on next activity
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        Intent intent = new Intent(MainActivity.this, PlaySong.class);
                        String currentSong = listView.getItemAtPosition(position).toString();
                        intent.putExtra("songList", mySongs);
                        intent.putExtra("currentSong", currentSong);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    }
                });


            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

            }
//            Asking for the permission again until granted
            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    private void logout() {
        auth.signOut();
        Intent intent = new Intent(MainActivity.this, SignUp.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity(); // Exit the app
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

        // Reset the flag after a certain time interval
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, DOUBLE_BACK_PRESS_INTERVAL);
    }

//    Accessing the songs from the storage directory
    public ArrayList<File> fetchSongs(File file){
        ArrayList arrayList = new ArrayList();
        File [] songs = file.listFiles();
        if(songs != null){
            for(File myFile: songs){
                if(!myFile.isHidden() && myFile.isDirectory()){
                    arrayList.addAll(fetchSongs(myFile));
                }
                else{
                    if(myFile.getName().endsWith(".mp3") && !myFile.getName().startsWith(".")){
                        arrayList.add(myFile);
                    }
                }
            }
        }
        return arrayList;

    }

}