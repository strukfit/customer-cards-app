package com.strukfit.customercardsapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.strukfit.customercardsapp.R;
import com.strukfit.customercardsapp.database.CardsDatabase;
import com.strukfit.customercardsapp.entities.Card;
import com.strukfit.customercardsapp.listeners.CardsListener;

import java.util.List;

public class ViewCardActivity extends MainActivity {
    private TextView textName, textPhoneNumber, textDateOfBirth;
    private EditText inputCardNotes;

    private Card alreadyAvailableCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_card);

        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ImageView imageMore = findViewById(R.id.imageMore);
        imageMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v, alreadyAvailableCard, (int) getIntent().getSerializableExtra("position"));
            }
        });

        textName = findViewById(R.id.textName);
        textPhoneNumber = findViewById(R.id.textPhoneNumber);
        textDateOfBirth = findViewById(R.id.textDateOfBirth);
        inputCardNotes = findViewById(R.id.inputCardNotes);

        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        ImageView imageDone = findViewById(R.id.imageDone);
        imageDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCard();
            }
        });

        if(getIntent().getBooleanExtra("isView", false)){
            alreadyAvailableCard = (Card) getIntent().getSerializableExtra("card");
            setViewCard();
        }
    }

    private void setViewCard() {
        textName.setText(alreadyAvailableCard.getName());
        textPhoneNumber.setText(alreadyAvailableCard.getPhoneNumber());
        textDateOfBirth.setText(alreadyAvailableCard.getDateOfBirth());
        inputCardNotes.setText(alreadyAvailableCard.getCardNotes());
    }

    private void saveCard() {
        final Card card = new Card();
        card.setName(alreadyAvailableCard.getName());
        card.setPhoneNumber(alreadyAvailableCard.getPhoneNumber());
        card.setDateOfBirth(alreadyAvailableCard.getDateOfBirth());
        card.setCardNotes(inputCardNotes.getText().toString());

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
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new SaveCardTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_UPDATE_CARD && resultCode == RESULT_OK) {
            Card updatedCard = (Card) data.getSerializableExtra("updatedCard");
            alreadyAvailableCard.setName(updatedCard.getName());
            alreadyAvailableCard.setPhoneNumber(updatedCard.getPhoneNumber());
            alreadyAvailableCard.setDateOfBirth(updatedCard.getDateOfBirth());

            setViewCard();
        }
    }
}