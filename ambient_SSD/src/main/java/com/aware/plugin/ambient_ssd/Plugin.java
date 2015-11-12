package com.aware.plugin.ambient_ssd;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.ambient_ssd.Provider.AmbientNoise_Data;
import com.aware.utils.Aware_Plugin;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Plugin extends Aware_Plugin {
static String formattedDate;
    /**
     * Broadcasted with sound frequency value <br/>
     * Extra: sound_frequency, in Hz
     */
    public static final String ACTION_AWARE_PLUGIN_AMBIENT_SSD = "ACTION_AWARE_PLUGIN_AMBIENT_SSD";

    /**
     * Extra for ACTION_AWARE_PLUGIN_AMBIENT_NOISE
     * (double) sound frequency in Hz
     */
    public static final String EXTRA_SOUND_FREQUENCY = "sound_frequency";

    /**
     * Extra for ACTION_AWARE_PLUGIN_AMBIENT_NOISE
     * (boolean) true/false if silent
     */
    public static final String EXTRA_IS_SILENT = "is_silent";

    /**
     * Extra for ACTION_AWARE_PLUGIN_AMBIENT_NOISE
     * (double) sound noise in dB
     */
    public static final String EXTRA_SOUND_DB = "sound_db";

    /**
     * Extra for ACTION_AWARE_PLUGIN_AMBIENT_NOISE
     * (double) sound RMS
     */
    public static final String EXTRA_SOUND_RMS = "sound_rms";

    //AWARE context producer
    public static ContextProducer context_producer;

    private AlarmManager alarmManager;
    private PendingIntent audioTask;

    @Override
    public void onCreate() {
        super.onCreate();
        //  Intent ab = new Intent(this,Sound.class);
        //  startActivity(ab);
        TAG = "AWARE::Ambient SSD";
        DEBUG = true;

        Aware.setSetting(getApplicationContext(), Settings.STATUS_PLUGIN_AMBIENT_SSD, true);

        if (Aware.getSetting(getApplicationContext(), Settings.FREQUENCY_PLUGIN_AMBIENT_SSD).length() == 0) {
            Aware.setSetting(getApplicationContext(), Settings.FREQUENCY_PLUGIN_AMBIENT_SSD, 5);
        }
        if (Aware.getSetting(getApplicationContext(), Settings.PLUGIN_AMBIENT_SSD_SILENCE_THRESHOLD).length() == 0) {
            Aware.setSetting(getApplicationContext(), Settings.PLUGIN_AMBIENT_SSD_SILENCE_THRESHOLD, 50);
        }
        if (Aware.getSetting(getApplicationContext(), Settings.PLUGIN_AMBIENT_SSD_SAMPLE_SIZE).length() == 0) {
            Aware.setSetting(getApplicationContext(), Settings.PLUGIN_AMBIENT_SSD_SAMPLE_SIZE, 30);
        }

        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
                Intent context_ambient_ssd = new Intent();
                context_ambient_ssd.setAction(ACTION_AWARE_PLUGIN_AMBIENT_SSD);
                context_ambient_ssd.putExtra(EXTRA_SOUND_FREQUENCY, AudioAnalyser.sound_frequency);
                context_ambient_ssd.putExtra(EXTRA_SOUND_DB, AudioAnalyser.sound_db);
                context_ambient_ssd.putExtra(EXTRA_SOUND_RMS, AudioAnalyser.sound_rms);
                context_ambient_ssd.putExtra(EXTRA_IS_SILENT, AudioAnalyser.is_silent);
                sendBroadcast(context_ambient_ssd);
            }
        };
        context_producer = CONTEXT_PRODUCER;

        DATABASE_TABLES = Provider.DATABASE_TABLES;
        TABLES_FIELDS = Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{AmbientNoise_Data.CONTENT_URI};

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent audioIntent = new Intent(this, AudioAnalyser.class);
        audioTask = PendingIntent.getService(getApplicationContext(), 0, audioIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, Integer.parseInt(Aware.getSetting(this, Settings.FREQUENCY_PLUGIN_AMBIENT_SSD)) * 60 * 1000, audioTask);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        alarmManager.cancel(audioTask);
        Aware.setSetting(getApplicationContext(), Settings.STATUS_PLUGIN_AMBIENT_SSD, false);
        //sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
    }

    public static class AudioAnalyser extends IntentService {
        public static double sound_frequency;
        public static double sound_db;
        public static boolean is_silent;
        public static double sound_rms;

        public AudioAnalyser() {
            super(TAG);
        }
       // @TargetApi(Build.VERSION_CODES.JELLY_BEAN
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onHandleIntent(Intent intent) {
            //Get minimum size of the buffer for pre-determined audio setup and minutes
            int buffer_size = AudioRecord.getMinBufferSize(AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_SYSTEM), AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 10;

            //Initialize audio recorder. Use MediaRecorder.AudioSource.VOICE_RECOGNITION to disable Automated Voice Gain from microphone and use DSP if available
            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_SYSTEM), AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer_size);

            //Audio data buffer
            short[] audio_data = new short[buffer_size];

            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
                    recorder.startRecording();
                }

                double now = System.currentTimeMillis();
                double elapsed = 0;
                while (elapsed < Integer.parseInt(Aware.getSetting(getApplicationContext(), Settings.PLUGIN_AMBIENT_SSD_SAMPLE_SIZE)) * 1000) {
                    //wait...
                    elapsed = System.currentTimeMillis() - now;
                    Log.d(TAG, "Recording... " + elapsed / 1000 + " seconds");
                }

                recorder.read(audio_data, 0, buffer_size);

                AudioAnalysis audio_analysis = new AudioAnalysis(getApplicationContext(), audio_data, buffer_size);

                sound_rms = audio_analysis.getRMS();
                is_silent = audio_analysis.isSilent(sound_rms);
                sound_frequency = audio_analysis.getFrequency();
                sound_db = audio_analysis.getdB();
                // ((Sound)getActivity()).yourPublicMethod();
                //  Intent ab = new Intent(this,Sound.class);
                //  startActivity(ab);
                //  public static class EsmActivity extends Activity {
                //     public static boolean is_silent;
                //is_silent = audio_analysis.isSilent(sound_rms);

                if (is_silent == false)


                {
                    //  setContentView(R.layout.activity_main);
                    //addListenerOnButton();
                    //Intent ab = new Intent(this, EsmActivity.class);
                    //startActivity(ab);
                   // Intent intent = new Intent();
                    // PendingIntent pIntent = PendingIntent.getActivity(this,0,intent,0);
                    // TaskStackBuilder stackBuilder = null;
                    Intent resultIntent = new Intent(this,EsmActivity.class);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addParentStack(EsmActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    Notification noti = new Notification.Builder(this)
                            // NotificationCompat.Builder noti = new NotificationCompat.Builder(
                            //       this)
                            // NotificationCompat.Builder builder = new NotificationCompat.Builder(
                            //  this);
                            // notification = builder.setContentIntent(contentIntent)
                            .setTicker("Social Situation")
                            .setContentTitle("Social Situation")
                            .setContentText("Describe your social situation.")
                            .setSmallIcon(R.drawable.social_situations_1_challenge)
                            .setContentIntent(resultPendingIntent).getNotification();




                    //   noti.flags = Notification.FLAG_AUTO_CANCEL;
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    // mNM.notify(NOTIFICATION, notification)
                    notificationManager.notify(0, noti);
                    Calendar c = Calendar.getInstance();
                    System.out.println("Current time =&gt; " + c.getTime());
                    String time= String.valueOf(c.getTime());
                    System.out.println("Current time =&gt; " + time);


                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    formattedDate = df.format(c.getTime());
// Now formattedDate have current date/time
                    //Toast.makeText(this, formattedDate, Toast.LENGTH_SHORT).show();

                    Long tsLong = System.currentTimeMillis()/1000;
                    formattedDate +="split"+tsLong.toString();


                }
            }
             /*  else
                {
                    Intent abs = new Intent(this,Esm2.class);
                    startActivity(abs);
                }*/
            ContentValues data = new ContentValues();
            data.put(AmbientNoise_Data.TIMESTAMP, System.currentTimeMillis());
            data.put(AmbientNoise_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
            data.put(AmbientNoise_Data.FREQUENCY, sound_frequency);
            data.put(AmbientNoise_Data.DECIBELS, sound_db);
            data.put(AmbientNoise_Data.RMS, sound_rms);
            data.put(AmbientNoise_Data.IS_SILENT, is_silent);
            data.put(AmbientNoise_Data.RAW, String.valueOf(audio_data));
            data.put(AmbientNoise_Data.SILENCE_THRESHOLD, Aware.getSetting(getApplicationContext(), Settings.PLUGIN_AMBIENT_SSD_SILENCE_THRESHOLD));

            getContentResolver().insert(AmbientNoise_Data.CONTENT_URI, data);

            //Share context
            context_producer.onContext();

        }
    }
}

      /*  else { //recorder is busy right now, let's wait 30 seconds before we try again
                Log.d(TAG,"Recorder is busy at the moment...");
            }

            //Release microphone and stop recording
            recorder.stop();
            recorder.release();
        }
    }*/

