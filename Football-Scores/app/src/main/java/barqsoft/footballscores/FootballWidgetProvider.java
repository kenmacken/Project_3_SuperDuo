package barqsoft.footballscores;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import barqsoft.footballscores.service.FootballWidgetIntentService;

/**
 * Created by ken on 4/10/2015.
 */
public class FootballWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        Log.d("onUpdate", "2");
        context.startService(new Intent(context, FootballWidgetIntentService.class).putExtra("MATCH", 2));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        int i, pos;
        pos = intent.getIntExtra("POS", 0);
        i = intent.getIntExtra("MATCH", 2);
        Log.d("Football", "onReceive in: " + i);
        context.startService(new Intent(context, FootballWidgetIntentService.class).putExtra("MATCH", i).putExtra("POS", pos));

    }

}