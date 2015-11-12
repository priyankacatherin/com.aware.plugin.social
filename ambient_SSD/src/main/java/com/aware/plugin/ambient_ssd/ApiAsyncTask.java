package com.aware.plugin.ambient_ssd;

import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An asynchronous task that handles the Google Calendar API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
public class ApiAsyncTask extends AsyncTask<Void, Void, Void> {
    private CalendarActivity mActivity;
    String tval;

    /**
     * Constructor.
     *
     * @param activity MainActivity that spawned this task.
     */
    ApiAsyncTask(CalendarActivity activity) {
        this.mActivity = activity;
    }

    /**
     * Background task to call Google Calendar API.
     *
     * @param params no parameters needed for this task.
     */

    @Override
    protected Void doInBackground(Void... params) {
        try {
            mActivity.clearResultsText();
            mActivity.updateResultsText(getDataFromApi());
            //  writeToCalendar();


        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
            mActivity.showGooglePlayServicesAvailabilityErrorDialog(
                    availabilityException.getConnectionStatusCode());

        } catch (UserRecoverableAuthIOException userRecoverableException) {
            mActivity.startActivityForResult(
                    userRecoverableException.getIntent(),
                    CalendarActivity.REQUEST_AUTHORIZATION);

        } catch (Exception e) {
            mActivity.updateStatus("The following error occurred:\n" +
                    e.getMessage());
        }
        return null;
    }

    /**
     * Fetch a list of the next 10 events from the primary calendar.
     *
     * @return List of Strings describing returned events.
     * @throws IOException
     */

    private List<String> getDataFromApi() throws IOException {


      // String[] tableColumns = new String[2];
       // tableColumns[0] = SocialEsmProvider.Esm_Data.TIMESTAMP;
        //tableColumns[1] = SocialEsmProvider.Esm_Data.SOCIALSITUATION;


     //   Cursor esm_data = mActivity.esm_data;
       //esm_data.moveToFirst();
        String radioval= mActivity.radioval;
        String final_text=mActivity.final_text;
        tval=(mActivity.utime);
        System.out.println("Tval"+tval);


    /*   java.util.Date time = new java.util.Date((long) esm_data.getDouble(0));
        java.util.Date time2 = new java.util.Date((long) esm_data.getDouble(0) + (60 * 60 * 60 * 20));


        DateTime pqr = new DateTime(time);
        DateTime two = new DateTime(time2);
        EventDateTime example = new EventDateTime();
        example.setDateTime(pqr);
        EventDateTime example1 = new EventDateTime();
        example1.setDateTime(two);

        Event event = new Event()
                .setSummary(esm_data.getString(1))
                .setDescription("Social Situation");
//DateTime startDateTime = new DateTime("2015-09-28T09:00:00-07:00");
        EventDateTime start = new EventDateTime()
                .setDateTime(pqr)
                .setTimeZone("American/Los_Angeles");
        event.setStart(start);

    //    DateTime endDateTime = new DateTime("2015-09-28T17:00:00-07:00");
        EventDateTime end = new EventDateTime()
                .setDateTime(two)
                .setTimeZone("American/Los_Angeles");
        event.setEnd(end);


        String calendarId = "primary";
        event = mActivity.mService.events().insert(calendarId, event).execute();
        System.out.printf("Event created: %s\n", event.getHtmlLink());*/

    //     DateTime now = new DateTime(System.currentTimeMillis());
    //    List<String> eventStrings = new ArrayList<String>();
       Event event = new Event()
                .setSummary(radioval)
              //  .setLocation("800 Howard St., San Francisco, CA 94103")
               .setDescription("Social Situation");
        DateTime now = new DateTime(System.currentTimeMillis());
      //  DateTime startDateTime = new DateTime("2015-09-27T09:00:00-07:00");
        EventDateTime start = new EventDateTime()
               .setDateTime(now);
        event.setStart(start);

       // DateTime endDateTime = new DateTime("2015-09-27T17:00:00-07:00");
        EventDateTime end = new EventDateTime()
                .setDateTime(now)
                .setTimeZone("America/New_York");
        event.setEnd(end);
        String calendarId = "primary";
        event = mActivity.mService.events().insert(calendarId, event).execute();
        System.out.printf("Event created: %s\n", event.getHtmlLink());
     //   return eventStrings;




        Event event2 = new Event()
                .setSummary(final_text)
                        //  .setLocation("800 Howard St., San Francisco, CA 94103")
                .setDescription("Conversation Type");

        DateTime now2= new DateTime(System.currentTimeMillis());
//  DateTime startDateTime = new DateTime("2015-09-27T09:00:00-07:00");
        EventDateTime start2 = new EventDateTime()
                .setDateTime(now2);
        event2.setStart(start2);

// DateTime endDateTime = new DateTime("2015-09-27T17:00:00-07:00");
        EventDateTime end2 = new EventDateTime()
                .setDateTime(now2)
                .setTimeZone("America/Los_Angeles");
        event2.setEnd(end2);
        String calendarId2 = "primary";
        event2 = mActivity.mService.events().insert(calendarId2, event2).execute();
        System.out.printf("Event created: %s\n", event.getHtmlLink());


        DateTime now1 = new DateTime(System.currentTimeMillis());
        List<String> eventStrings = new ArrayList<String>();
        Events events = mActivity.mService.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now1)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();

        for (Event event1 : items) {
            DateTime start1 = event1.getStart().getDateTime();
            if (start == null) {
                // All-day events don't have start times, so just use
                // the start date.
                start1 = event1.getStart().getDate();
            }
            eventStrings.add(
                    String.format("%s (%s)", event1.getSummary(), start1));
        }
        return eventStrings;

    }



}



    //    private void writeToCalendar (){
   /*    DateTime now = new DateTime(System.currentTimeMillis());
        List<String> eventStrings = new ArrayList<String>();
        Event event = new Event()
                .setSummary("Google I/O 2015")
                .setLocation("800 Howard St., San Francisco, CA 94103")
                .setDescription("A chance to hear more about Google's developer products.");

        DateTime startDateTime = new DateTime("2015-09-18T09:00:00-07:00");
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("America/Los_Angeles");
        event.setStart(start);

        DateTime endDateTime = new DateTime("2015-09-29T17:00:00-07:00");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("America/Los_Angeles");
        event.setEnd(end);
        String calendarId = "primary";
        event = mActivity.mService.events().insert(calendarId, event).execute();
        System.out.printf("Event created: %s\n", event.getHtmlLink());
        return eventStrings;
    }


}*/