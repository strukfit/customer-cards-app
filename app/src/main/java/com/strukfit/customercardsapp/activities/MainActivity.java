package com.strukfit.customercardsapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.strukfit.customercardsapp.R;
import com.strukfit.customercardsapp.database.CardsDatabase;
import com.strukfit.customercardsapp.entities.Card;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_ADD_CARD = 1;

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

        getCards();
    }

    private void getCards() {
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
                Log.d("CARDS", cards.toString());
            }
        }
        new GetCardsTask().execute();
    }
}