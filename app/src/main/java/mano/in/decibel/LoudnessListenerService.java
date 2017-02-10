package mano.in.decibel;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class LoudnessListenerService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    public LoudnessListenerService() {
    }

    private CountDownTimer timer = null;
    private MediaRecorder recorder = null;
    private Intent callIntent = null;
    private PhoneCallListener phoneCallListener = null;
    private String contactNumber = null;
    private int timerInterval = 0;
    private int ampThreshold = 0;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initPhoneCallListener();
        initSharedPreferences();
        initCallIntent();
        if (areParamsOK()) {
            initRecorder();
            initCountdownTimer();
            startListening();
            Toast.makeText(LoudnessListenerService.this, getString(R.string.toast_lls_msg_service_start), Toast.LENGTH_SHORT).show();
        } else {
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_NO_CREATE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
        notificationBuilder.setSmallIcon(android.R.drawable.ic_secure);
        notificationBuilder.setContentText(getString(R.string.notification_content));
        notificationBuilder.setContentIntent(pendingIntent);
        Notification notification = notificationBuilder.build();
        startForeground(1, notification);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.e("decibel", "service is destroying");
        stopListening();
        Toast.makeText(LoudnessListenerService.this, getString(R.string.toast_lls_destroy), Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    private void initPhoneCallListener() {
        phoneCallListener = new PhoneCallListener();
        TelephonyManager telMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telMgr.listen(phoneCallListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void initSharedPreferences() {
        SharedPreferences sharedPref = getSharedPreferences(HomeActivity.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        contactNumber = sharedPref.getString(HomeActivity.SHARED_PREF_KEY_CONTACT, null);
        timerInterval = sharedPref.getInt(HomeActivity.SHARED_PREF_KEY_TIMER, 0) * 1000;
        ampThreshold = sharedPref.getInt(HomeActivity.SHARED_PREF_KEY_AMP, 0);
    }

    private void initCallIntent() {
        callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        callIntent.setPackage("com.android.phone");
    }

    private void initRecorder() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile("/dev/null");
        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean areParamsOK() {
        boolean ok = true;
        if (timerInterval <= 0) {
            Toast.makeText(LoudnessListenerService.this, getString(R.string.toast_lls_err_timer), Toast.LENGTH_SHORT).show();
            ok = false;
        } else if (contactNumber == null) {
            Toast.makeText(LoudnessListenerService.this, getString(R.string.toast_lls_err_contact), Toast.LENGTH_SHORT).show();
            ok = false;
        } else if (ampThreshold <= 0) {
            Toast.makeText(LoudnessListenerService.this, getString(R.string.toast_lls_err_threshold), Toast.LENGTH_SHORT).show();
            ok = false;
        }
        return ok;
    }

    private void startListening() {
        if (recorder != null) {
            recorder.start();
        }
        if (timer != null) {
            timer.start();
        }
    }

    private void stopListening() {
        if (timer != null) {
            timer.cancel();
        }
        if (recorder != null) {
            recorder.stop();
            recorder.release();
        }
    }

    private void initCountdownTimer() {
        timer = new CountDownTimer(timerInterval, timerInterval) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                Log.e("decibel", "countdown timer finished");
                if (!areParamsOK()) {
                    stopSelf();
                    return;
                }
                if (recorder.getMaxAmplitude() > ampThreshold) {
                    if (ContextCompat.checkSelfPermission(LoudnessListenerService.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(LoudnessListenerService.this, String.format(getString(R.string.toast_lls_calling_contact), contactNumber), Toast.LENGTH_SHORT).show();
                        callIntent.setData(Uri.parse("tel:" + contactNumber));
                        if (!phoneCallListener.isCalledFromHere()) {
                            Log.e("decibel", "starting phone call activity");
                            phoneCallListener.setCalledFromHere(true);
                            startActivity(callIntent);
                        }
                    } else {
                        Toast.makeText(LoudnessListenerService.this, R.string.toast_home_call_no_permission, Toast.LENGTH_SHORT).show();
                    }
                }
                this.start();
            }
        };
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(HomeActivity.SHARED_PREF_KEY_CONTACT))
            contactNumber = sharedPreferences.getString(HomeActivity.SHARED_PREF_KEY_CONTACT, null);
        else if (key.equals(HomeActivity.SHARED_PREF_KEY_TIMER)) {
            timerInterval = sharedPreferences.getInt(HomeActivity.SHARED_PREF_KEY_CONTACT, 0);
            timer.cancel();
            initCountdownTimer();
            timer.start();
        } else if (key.equals(HomeActivity.SHARED_PREF_KEY_AMP))
            ampThreshold = sharedPreferences.getInt(HomeActivity.SHARED_PREF_KEY_AMP, 0);
    }

    private class PhoneCallListener extends PhoneStateListener {
        private boolean calledFromHere = false;
        private boolean isPhoneCalling = false;

        public void setCalledFromHere(boolean flag) {
            this.calledFromHere = flag;
        }

        public boolean isCalledFromHere() {
            return this.calledFromHere;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (TelephonyManager.CALL_STATE_OFFHOOK == state && this.calledFromHere) {
                this.isPhoneCalling = true;
                Log.v("decibel", "inside call state changed - offhook");
            } else if (TelephonyManager.CALL_STATE_IDLE == state && isPhoneCalling) {
                Log.v("decibel", "inside call state changed - idle");
                this.calledFromHere = false;
                this.isPhoneCalling = false;
            }
        }
    }
}
