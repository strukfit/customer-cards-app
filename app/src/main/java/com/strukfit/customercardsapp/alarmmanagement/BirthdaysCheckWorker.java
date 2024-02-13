package com.strukfit.customercardsapp.alarmmanagement;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.strukfit.customercardsapp.R;
import com.strukfit.customercardsapp.activities.MainActivity;
import com.strukfit.customercardsapp.database.CardsDatabase;
import com.strukfit.customercardsapp.entities.Card;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BirthdaysCheckWorker extends Worker {
    private static final String channelId = "CHANNEL_ID_NOTIFICATION";

    List<Card> cardList;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public BirthdaysCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        cardList = new ArrayList<>();
        executorService.execute(() -> {
            List<Card> cards = CardsDatabase.getCardsDatabase(getApplicationContext())
                    .cardDao().getAllCards();

            cardList.addAll(cards);

            String birthdayPeople = peopleWhoHaveBirthday();

            if (!birthdayPeople.isEmpty()) {
                sendNotification(birthdayPeople);
            }

        });

        return Result.success();
    }

    private String peopleWhoHaveBirthday() {
        Calendar calendar = Calendar.getInstance();
        String currentDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String currentMonth = String.valueOf(calendar.get(Calendar.MONTH) + 1);

        String currentDayWithZero = "0" + currentDay;
        String currentMonthWithZero = "0" + currentMonth;

        String birthdayPeople = "";

        for (Card card : cardList) {

            if (card.getDateOfBirth().isEmpty()) {
                continue;
            }

            String[] parts = card.getDateOfBirth().split("\\.");

            String birthDay = parts[0];
            String birthMonth = parts[1];

            if ((birthDay.equals(currentDay) && birthMonth.equals(currentMonth))
                    || birthDay.equals(currentDayWithZero) && birthMonth.equals(currentMonthWithZero)
                    || (birthDay.equals(currentDayWithZero) && birthMonth.equals(currentMonth))
                    || birthDay.equals(currentDay) && birthMonth.equals(currentMonthWithZero)) {
                birthdayPeople += card.getName() + "\n";
            }
        }

        return birthdayPeople;
    }

    private void sendNotification(String birthdayPeople) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), MainActivity.REQUEST_CODE_ALARM, intent, PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.ic_notification_birthday)
                .setContentTitle("Некоторые клиенты празднуют день рождения")
                .setContentText(birthdayPeople)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);;

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelId);
            if(notificationChannel == null) {
                CharSequence name = "BirthdayNotifications";
                String description = "Channel for birthday notifications";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelId, name, importance);
                notificationChannel.setDescription(description);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
                Log.d("aboba", "Notification channel created");
            }
        }

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        notificationManager.notify(0, builder.build());
        Log.d("aboba", "Notification sent");
    }
}
