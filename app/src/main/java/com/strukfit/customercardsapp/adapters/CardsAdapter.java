package com.strukfit.customercardsapp.adapters;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.strukfit.customercardsapp.R;
import com.strukfit.customercardsapp.activities.MainActivity;
import com.strukfit.customercardsapp.entities.Card;
import com.strukfit.customercardsapp.listeners.CardsListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.CardViewHolder> {

    private List<Card> cards;
    private CardsListener cardsListener;
    private Timer timer;
    private List<Card> cardsSource;

    public CardsAdapter(List<Card> cards, CardsListener cardsListener) {
        this.cards = cards;
        this.cardsListener = cardsListener;
        cardsSource = cards;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CardViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_card,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.setCard(cards.get(position));

        holder.layoutCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardsListener.onCardClicked(cards.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {

        TextView textName;
        LinearLayout layoutCard;
        ImageView imageMore;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            layoutCard = itemView.findViewById(R.id.layoutCard);
            imageMore = itemView.findViewById(R.id.imageMore);
        }

        void setCard(Card card) {
            textName.setText(card.getName());
        }
    }

    public void searchCards(final String searchKeyword){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(searchKeyword.trim().isEmpty()){
                    cards = cardsSource;
                } else {
                    ArrayList<Card> temp = new ArrayList<>();
                    for(Card card : cardsSource) {
                        if(card.getName().toLowerCase().contains(searchKeyword.toLowerCase())) {
                            temp.add(card);
                        }
                    }
                    cards = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run(){
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }

    public void cancelTimer(){
        if(timer != null){
            timer.cancel();
        }
    }

}
