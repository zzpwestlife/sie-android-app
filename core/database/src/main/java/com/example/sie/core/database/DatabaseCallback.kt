package com.example.sie.core.database

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
            
            val questionsToInsert = mutableListOf<QuestionEntity>()
            var updatedCount = 0

            for (i in 0 until jsonArray.length()) {
                try {
                    val obj = jsonArray.getJSONObject(i)
                    
                    val content_en = obj.getString("content_en")
                    val content_zh = obj.getString("content_zh")
                    val options_en = obj.getJSONArray("options_en").toString()
                    val options_zh = obj.getJSONArray("options_zh").toString()
                    val correctAnswerIndex = obj.getInt("correctAnswerIndex")
                    val explanation_en = obj.getString("explanation_en")
                    val explanation_zh = obj.getString("explanation_zh")
                    val category_en = obj.getString("category_en")
                    val category_zh = obj.getString("category_zh")
                    val category_short = obj.getString("category_short")
                    
                    questionsToInsert.add(
                        QuestionEntity(
                            content_en = content_en,
                            content_zh = content_zh,
                            options_en = options_en,
                            options_zh = options_zh,
                            correctAnswerIndex = correctAnswerIndex,
                            explanation_en = explanation_en,
                            explanation_zh = explanation_zh,
                            category_en = category_en,
                            category_zh = category_zh,
                            category_short = category_short
                        )
                    )
                } catch (e: Exception) {
                    Log.e("DatabaseCallback", "Error parsing question at index $i", e)
                }
            }
            
            if (questionsToInsert.isNotEmpty()) {
                dao.insertAll(questionsToInsert)
                Log.d("DatabaseCallback", "Inserted ${questionsToInsert.size} new questions.")
            }
        } catch (e: Exception) {
            Log.e("DatabaseCallback", "Error populating questions", e)
            e.printStackTrace()
        }
    }

}
