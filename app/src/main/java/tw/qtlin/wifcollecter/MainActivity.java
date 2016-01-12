package tw.qtlin.wifcollecter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private LocationManager lms;
    private String bestProvider = LocationManager.GPS_PROVIDER;
    private WifiManager m_wifiManager;
    private String m_wifi_msg = "";
    private ListView m_list;

    private static String getOutputMediaFilePath() {
        //make a new file directory inside the "sdcard" folder
        File mediaStorageDir = new File("/sdcard/", "myWifis");

        //if this "JCGCamera folder does not exist
        if (!mediaStorageDir.exists()) {
            //if you cannot make this folder return
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        //take the current timeStamp
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //File mediaFile;
        //and make a media file:
        //mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaStorageDir.getPath() + File.separator;
    }
    private void locationServiceInitial() {
        lms = (LocationManager) getSystemService(LOCATION_SERVICE);	//取得系統定位服務
        Location location = lms.getLastKnownLocation(LocationManager.GPS_PROVIDER);	//使用GPS定位座標
        getLocation(location);
    }
    private void getLocation(Location location) {	//將定位資訊顯示在畫面中
        if(location != null) {

            Double longitude = location.getLongitude();	//取得經度
            Double latitude = location.getLatitude();	//取得緯度
        }
        else {
            Toast.makeText(this, "無法定位座標", Toast.LENGTH_LONG).show();
        }
    }
    private ArrayList<String> scanDir()
    {
        String path = getOutputMediaFilePath();
        File f = new File(path);
        File file[] = f.listFiles();
        final ArrayList<String> list = new ArrayList<String>();
        for (int i=0; i < file.length; i++)
        {
            list.add(file[i].getName());
        }
        return  list;
    }
    private void initalListView() {

        updateListView();

        m_list.setLongClickable(true);
        m_list.setFocusable(false);
        m_list.setFocusableInTouchMode(false);

        m_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);

                String filePath = getOutputMediaFilePath() + item;
                String content = "";
                try {

                    BufferedReader br = new BufferedReader(new FileReader(filePath));
                    String line;

                    while ((line = br.readLine()) != null) {
                        content += (line);
                        content += ("\n\n");
                    }
                    br.close();
                } catch (IOException e) {
                    //You'll need to add proper error handling here
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(content);
                builder.setTitle(item);
                builder.setPositiveButton("關閉", null);
                builder.create().show();

            }

        });

        m_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v,
                                           int position, long id) {

                final String item = (String) parent.getItemAtPosition(position);
                final String m_del_filePath = getOutputMediaFilePath() + item;
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setTitle("確認");
                b.setMessage("確定刪除 " + item + "？");
                b.setCancelable(true);
                b.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean deleted = false;
                        File myFile = new File(m_del_filePath);
                        if(myFile.exists())
                            deleted = myFile.delete();
                        if (deleted)
                            Toast.makeText(MainActivity.this, "Delete success", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(MainActivity.this, "Delete fail", Toast.LENGTH_SHORT).show();
                        updateListView();
                    }
                });
                b.setNegativeButton("取消", null);
                b.create().show();
                return true;
            }
        });
    }
    private void updateListView(){
        StableArrayAdapter adapter = new StableArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, scanDir());
        m_list.setAdapter(adapter);
        ((BaseAdapter) m_list.getAdapter()).notifyDataSetChanged();

    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        m_list = (ListView)findViewById(R.id.listView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //List<WifiConfiguration> arraylist = m_wifiManager.getConfiguredNetworks();
                //WifiInfo wifiInfo = m_wifiManager.getConnectionInfo();

                List<ScanResult> arraylist = m_wifiManager.getScanResults();
                m_wifi_msg = "";
                for(int i = 0; i< arraylist.size(); i++)
                {
                    ScanResult a = arraylist.get(i);
                    m_wifi_msg+=a.BSSID;
                    m_wifi_msg+=", "+a.level;
                    m_wifi_msg+=", "+a.SSID;
                    m_wifi_msg+="\n";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Location");
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String location = input.getText().toString();
                        String filePath = getOutputMediaFilePath()+location + ".csv";
                        try{
                            FileWriter fw = new FileWriter(filePath, false);
                            BufferedWriter bw = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結
                            bw.write(m_wifi_msg);
                            bw.close();
                            updateListView();
                            Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();


               // Snackbar.make(view, "Wifi:"+ msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });



        LocationManager status = (LocationManager) (this.getSystemService(Context.LOCATION_SERVICE));
        if (status.isProviderEnabled(LocationManager.GPS_PROVIDER) || status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //如果GPS或網路定位開啟，呼叫locationServiceInitial()更新位置
            locationServiceInitial();
        } else {
            Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));	//開啟設定頁面
        }
        initalListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
