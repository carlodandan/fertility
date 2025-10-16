package com.fertility.womenshealth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var calculator: MenstrualCycleCalculator
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        calculator = MenstrualCycleCalculator()
        initializeViews()
        setupClickListeners()
    }
    
    private fun initializeViews() {
        val today = Date()
        val todayString = dateFormatter.format(today)
        
        findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etMenstrualDate).setText(todayString)
        findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etLastPeriodDate).setText(todayString)
        findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etCycleLength).setText("28")
    }
    
    private fun setupClickListeners() {
        findViewById<Button>(R.id.btnCalculateFertility).setOnClickListener {
            calculateFertilityWindow()
        }
        
        findViewById<Button>(R.id.btnCalculateDueDate).setOnClickListener {
            calculateDueDate()
        }
        
        findViewById<Button>(R.id.btnCalculateWeight).setOnClickListener {
            calculateWeightRecommendation()
        }
    }
    
    private fun calculateFertilityWindow() {
        val cycleLength = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etCycleLength).text.toString().toIntOrNull() ?: 28
        val menstrualDate = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etMenstrualDate).text.toString()
        
        if (menstrualDate.isEmpty()) {
            Toast.makeText(this, "Please enter menstrual date", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val result = calculator.calculateFertilityWindow(menstrualDate, cycleLength)
            
            val resultText = """
                📅 Next Period: ${result.nextPeriodDate}
                🥚 Ovulation Date: ${result.ovulationDate}
                💖 Fertility Window: ${result.fertilityWindowStart} to ${result.fertilityWindowEnd}
                
                Most fertile days:
                ${result.fertileDays.joinToString("\n") { "• $it" }}
            """.trimIndent()
            
            findViewById<TextView>(R.id.tvFertilityResult).text = resultText
        } catch (e: Exception) {
            Toast.makeText(this, "Error: Please use YYYY-MM-DD date format", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun calculateDueDate() {
        val lastPeriodDate = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etLastPeriodDate).text.toString()
        
        if (lastPeriodDate.isEmpty()) {
            Toast.makeText(this, "Please enter last period date", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val result = calculator.calculateDueDate(lastPeriodDate)
            
            val resultText = """
                🎀 Estimated Due Date: ${result.dueDate}
                📊 Current Week: ${result.currentWeek} weeks
                🗓️ Trimester: ${result.trimester}
                📅 Days to Go: ${result.daysToGo} days
            """.trimIndent()
            
            findViewById<TextView>(R.id.tvDueDateResult).text = resultText
        } catch (e: Exception) {
            Toast.makeText(this, "Error: Please use YYYY-MM-DD date format", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun calculateWeightRecommendation() {
        val prePregnancyWeight = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPrePregnancyWeight).text.toString().toDoubleOrNull()
        val height = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etHeight).text.toString().toDoubleOrNull()
        val gestationalAge = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etGestationalAge).text.toString().toIntOrNull()
        
        if (prePregnancyWeight == null || height == null || gestationalAge == null) {
            Toast.makeText(this, "Please enter all required fields", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val result = calculator.calculateWeightRecommendation(prePregnancyWeight, height, gestationalAge)
            
            val resultText = """
                📊 Your BMI: ${"%.1f".format(result.bmi)} (${result.bmiCategory})
                
                📈 Total Recommended Weight Gain:
                ${"%.1f".format(result.recommendedTotalGain.first)} kg - ${"%.1f".format(result.recommendedTotalGain.second)} kg
                
                🤰 Current Recommended Gain (week ${gestationalAge}):
                ${"%.1f".format(result.currentRecommendedGain.first)} kg - ${"%.1f".format(result.currentRecommendedGain.second)} kg
            """.trimIndent()
            
            findViewById<TextView>(R.id.tvWeightResult).text = resultText
        } catch (e: Exception) {
            Toast.makeText(this, "Error calculating weight recommendation", Toast.LENGTH_LONG).show()
        }
    }
}