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
 * Created by manoj on 03-Feb-17.
 */
public class LoudnessSettingsDialog extends AlertDialog{

    public static final int MAX_LOUDNESS_THRESHOLD = 32767;

    protected LoudnessSettingsDialog(Context context) {
        super(context);
    }

    public void show() {
        buildDialog();
        super.show();
    }

    private void buildDialog() {
        setTitle(getContext().getString(R.string.dialog_heading_loudness));
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.dialog_loudness, null);
        final EditText et_dialog_loudness = (EditText) customView.findViewById(R.id.et_dialog_loudness);
        Button btn_save = (Button) customView.findViewById(R.id.btn_dialog_loudness_save);
        Button btn_discard = (Button) customView.findViewById(R.id.btn_dialog_loudness_discard);
        SharedPreferences sharedPref = getContext().getSharedPreferences(HomeActivity.SHARED_PREF_FILE, Context.MODE_PRIVATE);
        et_dialog_loudness.setText(String.valueOf(sharedPref.getInt(HomeActivity.SHARED_PREF_KEY_AMP, 0)));
        setView(customView);
        setCancelable(false);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int threshold = 0;
                String thresholdText = et_dialog_loudness.getText().toString();
                if(!thresholdText.trim().equals(""))
                    threshold = Integer.parseInt(String.valueOf(thresholdText));
                if (threshold <= 0 || threshold > MAX_LOUDNESS_THRESHOLD)
                    Toast.makeText(getContext(), String.format(getContext().getString(R.string.toast_settings_warn_loudness_level),MAX_LOUDNESS_THRESHOLD), Toast.LENGTH_SHORT).show();
                else {
                    try {
                        SharedPreferences sharedPref = getContext().getSharedPreferences(HomeActivity.SHARED_PREF_FILE, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt(HomeActivity.SHARED_PREF_KEY_AMP, threshold);
                        editor.commit();
                        Toast.makeText(getContext(), String.format(getContext().getString(R.string.toast_settings_msg_loudness_success),threshold), Toast.LENGTH_SHORT).show();
                        cancel();
                    } catch (Exception ex) {
                        Toast.makeText(getContext(), getContext().getString(R.string.toast_settings_err_loudness_fail), Toast.LENGTH_SHORT).show();
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
