package aka.braze.android;

import android.Manifest;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.braze.Braze;
import com.braze.IBraze;
import com.braze.support.BrazeLogger;
import com.braze.BrazeUser;
import com.braze.events.IValueCallback;
import com.google.firebase.messaging.FirebaseMessaging;
import com.braze.BrazeActivityLifecycleCallbackListener;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyApplication extends Application {
    private static String currentUserId = "";

    public static String getCurrentUserId() {
        return currentUserId;
    }

    public static void updateUserId(String uid) {
        currentUserId = uid;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        BrazeLogger.setLogLevel(Log.VERBOSE);
        registerActivityLifecycleCallbacks(new BrazeActivityLifecycleCallbackListener());

        triggerGoogleAdvertisingIdCollection();
        fetchCurrentBrazeUser();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }

        // FCM Token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("MyFirebaseMessagingService", "[MyApplication] Fetching FCM token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d("MyFirebaseMessagingService", "[MyApplication] FCM Token: " + token);
                });
    }

    private void triggerGoogleAdvertisingIdCollection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AdvertisingIdClient.Info idInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());

                    if (idInfo != null) {
                        String gaid = idInfo.getId();
                        boolean isLimitAdTrackingEnabled = idInfo.isLimitAdTrackingEnabled();

                        Log.d("[Braze]", "Thu thập GAID thành công: " + gaid);

                        Braze.getInstance(getApplicationContext()).setGoogleAdvertisingId(gaid, isLimitAdTrackingEnabled);
                    }
                } catch (Exception e) {
                    Log.e("[Braze]", "Lỗi trong quá trình thu thập Google Advertising ID", e);
                }
            }
        }).start();
    }

    private void fetchCurrentBrazeUser() {
        Braze.getInstance(this).getCurrentUser(new IValueCallback<BrazeUser>() {
            @Override
            public void onSuccess(BrazeUser brazeUser) {
                if (brazeUser != null) {
                    String userId = brazeUser.getUserId();
                    currentUserId = (userId != null) ? userId : "";
                    Log.d("[Braze]", "Current Braze External User ID: " + currentUserId);
                }
            }

            @Override
            public void onError() {
                Log.e("[Braze]", "Không thể lấy thông tin Current Braze User.");
            }
        });
    }

}


