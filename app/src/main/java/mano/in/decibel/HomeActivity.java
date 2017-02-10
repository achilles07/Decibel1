package mano.in.decibel;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Button btn_home_start, btn_home_stop = null;
    private Intent loudnessListenerServiceIntent = null;
    private static int REQUEST_CODE_CONTACT_PICK = 1001;

    public static String SHARED_PREF_KEY_CONTACT = "DEFAULT_CONTACT_NUMBER";
    public static String SHARED_PREF_KEY_TIMER = "DEFAULT_COUNTDOWN_TIMER";
    public static String SHARED_PREF_KEY_AMP = "DEFAULT_AMP_THRESHOLD";
    public static String SHARED_PREF_FILE = "DECIBEL_PREF_FILE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        loudnessListenerServiceIntent = new Intent(HomeActivity.this, LoudnessListenerService.class);
        btn_home_start = (Button) findViewById(R.id.btn_home_start);
        btn_home_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(loudnessListenerServiceIntent);
                startService(loudnessListenerServiceIntent);
            }
        });
        btn_home_stop = (Button) findViewById(R.id.btn_home_stop);
        btn_home_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(loudnessListenerServiceIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.menu_settings_image) {

        } else if (id == R.id.menu_settings_contact) {
            chooseContact();
        } else if (id == R.id.menu_settings_timer) {
            new TimerSettingsDialog(this).show();
        } else if (id == R.id.menu_settings_loudness) {
            new LoudnessSettingsDialog(this).show();
        } else if (id == R.id.menu_action_restart) {
            stopService(loudnessListenerServiceIntent);
            startService(loudnessListenerServiceIntent);
        } else if (id == R.id.menu_action_stop) {
            stopService(loudnessListenerServiceIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void chooseContact() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, REQUEST_CODE_CONTACT_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONTACT_PICK && resultCode == RESULT_OK) {
            try {
                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                Cursor cursor = getContentResolver().query(data.getData(), projection, null, null, null);
                cursor.moveToFirst();
                int columnNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int columnName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String number = cursor.getString(columnNumber);
                String name = cursor.getString(columnName);
                SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(SHARED_PREF_KEY_CONTACT, number);
                editor.commit();
                Toast.makeText(this, String.format(getString(R.string.toast_home_settings_contact_msg_success), name, number), Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                Toast.makeText(this, getString(R.string.toast_home_settings_contact_err_fail), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
