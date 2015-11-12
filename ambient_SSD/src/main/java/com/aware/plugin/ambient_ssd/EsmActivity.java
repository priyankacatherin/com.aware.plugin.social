package com.aware.plugin.ambient_ssd;

/**
 * Created by admin on 9/8/2015.
 */


import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.aware.Aware;
import com.aware.Aware_Preferences;

import java.util.ArrayList;

public class EsmActivity extends Activity {

    private RadioGroup radioSocialGroup;
    private RadioGroup radioconversationgroup;
    private RadioButton radioSocialButton;
    private RadioButton radioSocialButton2;
    private TextView Time;
    ArrayList<String> selectedItem = new ArrayList<>();

    private Button btnDisplay;
    String situation;
    public static String unix_time;
    public static String split[];
    String final_text;
    static String timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        situation = "";
        Time = (TextView) findViewById(R.id.time);
        split = timestamp.split("split");
        timestamp = split[0];
        System.out.println("Time:" + split[0]);
        System.out.println("Time:" + split[1]);
        unix_time = split[1];
        System.out.println("UTIME" + unix_time);
        Time.setText(timestamp);
        addListenerOnButton();


    }

    public void selectItem(View view) {

        boolean checked = ((CheckBox) view).isChecked();
        switch (view.getId()) {
            case R.id.a2:
                if (checked)
                    selectedItem.add("Am in a Conversation");
                else {
                    selectedItem.remove("Am in a Conversation");
                }
                break;
            case R.id.b2:
                if (checked)
                    selectedItem.add("Nobody is talking");
                else {
                    selectedItem.remove("Nobody is talking");
                }
                break;
            case R.id.c2:
                if (checked)
                    selectedItem.add("Other people around me are talking");
                else {
                    selectedItem.remove("Other people around me are talking");
                }
                break;
            case R.id.d2:
                if (checked)
                    selectedItem.add("Noise from the environment");
                else {
                    selectedItem.remove("Noise from the environment");
                }
                break;
        }
        String final_selection = "";
        for (String Selections : selectedItem) {
            final_selection = final_selection + Selections + ";";
        }

        final_text = final_selection;
    }





    public void addListenerOnButton() {

        radioSocialGroup = (RadioGroup) findViewById(R.id.radioSocial);

        btnDisplay = (Button) findViewById(R.id.btnDisplay);

        btnDisplay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // get selected radio button from radioGroup
                int selectedId = radioSocialGroup.getCheckedRadioButtonId();


                // find the radiobutton by returned id
                radioSocialButton = (RadioButton) findViewById(selectedId);


                Toast.makeText(EsmActivity.this,
                        radioSocialButton.getText(), Toast.LENGTH_SHORT).show();

                Toast.makeText(EsmActivity.this,
                        final_text, Toast.LENGTH_SHORT).show();


                String radioval= String.valueOf(radioSocialButton.getText());



                ContentValues new_data = new ContentValues();
                new_data.put(EsmProvider.Esm_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                new_data.put(EsmProvider.Esm_Data.TIMESTAMP,unix_time);
                // new_data.put(EsmProvider.Esm_Data.EMAIL, true);
                new_data.put(EsmProvider.Esm_Data.LOCATION, String.valueOf(radioSocialButton.getText()));
                new_data.put(EsmProvider.Esm_Data.SITUATION,final_text.toString());
                getContentResolver().insert(EsmProvider.Esm_Data.CONTENT_URI, new_data);
                //  Toast.makeText(getApplicationContext(), "You have been saved!", Toast.LENGTH_LONG).show();

              //  System.exit(0);
               // android.os.Process.killProcess(android.os.Process.myPid());
                Intent calendar_intent = new Intent(getApplicationContext(),CalendarActivity.class);
                calendar_intent.putExtra("Radio Value", radioval);
                calendar_intent.putExtra("Check Con", final_text);
                startActivity(calendar_intent);


                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancel(0);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
              //  finish();


            }

        });

    }
}




