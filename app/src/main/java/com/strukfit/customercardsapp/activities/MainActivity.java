package com.strukfit.customercardsapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.strukfit.customercardsapp.R;
import com.strukfit.customercardsapp.adapters.CardsAdapter;
import com.strukfit.customercardsapp.database.CardsDatabase;
import com.strukfit.customercardsapp.entities.Card;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_ADD_CARD = 1;

    private RecyclerView cardsRecyclerView;
    private List<Card> cardList;
    private CardsAdapter cardsAdapter;

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
        cardsAdapter = new CardsAdapter(cardList);
        cardsRecyclerView.setAdapter(cardsAdapter);

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
                if(cardList.isEmpty()) {
                    cardList.addAll(cards);
                    cardsAdapter.notifyDataSetChanged();
                } else {
                    cardList.add(0, cards.get(0));
                    cardsAdapter.notifyItemInserted(0);
                }
                cardsRecyclerView.smoothScrollToPosition(0);
            }
        }
        new GetCardsTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_CARD && resultCode == RESULT_OK) {
            getCards();
        }
    }
}