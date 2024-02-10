package com.strukfit.customercardsapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.strukfit.customercardsapp.R;
import com.strukfit.customercardsapp.database.CardsDatabase;
import com.strukfit.customercardsapp.entities.Card;

public class CreateCardActivity extends AppCompatActivity {

    private EditText inputCardName, inputCardPhoneNumber, inputCardDateOfBirth;

    private Card alreadyAvailableCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_card);

        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        inputCardName = findViewById(R.id.inputCardName);
        inputCardPhoneNumber = findViewById(R.id.inputCardPhoneNumber);
        inputCardDateOfBirth = findViewById(R.id.inputCardDateOfBirth);

        ImageView imageDone = findViewById(R.id.imageDone);
        imageDone.setOnClickListener(new View.OnClickListener() {
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

        final Card card = new Card();
        card.setName(inputCardName.getText().toString());
        card.setPhoneNumber(inputCardPhoneNumber.getText().toString());
        card.setDateOfBirth(inputCardDateOfBirth.getText().toString());

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
}