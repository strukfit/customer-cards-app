package com.strukfit.customercardsapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
    public static final int REQUEST_CODE_VIEW_CARDS = 4;

    private RecyclerView cardsRecyclerView;
    private List<Card> cardList;
    private CardsAdapter cardsAdapter;

    private int cardClickedPosition = -1;

    protected AlertDialog dialogDeleteCard;

    protected boolean isCardDeleted = false;

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

        EditText inputSearch = findViewById(R.id.inputSearch);
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
            }
        });
    }

    @Override
    public void onCardClicked(Card card, int position) {
        EditText inputSearch = findViewById(R.id.inputSearch);
        if(!inputSearch.getText().toString().isEmpty()) {
            inputSearch.setText("");
        }
        cardClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), ViewCardActivity.class);
        intent.putExtra("isView", true);
        intent.putExtra("card", card);
        intent.putExtra("position", cardClickedPosition);
        startActivityForResult(intent, REQUEST_CODE_VIEW_CARDS);
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
                    EditText inputSearch = findViewById(R.id.inputSearch);
                    if(inputSearch != null && !inputSearch.getText().toString().isEmpty()) {
                        inputSearch.setText("");
                    }
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
                                EditText inputSearch = findViewById(R.id.inputSearch);
                                if(!inputSearch.getText().toString().isEmpty()) {
                                    inputSearch.setText("");
                                }
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
    }
}