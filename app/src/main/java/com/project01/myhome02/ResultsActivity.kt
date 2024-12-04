package com.project01.myhome02

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate

class ResultsActivity : AppCompatActivity() {

    private lateinit var lettersChart: LineChart
    private lateinit var numbersChart: LineChart
    private lateinit var wordsChart: LineChart
    private lateinit var combinedChart: LineChart

    private lateinit var generateReportButton: Button
    private lateinit var lettersResultsTextView: TextView
    private lateinit var numbersResultsTextView: TextView
    private lateinit var wordsResultsTextView: TextView

    private lateinit var lettersAnswers: Array<String>
    private lateinit var lettersPlayedSounds: Array<String>

    private lateinit var numbersAnswers: Array<String>
    private lateinit var numbersPlayedSounds: Array<String>

    private lateinit var wordsAnswers: Array<String>
    private lateinit var wordsPlayedSounds: Array<String>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        lettersChart = findViewById(R.id.lettersChart)
        numbersChart = findViewById(R.id.numbersChart)
        wordsChart = findViewById(R.id.wordsChart)
        combinedChart = findViewById(R.id.combinedChart)

        generateReportButton = findViewById(R.id.generateReportButton)

        // Text views to display the played sounds, user inputs, and correct answers
        lettersResultsTextView = findViewById(R.id.lettersResultsTextView)
        numbersResultsTextView = findViewById(R.id.numbersResultsTextView)
        wordsResultsTextView = findViewById(R.id.wordsResultsTextView)

        // Retrieve the data passed from the test activities
        lettersAnswers = intent.getStringArrayExtra("lettersAnswers") ?: emptyArray()
        lettersPlayedSounds = intent.getStringArrayExtra("lettersPlayedSounds") ?: emptyArray()

        numbersAnswers = intent.getStringArrayExtra("numbersAnswers") ?: emptyArray()
        numbersPlayedSounds = intent.getStringArrayExtra("numbersPlayedSounds") ?: emptyArray()

        wordsAnswers = intent.getStringArrayExtra("wordsAnswers") ?: emptyArray()
        wordsPlayedSounds = intent.getStringArrayExtra("wordsPlayedSounds") ?: emptyArray()

        setupGraphs()
        generateReportButton.setOnClickListener { generateReport() }
        displayResults()
    }

    private fun setupGraphs() {
        displayChart(lettersChart, "Letters Test", getCorrectnessList(lettersAnswers, lettersPlayedSounds))
        displayChart(numbersChart, "Numbers Test", getCorrectnessList(numbersAnswers, numbersPlayedSounds))
        displayChart(wordsChart, "Words Test", getCorrectnessList(wordsAnswers, wordsPlayedSounds))
        displayCombinedChart()
    }

    private fun displayChart(chart: LineChart, label: String, correctnessList: List<Float>) {
        val entries = correctnessList.mapIndexed { index, value ->
            Entry(index.toFloat(), value)
        }

        val dataSet = LineDataSet(entries, label).apply {
            color = ColorTemplate.COLORFUL_COLORS[0]
            valueTextColor = Color.BLACK
            valueTextSize = 16f
        }

        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.invalidate() // refresh
    }

    private fun displayCombinedChart() {
        val combinedEntries = lettersAnswers.indices.map { index ->
            val average = (getCorrectness(lettersAnswers[index], lettersPlayedSounds[index]) +
                    getCorrectness(numbersAnswers[index], numbersPlayedSounds[index]) +
                    getCorrectness(wordsAnswers.getOrElse(index) { "" }, wordsPlayedSounds.getOrElse(index) { "" })) / 3.0f
            Entry(index.toFloat(), average)
        }

        val combinedDataSet = LineDataSet(combinedEntries, "Average Results").apply {
            color = ColorTemplate.COLORFUL_COLORS[1]
            valueTextColor = Color.BLACK
            valueTextSize = 16f
        }

        val combinedData = LineData(combinedDataSet)
        combinedChart.data = combinedData
        combinedChart.invalidate() // refresh
    }

    private fun displayResults() {
        // Display played sounds, user inputs, and whether they were correct or incorrect for letters
        lettersResultsTextView.text = buildString {
            append("Letters Test Results:\n")
            lettersPlayedSounds.forEachIndexed { index, sound ->
                val userInput = lettersAnswers.getOrElse(index) { "N/A" }
                append("Sound Played: $sound, User Input: $userInput, Correct: ${getCorrectnessText(userInput, sound)}\n")
            }
        }

        // Display played sounds, user inputs, and whether they were correct or incorrect for numbers
        numbersResultsTextView.text = buildString {
            append("Numbers Test Results:\n")
            numbersPlayedSounds.forEachIndexed { index, sound ->
                val userInput = numbersAnswers.getOrElse(index) { "N/A" }
                append("Sound Played: $sound, User Input: $userInput, Correct: ${getCorrectnessText(userInput, sound)}\n")
            }
        }

        // Display played sounds, user inputs, and whether they were correct or incorrect for words
        wordsResultsTextView.text = buildString {
            append("Words Test Results:\n")
            wordsPlayedSounds.forEachIndexed { index, sound ->
                val userInput = wordsAnswers.getOrElse(index) { "N/A" }
                append("Sound Played: $sound, User Input: $userInput, Correct: ${getCorrectnessText(userInput, sound)}\n")
            }
        }
    }

    private fun getCorrectnessList(answers: Array<String>, playedSounds: Array<String>): List<Float> {
        return answers.mapIndexed { index, answer ->
            getCorrectness(answer, playedSounds.getOrElse(index) { "" })
        }
    }

    private fun getCorrectness(answer: String, playedSound: String): Float {
        return if (answer == playedSound) 1f else 0f
    }

    private fun getCorrectnessText(answer: String, playedSound: String): String {
        return if (answer == playedSound) "Yes" else "No"
    }

    private fun generateReport() {
        // Logic for generating a report (e.g., save to a file, show a dialog, etc.)
    }
}
