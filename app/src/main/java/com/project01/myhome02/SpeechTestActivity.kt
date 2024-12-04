package com.project01.myhome02

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class SpeechTestActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var letterGrid: GridLayout
    private lateinit var numberGrid: GridLayout
    private lateinit var imageGrid: GridLayout
    private lateinit var resultTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var inputEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var playAgainButton: Button
    private lateinit var showResultsButton: Button

    private var currentTest = "letters"
    private var currentRound = 0
    private var correctCount = 0
    private var wrongCount = 0
    private var mediaPlayer: MediaPlayer? = null
    private val currentTestItems = mutableListOf<String>()
    private val usedLetters = mutableSetOf<String>() // Store used letters
    private val userAnswers = mutableListOf<String>() // Store user's answers for each round
    private val correctAnswers = mutableListOf<String>() // Store the correct answers
    private val userResponses = mutableListOf<Pair<String, String>>()
    private val lettersPattern = Regex("^[A-Za-z]{3}$") // Regex to match exactly 3 letters
    private val wordImages = mapOf(
        "apple" to R.drawable.apple,
        "banana" to R.drawable.banana,
        "car" to R.drawable.car,
        "dog" to R.drawable.dog,
        "elephant" to R.drawable.elephant,
        "fish" to R.drawable.fish,
        "grapes" to R.drawable.grapes,
        "house" to R.drawable.house,
        "ice" to R.drawable.ice
    )

    private var itemIndex = 0
    private lateinit var handler: Handler
    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speech_test)

        letterGrid = findViewById(R.id.letterGrid)
        numberGrid = findViewById(R.id.numberGrid)
        imageGrid = findViewById(R.id.imageGrid)
        resultTextView = findViewById(R.id.resultTextView)
        timerTextView = findViewById(R.id.timerTextView)
        inputEditText = findViewById(R.id.inputEditText)
        submitButton = findViewById(R.id.submitButton)
        playAgainButton = findViewById(R.id.playAgainButton)
        barChart = findViewById(R.id.barChart)
        showResultsButton = findViewById(R.id.showResultsButton)

        handler = Handler(Looper.getMainLooper())
        setupBarChart()
        showInstructionsPopup()

        // Set up click listeners for the buttons
        submitButton.setOnClickListener { handleSubmit() }
        playAgainButton.setOnClickListener { playAgain() }

        // Set up click listener for the show results button
        showResultsButton.setOnClickListener { navigateToResultsPage() }
    }

    private fun setupBarChart() {
        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(true)
            setDragEnabled(false)
            setScaleEnabled(false)

            axisLeft.apply {
                isEnabled = true
                setDrawGridLines(false)
                axisMinimum = 0f
            }

            axisRight.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
            }

            legend.isEnabled = true
        }
    }

    private fun showInstructionsPopup() {
        val instructions = "Welcome to the Speech Test!\n\n" +
                "In this test, you'll hear letters, numbers, and words.\n" +
                "Follow the instructions on the screen and input your answers accordingly.\n" +
                "Good luck!"

        AlertDialog.Builder(this)
            .setTitle("Instructions")
            .setMessage(instructions)
            .setPositiveButton("Start") { _, _ -> startTest() }
            .setCancelable(false)
            .show()
    }

    private fun startTest() {
        when (currentTest) {
            "letters" -> startLettersTest()
            "numbers" -> startNumbersTest()
            "words" -> startWordsTest()
        }
    }

    private fun startLettersTest() {
        currentTest = "letters"
        resetTestCounts()
        generateTestItems(currentTest) // Generate initial items
        showLetterGrid()
        playThreeItems() // Play the first 3 items
    }



    private fun startNumbersTest() {
        currentTest = "numbers"
        resetTestCounts()
        itemIndex = 0 // Ensure the itemIndex is reset
        generateTestItems("numbers")
        showNumberGrid()
        playThreeItems() // Play the first 3 items, same as in letters test
    }


    private fun startWordsTest() {
        currentTest = "words"
        resetTestCounts()
        generateTestItems("words")
        showWordImages()
        // Start by playing the first word sound immediately
        playNextSound() // Play the first word sound
    }

    private fun resetTestCounts() {
        correctCount = 0
        wrongCount = 0
        currentRound = 1
        itemIndex = 0
        usedLetters.clear() // Clear used letters for new test
    }

    private fun generateTestItems(testType: String) {
        currentTestItems.clear()
        when (testType) {
            "letters" -> {
                while (currentTestItems.size < 3) {
                    val randomLetter = ('a'..'z').random().toString()
                    if (!usedLetters.contains(randomLetter)) {
                        currentTestItems.add(randomLetter)
                        usedLetters.add(randomLetter) // Add to used letters
                    }
                }
            }
            "numbers" -> {
                repeat(3) { currentTestItems.add((0..9).random().toString()) }
            }
            "words" -> {
                repeat(5) { currentTestItems.add(wordImages.keys.random()) }
            }
        }
    }

    private fun handleSubmit() {
        val userInput = inputEditText.text.toString()

        // Ensure the user inputs exactly 3 items for letters and numbers tests
        if ((currentTest == "letters" && userInput.length != 3) ||
            (currentTest == "numbers" && userInput.length != 3)) {
            showToast("Please enter exactly 3 ${if (currentTest == "letters") "letters" else "numbers"}.")
            return
        }

        // Store the user answer and compare with the correct answer
        userAnswers.add(userInput) // Store user's input
        correctAnswers.add(currentTestItems.joinToString("")) // Store the correct sequence of items played

        // Check if the user's input matches the correct answer
        if (userInput == correctAnswers.last()) {
            correctCount++ // Increase correct count if the answer is correct
        } else {
            wrongCount++ // Increase wrong count if the answer is incorrect
        }

        // Handle answer checking for visual feedback or other logic
        handleAnswer(userInput)

        // Clear the input field for the next round
        inputEditText.setText("")

        // Proceed to the next round or test based on the current test type
        when (currentTest) {
            "numbers" -> {
                // Check if more rounds are remaining for the numbers test
                if (currentRound < 3) {
                    currentRound++
                    generateTestItems("numbers") // Generate new set of numbers for the next round
                    playThreeItems() // Play the next 3 numbers
                } else {
                    startWordsTest() // After 3 rounds, transition to the words test
                }
            }
            "letters" -> {
                // For letters test, play the next sound or start a new round
                if (currentRound < 3) {
                    currentRound++
                    generateTestItems("letters")
                    playThreeItems()
                } else {
                    startNumbersTest() // Transition to numbers test after 3 rounds
                }
            }
            "words" -> {
                // Play the next word or conclude the test
                playNextSound()
            }
        }
    }


    private fun playNextSound() {
        if (currentTest == "numbers") {
            // This is where you wait for user input before playing the next round
            // No need to do anything here as it's handled in the handleSubmit function
        } else if (currentTest == "words") {
            if (itemIndex < currentTestItems.size) {
                playAudio(currentTestItems[itemIndex])
                itemIndex++
            } else {
                showResults()
            }
        } else {
            playNextRound()
        }
    }


    private fun playAgain() {
        // Logic to replay current round's items
        // We want to re-play the last items played, without moving to the next round
        itemIndex = 0 // Reset the item index to replay the current round
        playThreeItems() // Replay the same sounds in the current round
    }

    private fun showLetterGrid() {
        letterGrid.removeAllViews()
        ('a'..'z').forEach { letter ->
            val button = Button(this).apply {
                text = letter.toString()
                setOnClickListener {
                    // Append the clicked letter to the input field
                    if (inputEditText.text.length < 3) { // Only allow 3 letters
                        inputEditText.append(letter.toString())
                    }
                }
            }
            letterGrid.addView(button)
        }
        updateVisibility(letterGrid)
        inputEditText.setText("") // Clear previous input
    }


    private fun showNumberGrid() {
        numberGrid.removeAllViews()
        (0..9).forEach { number ->
            val button = Button(this).apply {
                text = number.toString()
                setOnClickListener {
                    // Append the clicked number to the input field
                    if (inputEditText.text.length < 3) { // Only allow 3 numbers
                        inputEditText.append(number.toString())
                    }
                }
            }
            numberGrid.addView(button)
        }
        updateVisibility(numberGrid)
        inputEditText.setText("") // Clear previous input
        inputEditText.addTextChangedListener { resultTextView.text = it.toString() } // Show user input
    }


    private fun showWordImages() {
        imageGrid.removeAllViews()
        wordImages.forEach { (word, resId) ->
            val imageView = ImageView(this).apply {
                setImageResource(resId)
                setOnClickListener { handleAnswer(word) }
            }
            imageGrid.addView(imageView)
        }
        updateVisibility(imageGrid)
    }

    private fun updateVisibility(visibleGrid: GridLayout) {
        letterGrid.visibility = if (visibleGrid == letterGrid) View.VISIBLE else View.GONE
        numberGrid.visibility = if (visibleGrid == numberGrid) View.VISIBLE else View.GONE
        imageGrid.visibility = if (visibleGrid == imageGrid) View.VISIBLE else View.GONE
        inputEditText.visibility = if (visibleGrid == imageGrid) View.GONE else View.VISIBLE
        submitButton.visibility = View.VISIBLE
        playAgainButton.visibility = View.VISIBLE
    }

    private fun handleAnswer(userAnswer: String) {
        Log.d("SpeechTestActivity", "User Answer: $userAnswer")

        // Ensure itemIndex is valid before accessing the array
        if (itemIndex > 0 && itemIndex <= currentTestItems.size) {
            val correctAnswer = currentTestItems[itemIndex - 1] // Get the correct answer that was played

            // Store the played sound and user input for each question
            userResponses.add(Pair(correctAnswer, userAnswer))

            // Check if the answer is correct or wrong
            if (userAnswer == correctAnswer) {
                correctCount++ // Increment correct count if the answer is correct
            } else {
                wrongCount++ // Increment wrong count for incorrect answers
            }
        } else {
            Log.e("SpeechTestActivity", "Invalid index: $itemIndex")
            // Handle invalid index scenario
        }
    }


    private fun playNextRound() {
        Log.d("SpeechTestActivity", "Playing next round with items: $currentTestItems")

        if (currentRound < 3) { // Check if we need to go to the next round
            currentRound++ // Increment round
            itemIndex = 0 // Reset for the next round
            generateTestItems(currentTest) // Generate new items based on the current test type
            playThreeItems() // Start playing the new round
        } else {
            // All rounds for the current test have been completed; show results or switch to the next test
            when (currentTest) {
                "letters" -> {
                    currentTest = "numbers" // Set to numbers test after letters
                    currentRound = 1 // Reset round for numbers test
                    startNumbersTest() // Start the numbers test after completing letters
                }
                "numbers" -> {
                    currentTest = "words" // Set to words test after numbers
                    currentRound = 1 // Reset round for words test
                    startWordsTest() // Start the words test after completing numbers
                }
                "words" -> {
                    showResults() // Show results after the word test
                }
            }
        }
    }



    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun showResults() {
        val resultMessage = """
            Test Completed!
            Correct: $correctCount
            Wrong: $wrongCount
            User Input vs Correct:
        """.trimIndent()

        resultTextView.text = resultMessage
        userAnswers.forEachIndexed { index, answer ->
            resultTextView.append("\nRound ${index + 1}: Your answer: $answer, Correct answer: ${correctAnswers[index]}")
        }

        displayBarChart() // Call to display results on the bar chart
        showResultsButton.visibility = View.VISIBLE
    }

    private fun displayBarChart() {
        val entries = mutableListOf<BarEntry>(
            BarEntry(0f, correctCount.toFloat()),
            BarEntry(1f, wrongCount.toFloat())
        )

        val dataSet = BarDataSet(entries, "Test Results").apply {
            setColors(*ColorTemplate.MATERIAL_COLORS)
        }

        val data = BarData(dataSet).apply { barWidth = 0.4f }

        barChart.apply {
            xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Correct", "Wrong"))
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawLabels(true)
            axisLeft.setDrawLabels(true)

            setData(data)
            invalidate()
        }
    }


    private fun playNumberAudio(number: Int) {
        val audioMap = mapOf(
            0 to R.raw.zero,
            1 to R.raw.one,
            2 to R.raw.two,
            3 to R.raw.three,
            4 to R.raw.four,
            5 to R.raw.five,
            6 to R.raw.six,
            7 to R.raw.seven,
            8 to R.raw.eight,
            9 to R.raw.nine
        )

        val audioResId = audioMap[number]
        if (audioResId != null) {
            mediaPlayer?.release() // Release any previously used media player
            mediaPlayer = MediaPlayer.create(this, audioResId)
            mediaPlayer?.setOnCompletionListener {
                mediaPlayer?.release()
                mediaPlayer = null

                // Proceed to the next sound after current one finishes
                playNextSound()
            }
            mediaPlayer?.start() ?: Log.e("playNumberAudio", "MediaPlayer is null for number: $number")
        } else {
            Log.e("playNumberAudio", "Audio resource not found for number: $number")
        }
    }


    private fun playThreeItems() {
        if (currentTestItems.size < 3) {
            Log.e("SpeechTestActivity", "Not enough items to play. Required 3, but found ${currentTestItems.size}")
            return
        }

        var index = 0
        val delayBetweenItems = 1000L // 1-second delay between each item

        itemIndex = 0

        handler.post(object : Runnable {
            override fun run() {
                if (index < 3 && itemIndex < currentTestItems.size) {
                    if (currentTest == "numbers") {
                        val number = currentTestItems[itemIndex].toInt()
                        Log.d("SpeechTestActivity", "Playing number: $number")
                        playNumberAudio(number)
                    } else {
                        playAudio(currentTestItems[itemIndex])
                    }

                    itemIndex++
                    index++
                    handler.postDelayed(this, delayBetweenItems) // Schedule the next item after a delay
                } else {
                    // Once all items are played, we will wait for user input before proceeding
                    // No need to play the next sounds automatically here
                }
            }
        })
    }



    private fun playAudio(item: String) {
        val resId = resources.getIdentifier(item, "raw", packageName)

        if (resId != 0) {
            mediaPlayer?.release() // Release any previously used media player
            mediaPlayer = MediaPlayer.create(this, resId)

            mediaPlayer?.setOnCompletionListener {
                // Release the media player when the audio completes
                mediaPlayer?.release()
                mediaPlayer = null
            }

            mediaPlayer?.start()
        } else {
            // Log an error or show a message if the resource is not found
            Log.e("PlayAudio", "Audio resource not found for item: $item")
            Toast.makeText(this, "Audio resource not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToResultsPage() {
        // Start the ResultsActivity (replace with your actual ResultsActivity class)
        val intent = Intent(this, ResultsActivity::class.java)
        startActivity(intent)
    }

    private fun startCountdown(seconds: Int) {
        var timeLeft = seconds
        timerTextView.visibility = View.VISIBLE
        timerTextView.text = "$timeLeft"

        handler.post(object : Runnable {
            override fun run() {
                if (timeLeft > 0) {
                    timeLeft--
                    timerTextView.text = "$timeLeft"
                    handler.postDelayed(this, 1000) // Update every second
                } else {
                    timerTextView.visibility = View.GONE // Hide the timer when done
                    // Start the next test or continue with the current one
                    when (currentTest) {
                        "letters" -> startNumbersTest() // Start number test after countdown
                        "numbers" -> startWordsTest() // Start word test after countdown
                        "words" -> showResults() // Show results after the word test
                    }
                }
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        isRunning = false
    }
}
