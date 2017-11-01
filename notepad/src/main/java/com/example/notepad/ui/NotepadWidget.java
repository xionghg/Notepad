package com.example.notepad.ui;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.notepad.R;
import com.example.notepad.bean.Note;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Implementation of App Widget functionality.
 */
public class NotepadWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notepad_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notepad_widget);
        //Toast.makeText(context, "received in NotepadWidget", Toast.LENGTH_SHORT).show();

        int[] amount = new int[4];
        List<Note> tempNoteList;
        for (int i = 0; i < 4; i++) {
            tempNoteList = DataSupport.where("typeid=?", String.valueOf(i)).find(Note.class);
            amount[i] = 0;
            try {
                amount[i] = tempNoteList.size();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Log.e("size"+String.valueOf(i), String.valueOf(amount[i]));
            //intent.putExtra(String.valueOf(i), amount[i]);
            tempNoteList.clear();
        }
        views.setTextViewText(R.id.widget_meeting, String.valueOf(amount[0]));
        views.setTextViewText(R.id.widget_menu, String.valueOf(amount[1]));
        views.setTextViewText(R.id.widget_anniversary, String.valueOf(amount[2]));
        views.setTextViewText(R.id.widget_todolist, String.valueOf(amount[3]));

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, NotepadWidget.class);
        manager.updateAppWidget(thisWidget, views);
    }

    public static void actionToNotepadWidget(Context context) {
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
        context.sendBroadcast(intent);
    }
}

