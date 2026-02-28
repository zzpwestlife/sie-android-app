package com.example.sie.core.database

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sie.core.database.model.CardEntity
import com.example.sie.core.database.model.QuestionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Provider

class DatabaseCallback(
    private val context: Context,
    private val databaseProvider: Provider<AppDatabase>,
    private val scope: CoroutineScope
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        Log.d("DatabaseCallback", "onCreate called")
        scope.launch(Dispatchers.IO) {
            populateDatabase()
        }
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        Log.d("DatabaseCallback", "onOpen called")
        scope.launch(Dispatchers.IO) {
            populateDatabase()
        }
    }

    private suspend fun populateDatabase() {
        populateQuestions()
        populateCards()
    }

    private suspend fun populateQuestions() {
        try {
            Log.d("DatabaseCallback", "populateQuestions started")
            val database = databaseProvider.get()
            val dao = database.questionDao()

            val count = dao.getQuestionCount()
            Log.d("DatabaseCallback", "Current question count: $count")
            
            if (count > 0) {
                Log.d("DatabaseCallback", "Questions already populated, skipping.")
                return
            }

            Log.d("DatabaseCallback", "Starting question population from assets...")
            val inputStream = context.assets.open("questions.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonString = String(buffer, Charsets.UTF_8)
            Log.d("DatabaseCallback", "JSON string length: ${jsonString.length}")
            
            val questions = mutableListOf<QuestionEntity>()
            val jsonArray = JSONArray(jsonString)
            Log.d("DatabaseCallback", "JSONArray length: ${jsonArray.length()}")
            
            for (i in 0 until jsonArray.length()) {
                try {
                    val obj = jsonArray.getJSONObject(i)
                    val optionsArr = obj.getJSONArray("options")
                    // options list logic removed as it was unused
                    
                    questions.add(
                        QuestionEntity(
                            content = obj.getString("content"),
                            options = optionsArr.toString(),
                            correctAnswerIndex = obj.getInt("correctAnswerIndex"),
                            explanation = obj.getString("explanation"),
                            category = obj.getString("category")
                        )
                    )
                } catch (e: Exception) {
                    Log.e("DatabaseCallback", "Error parsing question at index $i", e)
                }
            }
            
            if (questions.isNotEmpty()) {
                dao.insertAll(questions)
                Log.d("DatabaseCallback", "Successfully inserted ${questions.size} questions.")
            } else {
                Log.e("DatabaseCallback", "No questions parsed!")
            }
        } catch (e: Exception) {
            Log.e("DatabaseCallback", "Error populating questions", e)
            e.printStackTrace()
        }
    }

    private suspend fun populateCards() {
        try {
            Log.d("DatabaseCallback", "populateCards started")
            val database = databaseProvider.get()
            val dao = database.cardDao()

            val count = dao.getCardCount()
            Log.d("DatabaseCallback", "Current card count: $count")

            if (count > 0) {
                Log.d("DatabaseCallback", "Cards already populated, skipping.")
                return
            }

            Log.d("DatabaseCallback", "Starting card population from assets...")
            val inputStream = context.assets.open("card.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonString = String(buffer, Charsets.UTF_8)
            Log.d("DatabaseCallback", "JSON string length: ${jsonString.length}")

            val cards = mutableListOf<CardEntity>()
            val jsonArray = JSONArray(jsonString)
            Log.d("DatabaseCallback", "JSONArray length: ${jsonArray.length()}")

            for (i in 0 until jsonArray.length()) {
                try {
                    val obj = jsonArray.getJSONObject(i)
                    val image = if (obj.has("image") && !obj.isNull("image")) obj.getString("image") else null

                    cards.add(
                        CardEntity(
                            id = obj.getInt("id"),
                            front = obj.getString("front"),
                            back = obj.getString("back"),
                            category = obj.getString("category"),
                            image = image
                        )
                    )
                } catch (e: Exception) {
                    Log.e("DatabaseCallback", "Error parsing card at index $i", e)
                }
            }

            if (cards.isNotEmpty()) {
                dao.insertCards(cards)
                Log.d("DatabaseCallback", "Successfully inserted ${cards.size} cards.")
            } else {
                Log.e("DatabaseCallback", "No cards parsed!")
            }
        } catch (e: Exception) {
            Log.e("DatabaseCallback", "Error populating cards", e)
            e.printStackTrace()
        }
    }
}
