package com.fertility.womenshealth

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MenstrualCycleCalculator {
    
    companion object {
        private const val DEFAULT_CYCLE_LENGTH = 28
        private const val LUTEAL_PHASE_LENGTH = 14
        private const val PREGNANCY_DAYS_FROM_LMP = 280
        private const val PREGNANCY_DAYS_FROM_CONCEPTION = 266
        
        private val weightGainRanges = mapOf(
            "underweight" to Pair(12.5, 18.0),
            "normal" to Pair(11.5, 16.0),
            "overweight" to Pair(7.0, 11.5),
            "obese" to Pair(5.0, 9.0)
        )
        
        private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }
    
    data class FertilityResult(
        val ovulationDate: String,
        val fertilityWindowStart: String,
        val fertilityWindowEnd: String,
        val nextPeriodDate: String,
        val fertileDays: List<String>
    )
    
    data class DueDateResult(
        val dueDate: String,
        val conceptionDate: String,
        val currentWeek: Int,
        val trimester: Int,
        val daysToGo: Int
    )
    
    data class WeightRecommendationResult(
        val bmiCategory: String,
        val recommendedTotalGain: Pair<Double, Double>,
        val currentRecommendedGain: Pair<Double, Double>,
        val bmi: Double
    )
    
    fun calculateFertilityWindow(
        lastPeriodDate: String,
        cycleLength: Int = DEFAULT_CYCLE_LENGTH
    ): FertilityResult {
        val lmp = dateFormatter.parse(lastPeriodDate) ?: Date()
        val calendar = Calendar.getInstance()
        calendar.time = lmp
        
        // Ovulation occurs approximately 14 days before next period
        calendar.time = lmp
        calendar.add(Calendar.DAY_OF_YEAR, cycleLength - LUTEAL_PHASE_LENGTH)
        val ovulationDate = calendar.time
        
        // Fertility window: 5 days before ovulation to 1 day after
        calendar.time = ovulationDate
        calendar.add(Calendar.DAY_OF_YEAR, -5)
        val fertilityWindowStart = calendar.time
        
        calendar.time = ovulationDate
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val fertilityWindowEnd = calendar.time
        
        calendar.time = lmp
        calendar.add(Calendar.DAY_OF_YEAR, cycleLength)
        val nextPeriodDate = calendar.time
        
        // Generate list of fertile days
        val fertileDays = mutableListOf<String>()
        calendar.time = fertilityWindowStart
        while (calendar.time <= fertilityWindowEnd) {
            fertileDays.add(dateFormatter.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return FertilityResult(
            ovulationDate = dateFormatter.format(ovulationDate),
            fertilityWindowStart = dateFormatter.format(fertilityWindowStart),
            fertilityWindowEnd = dateFormatter.format(fertilityWindowEnd),
            nextPeriodDate = dateFormatter.format(nextPeriodDate),
            fertileDays = fertileDays
        )
    }
    
    fun calculateDueDate(lastPeriodDate: String): DueDateResult {
        val lmp = dateFormatter.parse(lastPeriodDate) ?: Date()
        val calendar = Calendar.getInstance()
        
        // Due date is approximately 280 days (40 weeks) from LMP
        calendar.time = lmp
        calendar.add(Calendar.DAY_OF_YEAR, PREGNANCY_DAYS_FROM_LMP)
        val dueDate = calendar.time
        
        // Conception typically occurs around 14 days after LMP
        calendar.time = lmp
        calendar.add(Calendar.DAY_OF_YEAR, 14)
        val conceptionDate = calendar.time
        
        // Calculate current pregnancy week and days to go
        val today = Date()
        val weeksPregnant = calculateWeeksBetween(lmp, today)
        val daysToGo = calculateDaysBetween(today, dueDate)
        
        val trimester = when {
            weeksPregnant < 14 -> 1
            weeksPregnant < 28 -> 2
            else -> 3
        }
        
        return DueDateResult(
            dueDate = dateFormatter.format(dueDate),
            conceptionDate = dateFormatter.format(conceptionDate),
            currentWeek = weeksPregnant,
            trimester = trimester,
            daysToGo = daysToGo
        )
    }
    
    fun calculateWeightRecommendation(
        prePregnancyWeight: Double,
        height: Double,
        gestationalAge: Int
    ): WeightRecommendationResult {
        val bmi = prePregnancyWeight / (height * height)
        
        val (category, totalGainRange) = when {
            bmi < 18.5 -> "underweight" to weightGainRanges["underweight"]!!
            bmi < 25 -> "normal" to weightGainRanges["normal"]!!
            bmi < 30 -> "overweight" to weightGainRanges["overweight"]!!
            else -> "obese" to weightGainRanges["obese"]!!
        }
        
        val currentGainRange = calculateCurrentWeightGain(gestationalAge, totalGainRange)
        
        return WeightRecommendationResult(
            bmiCategory = category,
            recommendedTotalGain = totalGainRange,
            currentRecommendedGain = currentGainRange,
            bmi = bmi
        )
    }
    
    private fun calculateCurrentWeightGain(
        gestationalAge: Int,
        totalGainRange: Pair<Double, Double>
    ): Pair<Double, Double> {
        return when {
            gestationalAge <= 13 -> Pair(0.5, 2.0)
            gestationalAge <= 27 -> {
                val progress = (gestationalAge - 13) / 14.0
                Pair(
                    2.0 + (totalGainRange.first * 0.4 - 2.0) * progress,
                    2.0 + (totalGainRange.second * 0.4 - 2.0) * progress
                )
            }
            else -> {
                val progress = (gestationalAge - 27) / 13.0
                Pair(
                    totalGainRange.first * 0.4 + (totalGainRange.first * 0.6) * progress,
                    totalGainRange.second * 0.4 + (totalGainRange.second * 0.6) * progress
                )
            }
        }
    }
    
    private fun calculateWeeksBetween(startDate: Date, endDate: Date): Int {
        val diff = endDate.time - startDate.time
        val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
        return (days / 7).toInt()
    }
    
    private fun calculateDaysBetween(startDate: Date, endDate: Date): Int {
        val diff = endDate.time - startDate.time
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
    }
}