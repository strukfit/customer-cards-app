package com.strukfit.customercardsapp.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.strukfit.customercardsapp.R;
import com.strukfit.customercardsapp.entities.Card;
import com.strukfit.customercardsapp.listeners.CardsListener;

import java.util.List;

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.CardViewHolder> {

    private List<Card> cards;
    private CardsListener cardsListener;

    public CardsAdapter(List<Card> cards, CardsListener cardsListener) {
        this.cards = cards;
        this.cardsListener = cardsListener;
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

        holder.imageMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardsListener.showPopupMenu(v, cards.get(position), position);
            }
        });

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
}
