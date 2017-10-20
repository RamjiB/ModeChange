package com.example.ramji.android.modechange;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.tasks.Task;

public class GeofenceBroadcastReceiver extends BroadcastReceiver{

    private static final String TAG = "GeofenceBroadcastReceiv";


    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG,"onReceive called");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()){
            Log.e(TAG,String.format("Error code: %d", geofencingEvent.getErrorCode()));
            return;
        }

        //Get the transition type
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        //Check which transition type has triggered this event
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
            setRingerMode(context, AudioManager.RINGER_MODE_SILENT);
        }else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT){
            setRingerMode(context,AudioManager.RINGER_MODE_NORMAL);
        }else {
            Log.e(TAG,String.format("Unknown transition: %d", geofenceTransition));
            return;
        }

        //send the notification
        sendNotification(context,geofenceTransition);
    }

    private void setRingerMode(Context context, int ringerModeSilent) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Check for DND permissions for API 24+
        if (Build.VERSION.SDK_INT < 24 || (Build.VERSION.SDK_INT >= 24 && !nm.isNotificationPolicyAccessGranted())){
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(ringerModeSilent);
        }
    }

    private void sendNotification(Context context, int transitionType) {
        //Create an explicit content Intent that starts the main activity.
        Intent notificationIntent = new Intent(context,MainActivity.class);

        //Construct the task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        //Add the main Activity to the task stack as the parent
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Check the transition type to display the relevant icon image
        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                       builder.setSmallIcon(R.drawable.ic_volume_off_white_24dp)
                                       .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                                                R.drawable.ic_volume_off_white_24dp))
                                        .setContentTitle(context.getString(R.string.silent_mode_activated));
        } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
                        builder.setSmallIcon(R.drawable.ic_volume_up_white_24dp)
                                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                                                R.drawable.ic_volume_up_white_24dp))
                                        .setContentTitle(context.getString(R.string.back_to_normal));
        }

        // Continue building the notification
        builder.setContentText(context.getString(R.string.touch_to_relaunch));
        builder.setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }
}
