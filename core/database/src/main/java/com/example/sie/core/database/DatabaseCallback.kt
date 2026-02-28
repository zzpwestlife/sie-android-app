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
        try {
            Log.d("DatabaseCallback", "populateDatabase started")
            val database = databaseProvider.get()
            val dao = database.questionDao()

            val count = dao.getQuestionCount()
            Log.d("DatabaseCallback", "Current question count: $count")
            
            if (count > 0) {
                Log.d("DatabaseCallback", "Database already populated, skipping.")
                return
            }

            Log.d("DatabaseCallback", "Starting population from assets...")
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
            Log.e("DatabaseCallback", "Error populating database", e)
            e.printStackTrace()
        }
    }
}
