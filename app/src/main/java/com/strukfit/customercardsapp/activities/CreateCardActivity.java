package com.strukfit.customercardsapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.strukfit.customercardsapp.R;
import com.strukfit.customercardsapp.database.CardsDatabase;
import com.strukfit.customercardsapp.entities.Card;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateCardActivity extends AppCompatActivity {

    private EditText inputCardName, inputCardPhoneNumber, inputCardDateOfBirth;

    private Card alreadyAvailableCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_card);

        TextView textCancel = findViewById(R.id.textCancel);
        textCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        inputCardName = findViewById(R.id.inputCardName);
        inputCardPhoneNumber = findViewById(R.id.inputCardPhoneNumber);
        inputCardDateOfBirth = findViewById(R.id.inputCardDateOfBirth);

        TextView textSaveCard = findViewById(R.id.textSaveCard);
        textSaveCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCard();
            }
        });

        if(getIntent().getBooleanExtra("isUpdate", false)){
            alreadyAvailableCard = (Card) getIntent().getSerializableExtra("card");
            setUpdateCard();
        }
    }

    private void setUpdateCard() {
        inputCardName.setText(alreadyAvailableCard.getName());
        inputCardPhoneNumber.setText(alreadyAvailableCard.getPhoneNumber());
        inputCardDateOfBirth.setText(alreadyAvailableCard.getDateOfBirth());
    }

    private void saveCard() {
        if(inputCardName.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "ФИО карточки не может быть пустым!", Toast.LENGTH_SHORT).show();
            return;
        }

        String cardPhoneNumber = inputCardPhoneNumber.getText().toString();

        if(!isValidPhoneNumber(cardPhoneNumber)) {
            Toast.makeText(this, "Введите номер телефона в формате '+XXXXXXXXXX'. Номер телефона должен состоять не менее чем из 7 цифр!", Toast.LENGTH_SHORT).show();
            return;
        }

        String cardDateOfBirth = inputCardDateOfBirth.getText().toString();

        if(!isValidDate(cardDateOfBirth)) {
            Toast.makeText(this, "Введите дату рождения в формате 'дд.мм.гггг'", Toast.LENGTH_SHORT).show();
            return;
        }

        final Card card = new Card();
        card.setName(inputCardName.getText().toString());
        card.setPhoneNumber(cardPhoneNumber);
        card.setDateOfBirth(cardDateOfBirth);

        if(alreadyAvailableCard != null) {
            card.setId(alreadyAvailableCard.getId());
        }

        // Room doesn't allow database operation on the Main thread. That's why we are using async task to save card
        @SuppressLint("StaticFieldLeak")
        class SaveCardTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                CardsDatabase.getCardsDatabase(getApplicationContext()).cardDao().insertCard(card);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Intent intent = new Intent();
                intent.putExtra("updatedCard", card);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new SaveCardTask().execute();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        String phonePattern = "(^\\+(?:[0-9] ?){6,14}[0-9]$)?";

        Pattern pattern = Pattern.compile(phonePattern);

        Matcher matcher = pattern.matcher(phoneNumber);

        return matcher.matches();
    }

    public static boolean isValidDate(String date) {
        String datePattern = "(^(0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[012])\\.\\d{4}$)?";

        Pattern pattern = Pattern.compile(datePattern);

        Matcher matcher = pattern.matcher(date);

        return matcher.matches();
    }
}