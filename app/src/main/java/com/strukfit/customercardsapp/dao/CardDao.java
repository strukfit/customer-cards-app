package com.strukfit.customercardsapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.strukfit.customercardsapp.entities.Card;

import java.util.List;

@Dao
public interface CardDao {

    @Query("SELECT * FROM cards ORDER BY id DESC")
    List<Card> getAllCards();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCard(Card card);

    @Delete
    void deleteCard(Card card);

}
