package com.strukfit.customercardsapp.listeners;

import android.view.View;

import com.strukfit.customercardsapp.entities.Card;

public interface CardsListener {
    void onCardClicked(Card card, int position);
    void showPopupMenu(View v, Card card, int position);
}
