package ru.myandroidhelper.quizapp.checkyourself.notification;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import ru.myandroidhelper.quizapp.checkyourself.constants.AppConstants;
import ru.myandroidhelper.quizapp.checkyourself.data.sqlite.NotificationDbController;


public class MyFirebaseMessagingService extends FirebaseMessagingService {



    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        if (remoteMessage.getData().size() > 0) {
            Map<String, String> params = remoteMessage.getData();

                sendNotification(params.get("title"), params.get("message"), params.get("url"));
                broadcastNewNotification();
        }
    }


    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
        Log.d("FCMess", "onMessageSent: " + s);
    }


    private void sendNotification(String title, String messageBody, String url) {

        // insert data into database
        NotificationDbController notificationDbController = new NotificationDbController(MyFirebaseMessagingService.this);
        notificationDbController.insertData(title, messageBody, url);

    }

    private void broadcastNewNotification() {
        Intent intent = new Intent(AppConstants.NEW_NOTI);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);


    }

}
