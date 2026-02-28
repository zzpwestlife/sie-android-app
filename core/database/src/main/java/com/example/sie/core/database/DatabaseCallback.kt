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

            Log.d("DatabaseCallback", "Starting question population from assets...")
            val inputStream = context.assets.open("questions.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonString = String(buffer, Charsets.UTF_8)
            
            val jsonArray = JSONArray(jsonString)
            Log.d("DatabaseCallback", "JSONArray length: ${jsonArray.length()}")
            
            // Get existing questions map to check for updates
            val existingQuestions = dao.getAllQuestionsSync().associateBy { it.content }
            val questionsToInsert = mutableListOf<QuestionEntity>()
            var updatedCount = 0

            for (i in 0 until jsonArray.length()) {
                try {
                    val obj = jsonArray.getJSONObject(i)
                    val content = obj.getString("content")
                    val optionsArr = obj.getJSONArray("options")
                    val correctAnswerIndex = obj.getInt("correctAnswerIndex")
                    val explanation = obj.getString("explanation")
                    val category = obj.getString("category")
                    
                    val existing = existingQuestions[content]
                    
                    if (existing != null) {
                        // Check if update is needed (especially explanation)
                        if (existing.explanation != explanation || 
                            existing.options != optionsArr.toString() ||
                            existing.correctAnswerIndex != correctAnswerIndex ||
                            existing.category != category) {
                                
                            val updated = existing.copy(
                                explanation = explanation,
                                options = optionsArr.toString(),
                                correctAnswerIndex = correctAnswerIndex,
                                category = category
                            )
                            dao.update(updated)
                            updatedCount++
                        }
                    } else {
                        // Insert new
                        questionsToInsert.add(
                            QuestionEntity(
                                content = content,
                                options = optionsArr.toString(),
                                correctAnswerIndex = correctAnswerIndex,
                                explanation = explanation,
                                category = category
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e("DatabaseCallback", "Error parsing question at index $i", e)
                }
            }
            
            if (questionsToInsert.isNotEmpty()) {
                dao.insertAll(questionsToInsert)
                Log.d("DatabaseCallback", "Inserted ${questionsToInsert.size} new questions.")
            }
            
            if (updatedCount > 0) {
                Log.d("DatabaseCallback", "Updated $updatedCount existing questions.")
            } else {
                Log.d("DatabaseCallback", "No questions needed update.")
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
