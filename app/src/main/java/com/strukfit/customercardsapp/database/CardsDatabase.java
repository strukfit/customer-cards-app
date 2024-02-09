package com.strukfit.customercardsapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.strukfit.customercardsapp.dao.CardDao;
import com.strukfit.customercardsapp.entities.Card;

@Database(entities = Card.class, version = 1, exportSchema = false)
public abstract class CardsDatabase extends RoomDatabase {

    private static CardsDatabase cardsDatabase;

    public static synchronized CardsDatabase getCardsDatabase(Context context) {
        if(cardsDatabase == null) {
            cardsDatabase = Room.databaseBuilder(
                    context,
                    CardsDatabase.class,
                    "cards_db"
            ).build();
        }
        return cardsDatabase;
    }

    public abstract CardDao cardDao();

}
