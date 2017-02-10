package mano.in.decibel;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by manoj on 29-Jan-17.
 */
public class TimerSettingsDialog extends AlertDialog{

    public TimerSettingsDialog(Context context) {
        super(context);
    }

    public void show() {
        buildDialog();
        super.show();
    }

    private void buildDialog() {
        setTitle(getContext().getString(R.string.dialog_heading_timer));
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.dialog_timer, null);
        final EditText et_dialog_timer = (EditText) customView.findViewById(R.id.et_dialog_timer);
        Button btn_save = (Button) customView.findViewById(R.id.btn_dialog_timer_save);
        Button btn_discard = (Button) customView.findViewById(R.id.btn_dialog_timer_discard);
        SharedPreferences sharedPref = getContext().getSharedPreferences(HomeActivity.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        et_dialog_timer.setText(String.valueOf(sharedPref.getInt(HomeActivity.SHARED_PREF_KEY_TIMER, 0)));
        setView(customView);
        setCancelable(false);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int timer = 0;
                String timerText = et_dialog_timer.getText().toString();
                if(!timerText.trim().equals(""))
                    timer = Integer.parseInt(String.valueOf(timerText));
                if (timer <= 0)
                    Toast.makeText(getContext(), getContext().getString(R.string.toast_settings_warn_timer_limit), Toast.LENGTH_SHORT).show();
                else {
                    try {
                        SharedPreferences sharedPref = getContext().getSharedPreferences(HomeActivity.SHARED_PREF_FILE, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt(HomeActivity.SHARED_PREF_KEY_TIMER, timer);
                        editor.commit();
                        Toast.makeText(getContext(), String.format(getContext().getString(R.string.toast_settings_msg_timer_success), timer), Toast.LENGTH_SHORT).show();
                        cancel();
                    } catch (Exception ex) {
                        Toast.makeText(getContext(), getContext().getString(R.string.toast_settings_err_timer_fail), Toast.LENGTH_SHORT).show();
                        cancel();
                    }
                }
            }
        });
        btn_discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
    }
}
