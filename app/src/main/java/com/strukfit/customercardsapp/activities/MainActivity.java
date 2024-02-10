package com.strukfit.customercardsapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.strukfit.customercardsapp.R;
import com.strukfit.customercardsapp.adapters.CardsAdapter;
import com.strukfit.customercardsapp.database.CardsDatabase;
import com.strukfit.customercardsapp.entities.Card;
import com.strukfit.customercardsapp.listeners.CardsListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CardsListener {

    public static final int REQUEST_CODE_ADD_CARD = 1;
    public static final int REQUEST_CODE_UPDATE_CARD = 2;
    public static final int REQUEST_CODE_SHOW_CARDS = 3;

    private RecyclerView cardsRecyclerView;
    private List<Card> cardList;
    private CardsAdapter cardsAdapter;

    private int cardClickedposition = -1;

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
    }

    @Override
    public void onCardClicked(Card card, int position) {

    }

    @Override
    public void showPopupMenu(View v, Card card, int position){
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_update) {
                    cardClickedposition = position;
                    Intent intent = new Intent(getApplicationContext(), CreateCardActivity.class);
                    intent.putExtra("isUpdate", true);
                    intent.putExtra("card", card);
                    startActivityForResult(intent, REQUEST_CODE_UPDATE_CARD);
                    return true;
                } else if (itemId == R.id.menu_delete) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        popupMenu.show();
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
                    cardList.remove(cardClickedposition);
                    cardList.add(cardClickedposition, cards.get(cardClickedposition));
                    cardsAdapter.notifyItemChanged(cardClickedposition);
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
        }
    }
}