package com.strukfit.customercardsapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.strukfit.customercardsapp.R;
import com.strukfit.customercardsapp.adapters.CardsAdapter;
import com.strukfit.customercardsapp.alarmmanagement.BirthdaysCheckWorker;
import com.strukfit.customercardsapp.database.CardsDatabase;
import com.strukfit.customercardsapp.entities.Card;
import com.strukfit.customercardsapp.listeners.CardsListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements CardsListener {

    public static final int REQUEST_CODE_ADD_CARD = 1;
    public static final int REQUEST_CODE_UPDATE_CARD = 2;
    public static final int REQUEST_CODE_SHOW_CARDS = 3;
    public static final int REQUEST_CODE_VIEW_CARDS = 4;
    public static final int REQUEST_CODE_ALARM = 5;

    private RecyclerView cardsRecyclerView;
    private List<Card> cardList;
    private CardsAdapter cardsAdapter;
    private View birthdaysDialogView;

    private EditText inputSearch;

    private int cardClickedPosition = -1;

    protected AlertDialog dialogDeleteCard;
    private AlertDialog dialogBirthdays;

    protected boolean isCardDeleted = false;

    private boolean nightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageAddCardMain = findViewById(R.id.imageAddCardMain);
        imageAddCardMain.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateCardActivity.class),
                        REQUEST_CODE_ADD_CARD
                );
            }
        });

        cardsRecyclerView = findViewById(R.id.cardsRecyclerView);
        cardsRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        );

        cardList = new ArrayList<>();
        cardsAdapter = new CardsAdapter(cardList, this);
        cardsRecyclerView.setAdapter(cardsAdapter);

        getCards(REQUEST_CODE_SHOW_CARDS);

        inputSearch = findViewById(R.id.inputSearch);

        ImageView imageSearchClear = findViewById(R.id.imageSearchClear);
        imageSearchClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSearch();
            }
        });

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cardsAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!cardList.isEmpty()) {
                    cardsAdapter.searchCards(s.toString());
                }

                if (s.length() > 0) {
                    imageSearchClear.setVisibility(View.VISIBLE);
                } else {
                    imageSearchClear.setVisibility(View.GONE);
                }
            }
        });

        ImageView imageBirthdays = findViewById(R.id.imageBirthdays);
        imageBirthdays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBirthdaysDialog();
            }
        });

        SharedPreferences prefs = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        nightMode = prefs.getBoolean("nightmode", false);

        ImageView imageChangeTheme = findViewById(R.id.imageChangeTheme);

        if(nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            imageChangeTheme.setImageResource(R.drawable.ic_sun);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            imageChangeTheme.setImageResource(R.drawable.ic_moon);
        }

        imageChangeTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSearch();
                if (nightMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    imageChangeTheme.setImageResource(R.drawable.ic_moon);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    imageChangeTheme.setImageResource(R.drawable.ic_sun);
                }

                nightMode = !nightMode;

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("nightmode", nightMode);
                editor.apply();

                recreate();
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        setAlarm();
    }

    @Override
    public void onCardClicked(Card card, int position) {
        clearSearch();
        cardClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), ViewCardActivity.class);
        intent.putExtra("isView", true);
        intent.putExtra("card", card);
        intent.putExtra("position", cardClickedPosition);
        startActivityForResult(intent, REQUEST_CODE_VIEW_CARDS);
    }

    @Override
    public void showPopupMenu(View v, Card card, int position){
        int style;

        if (nightMode) {
            style = R.style.PopupMenuStyleDark;
        } else {
            style = R.style.PopupMenuStyle;
        }

        Context wrapper = new ContextThemeWrapper(getApplicationContext(), style);
        PopupMenu popupMenu = new PopupMenu(wrapper, v, Gravity.END);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_update) {
                    clearSearch();
                    cardClickedPosition = position;
                    Intent intent = new Intent(getApplicationContext(), CreateCardActivity.class);
                    intent.putExtra("isUpdate", true);
                    intent.putExtra("card", card);
                    startActivityForResult(intent, REQUEST_CODE_UPDATE_CARD);
                    return true;
                } else if (itemId == R.id.menu_delete) {
                    cardClickedPosition = position;
                    showDeleteCardDialog(card);
                    return true;
                } else {
                    return false;
                }
            }
        });

        popupMenu.show();
    }

    protected void showDeleteCardDialog(Card card) {
        if(dialogDeleteCard == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_card,
                    (ViewGroup) findViewById(R.id.layoutDeleteCardContainer)
            );
            builder.setView(view);
            dialogDeleteCard = builder.create();
            if(dialogDeleteCard.getWindow() != null) {
                dialogDeleteCard.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            Class<? extends Activity> currentActivityClass = getClass();

            view.findViewById(R.id.textDeleteCard).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    @SuppressLint("StaticFieldLeak")
                    class DeleteCard extends AsyncTask<Void, Void, Void> {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            CardsDatabase.getCardsDatabase(getApplicationContext()).cardDao()
                                    .deleteCard(card);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);

                            if(currentActivityClass == ViewCardActivity.class) {
                                intent.putExtra("isCardDeleted", true);
                                finish();
                            } else {
                                clearSearch();
                                dialogDeleteCard.dismiss();
                            }
                        }
                    }
                    new DeleteCard().execute();
                    isCardDeleted = true;
                    if (currentActivityClass == MainActivity.class) {
                        getCards(-1);
                    }
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDeleteCard.dismiss();
                    isCardDeleted = false;
                }
            });
        }

        dialogDeleteCard.show();

    }

    private void showBirthdaysDialog() {
        if(dialogBirthdays == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_birthdays,
                    (ViewGroup) findViewById(R.id.layoutBirthdaysContainer)
            );
            builder.setView(view);
            dialogBirthdays = builder.create();

            if(dialogBirthdays.getWindow() != null) {
                dialogBirthdays.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            birthdaysDialogView = view;
        }

        String birthdayPeople = peopleWhoHaveBirthday();

        if(!birthdayPeople.isEmpty()) {
            birthdayPeople = "Сегодня день рождения празднуют:\n" + birthdayPeople;
            TextView textBirthdays = birthdaysDialogView.findViewById(R.id.textBirthdays);
            textBirthdays.setText(birthdayPeople);
        }

        dialogBirthdays.show();

    }

    private String peopleWhoHaveBirthday() {
        Calendar calendar = Calendar.getInstance();
        String currentDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String currentMonth = String.valueOf(calendar.get(Calendar.MONTH) + 1);

        String currentDayWithZero = "0" + currentDay;
        String currentMonthWithZero = "0" + currentMonth;

        String birthdayPeople = "";

        for(Card card : cardList) {

            if(card.getDateOfBirth().isEmpty()) {
                continue;
            }

            String[] parts = card.getDateOfBirth().split("\\.");

            String birthDay = parts[0];
            String birthMonth = parts[1];

            if((birthDay.equals(currentDay) && birthMonth.equals(currentMonth))
                    || birthDay.equals(currentDayWithZero) && birthMonth.equals(currentMonthWithZero)
                    || (birthDay.equals(currentDayWithZero) && birthMonth.equals(currentMonth))
                    || birthDay.equals(currentDay) && birthMonth.equals(currentMonthWithZero)) {
                birthdayPeople += card.getName() + "\n";
            }
        }

        return birthdayPeople;
    }

    private void setAlarm() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);

        if(!sharedPreferences.getBoolean("isAlarmSet", false)) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            long currentTime = System.currentTimeMillis();
            long targetTime = calendar.getTimeInMillis();

            long delay = targetTime >= currentTime ? targetTime - currentTime : TimeUnit.DAYS.toMillis(1) + targetTime - currentTime;

            PeriodicWorkRequest dailyWork = new PeriodicWorkRequest.Builder(BirthdaysCheckWorker.class, 1, TimeUnit.DAYS)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build();

            WorkManager.getInstance(getApplicationContext()).enqueueUniquePeriodicWork(
                    "BirthdaysCheckWorker",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    dailyWork
            );

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isAlarmSet", true);
            editor.apply();
        }
    }

//    private void setAlarm() {
//        SharedPreferences sharedPreferences = this.getSharedPreferences("AlarmPrefs", Context.MODE_PRIVATE);
//
//        if(!sharedPreferences.getBoolean("isAlarmSet", false)) {
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTimeInMillis(System.currentTimeMillis());
//            calendar.set(Calendar.HOUR_OF_DAY, 20);
//            calendar.set(Calendar.MINUTE, 14);
//            calendar.set(Calendar.SECOND, 0);
//
//            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//
//            Intent intent = new Intent(this, AlarmReceiver.class);
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 10000, pendingIntent);
//
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putBoolean("isAlarmSet", true);
//            editor.apply();
//
//            Log.d("aboba", "Alarm set at time: " + String.valueOf(calendar.getTime()));
//            Log.d("aboba", "Next alarm clock info: " + alarmManager.getNextAlarmClock());
//        }
//    }

    protected void clearSearch() {
        if(!inputSearch.getText().toString().isEmpty()) {
            inputSearch.setText("");
        }
    }

    private void getCards(final int requestCode) {
        @SuppressLint("StaticFieldLeak")
        class GetCardsTask extends AsyncTask<Void, Void, List<Card>>{

            @Override
            protected List<Card> doInBackground(Void... voids) {
                return CardsDatabase
                        .getCardsDatabase(getApplicationContext())
                        .cardDao().getAllCards();
            }

            @Override
            protected void onPostExecute(List<Card> cards) {
                super.onPostExecute(cards);
                if(requestCode == REQUEST_CODE_SHOW_CARDS) {
                    cardList.addAll(cards);
                    cardsAdapter.notifyDataSetChanged();
                } else if(requestCode == REQUEST_CODE_ADD_CARD) {
                    cardList.add(0, cards.get(0));
                    cardsAdapter.notifyItemInserted(0);
                    cardsRecyclerView.smoothScrollToPosition(0);
                } else if(requestCode == REQUEST_CODE_UPDATE_CARD) {
                    cardList.remove(cardClickedPosition);
                    cardList.add(cardClickedPosition, cards.get(cardClickedPosition));
                    cardsAdapter.notifyItemChanged(cardClickedPosition);
                } else if(requestCode == REQUEST_CODE_VIEW_CARDS) {
                    cardList.remove(cardClickedPosition);
                    if(isCardDeleted) {
                        cardsAdapter.notifyItemRemoved(cardClickedPosition);
                        isCardDeleted = false;
                    } else {
                        cardList.add(cardClickedPosition, cards.get(cardClickedPosition));
                        cardsAdapter.notifyItemChanged(cardClickedPosition);
                    }
                } else if(isCardDeleted) {
                    cardList.remove(cardClickedPosition);
                    cardsAdapter.notifyItemRemoved(cardClickedPosition);
                    isCardDeleted = false;
                }
            }
        }
        new GetCardsTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_CARD && resultCode == RESULT_OK) {
            getCards(REQUEST_CODE_ADD_CARD);
        } else if(requestCode == REQUEST_CODE_UPDATE_CARD && resultCode == RESULT_OK) {
            if(data != null) {
                getCards(REQUEST_CODE_UPDATE_CARD);
            }
        } else if(requestCode == REQUEST_CODE_VIEW_CARDS && resultCode == RESULT_OK) {
            if(data != null) {
                isCardDeleted = data.getBooleanExtra("isCardDeleted", false);
                getCards(REQUEST_CODE_VIEW_CARDS);
            }
        }
        if(requestCode == REQUEST_CODE_ALARM) {
            showBirthdaysDialog();
        }
    }
}