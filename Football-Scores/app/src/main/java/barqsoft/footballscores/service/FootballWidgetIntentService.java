package barqsoft.footballscores.service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.FootballWidgetProvider;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by ken on 5/10/2015.
 */
public class FootballWidgetIntentService extends IntentService {

    public static final int COL_DATE = 1;
    public static final int COL_MATCHTIME = 2;
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_LEAGUE = 5;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_ID = 8;
    public static final int COL_MATCHDAY = 9;


    public FootballWidgetIntentService() {
        super("FootballWidgetIntentService");
    }

    //Get the today date (use to query for last game to update widget)
    public static String getTodayDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        //Getting the date for today, can do -1 or -2 for previous and two days before to test
        calendar.add(Calendar.DATE, 0);
        String myDate = dateFormat.format(calendar.getTime());

        return myDate;
    }

    @Override
    public void onHandleIntent(Intent intent) {

        Log.d("football", "service started");
        // Get all of the widget ID's
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, FootballWidgetProvider.class));

        //Gets the position of the cursor
        int pos = intent.getIntExtra("POS", 0);

        //Gets the day to get results for
        int i = intent.getIntExtra("MATCH", 2);

        int nextDay = i;
        boolean foundGames = true;
        //Log.d("football", "this i value in: " + i);
        Date date = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        String[] selectionArgs = {mformat.format(date)};

        // Get data from content provider
        Uri uri = DatabaseContract.scores_table.buildScoreWithDate();
        Cursor cursor = getContentResolver().query(uri, null, null, selectionArgs, null);
        if (cursor == null) {
            foundGames = false;
        }
        if (!cursor.moveToPosition(pos)) {
            cursor.close();
            foundGames = false;
        }
        //
        Log.d("football", "number of games found: " + cursor.getCount());
        Log.d("football", "found games: " + foundGames);

        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_layout);
            //
            Intent next = new Intent(this, FootballWidgetProvider.class).setAction("next");
            //
            if (foundGames) {
                String homeGoals;
                String awayGoals;
                if (cursor.getInt(COL_HOME_GOALS) == -1) {
                    homeGoals = "";
                    awayGoals = "";
                } else {
                    homeGoals = cursor.getString(COL_HOME_GOALS);
                    awayGoals = cursor.getString(COL_AWAY_GOALS);
                }

                // Set the text and images
                remoteViews.setTextViewText(R.id.widget_TeamHome, cursor.getString(COL_HOME));
                remoteViews.setTextViewText(R.id.widget_TeamAway, cursor.getString(COL_AWAY));
                remoteViews.setTextViewText(R.id.widget_score, homeGoals + " - " + awayGoals);
                remoteViews.setTextViewText(R.id.widget_Day, Utilies.getDayName(getApplicationContext(), System.currentTimeMillis() + ((i - 2) * 86400000)));
                remoteViews.setTextViewText(R.id.widget_GameTime, cursor.getString(COL_MATCHTIME));
                remoteViews.setImageViewResource(R.id.imageView_TeamHome, Utilies.getTeamCrestByTeamName(cursor.getString(COL_HOME)));
                remoteViews.setImageViewResource(R.id.imageView_TeamAway, Utilies.getTeamCrestByTeamName(cursor.getString(COL_AWAY)));

                remoteViews.setContentDescription(R.id.widget_TeamHome, "The home team is " + cursor.getString(COL_HOME));
                remoteViews.setContentDescription(R.id.widget_TeamAway, "The away team is " + cursor.getString(COL_AWAY));
                remoteViews.setContentDescription(R.id.widget_score, "The home team scored " + cursor.getString(COL_HOME_GOALS) + " goals and the away team scored " + cursor.getString(COL_AWAY_GOALS) + " goals");
                remoteViews.setContentDescription(R.id.widget_Day, "Game day is " + Utilies.getDayName(getApplicationContext(), System.currentTimeMillis() + ((i - 2) * 86400000)));
                remoteViews.setContentDescription(R.id.widget_GameTime, "The match time is " + cursor.getString(COL_MATCHTIME));
                //
                if (pos == cursor.getCount() - 1) {
                    pos = 0;
                    nextDay = i + 1;
                    if (i == 4) {
                        nextDay = 0;
                    }
                } else {
                    pos = pos + 1;
                }
                next.putExtra("MATCH", nextDay);
                next.putExtra("POS", pos);
            } else {
                remoteViews.setTextViewText(R.id.widget_Day, "No Games... Press Next");
                remoteViews.setContentDescription(R.id.widget_Day, "No games on this day. Press Next Button");
                //
                remoteViews.setTextViewText(R.id.widget_TeamHome, "");
                remoteViews.setTextViewText(R.id.widget_TeamAway, "");
                remoteViews.setTextViewText(R.id.widget_score, "");
                remoteViews.setTextViewText(R.id.widget_GameTime, "");
                remoteViews.setImageViewResource(R.id.imageView_TeamHome, -1);
                remoteViews.setImageViewResource(R.id.imageView_TeamAway, -1);
                //
                nextDay = i + 1;
                if (i == 4) {
                    nextDay = 0;
                }
                next.putExtra("MATCH", nextDay);
                next.putExtra("POS", 0);
            }
            //
            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0, next, PendingIntent.FLAG_CANCEL_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_NextButton, nextPendingIntent);
            //
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
}
