package com.fertility.womenshealth

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
    
    // Calculate fertility window based on last menstrual period
    fun calculateFertilityWindow(
        lastPeriodDate: String,
        cycleLength: Int = DEFAULT_CYCLE_LENGTH
    ): FertilityResult {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val lmp = LocalDate.parse(lastPeriodDate, formatter)
        
        // Ovulation occurs approximately 14 days before next period
        val ovulationDate = lmp.plusDays((cycleLength - LUTEAL_PHASE_LENGTH).toLong())
        
        // Fertility window: 5 days before ovulation to 1 day after
        val fertilityWindowStart = ovulationDate.minusDays(5)
        val fertilityWindowEnd = ovulationDate.plusDays(1)
        val nextPeriodDate = lmp.plusDays(cycleLength.toLong())
        
        // Generate list of fertile days
        val fertileDays = mutableListOf<String>()
        var currentDate = fertilityWindowStart
        while (!currentDate.isAfter(fertilityWindowEnd)) {
            fertileDays.add(currentDate.format(formatter))
            currentDate = currentDate.plusDays(1)
        }
        
        return FertilityResult(
            ovulationDate = ovulationDate.format(formatter),
            fertilityWindowStart = fertilityWindowStart.format(formatter),
            fertilityWindowEnd = fertilityWindowEnd.format(formatter),
            nextPeriodDate = nextPeriodDate.format(formatter),
            fertileDays = fertileDays
        )
    }
    
    // Calculate due date based on last menstrual period
    fun calculateDueDate(lastPeriodDate: String): DueDateResult {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val lmp = LocalDate.parse(lastPeriodDate, formatter)
        
        // Due date is approximately 280 days (40 weeks) from LMP
        val dueDate = lmp.plusDays(PREGNANCY_DAYS_FROM_LMP.toLong())
        
        // Conception typically occurs around 14 days after LMP
        val conceptionDate = lmp.plusDays(14)
        
        // Calculate current pregnancy week and days to go
        val weeksPregnant = ChronoUnit.WEEKS.between(lmp, LocalDate.now()).toInt()
        val daysToGo = ChronoUnit.DAYS.between(LocalDate.now(), dueDate).toInt()
        
        val trimester = when {
            weeksPregnant < 14 -> 1
            weeksPregnant < 28 -> 2
            else -> 3
        }
        
        return DueDateResult(
            dueDate = dueDate.format(formatter),
            conceptionDate = conceptionDate.format(formatter),
            currentWeek = weeksPregnant,
            trimester = trimester,
            daysToGo = daysToGo
        )
    }
    
    // Calculate due date based on conception date
    fun calculateDueDateFromConception(conceptionDate: String): DueDateResult {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val conception = LocalDate.parse(conceptionDate, formatter)
        
        // Due date is 266 days (38 weeks) from conception
        val dueDate = conception.plusDays(PREGNANCY_DAYS_FROM_CONCEPTION.toLong())
        val lmp = conception.minusDays(14) // Estimate LMP
        
        val weeksPregnant = ChronoUnit.WEEKS.between(lmp, LocalDate.now()).toInt()
        val daysToGo = ChronoUnit.DAYS.between(LocalDate.now(), dueDate).toInt()
        
        val trimester = when {
            weeksPregnant < 14 -> 1
            weeksPregnant < 28 -> 2
            else -> 3
        }
        
        return DueDateResult(
            dueDate = dueDate.format(formatter),
            conceptionDate = conception.format(formatter),
            currentWeek = weeksPregnant,
            trimester = trimester,
            daysToGo = daysToGo
        )
    }
    
    // Calculate pregnancy weight recommendation
    fun calculateWeightRecommendation(
        prePregnancyWeight: Double,
        height: Double,
        gestationalAge: Int
    ): WeightRecommendationResult {
        // Calculate BMI
        val bmi = prePregnancyWeight / (height * height)
        
        // Determine BMI category
        val (category, totalGainRange) = when {
            bmi < 18.5 -> "underweight" to weightGainRanges["underweight"]!!
            bmi < 25 -> "normal" to weightGainRanges["normal"]!!
            bmi < 30 -> "overweight" to weightGainRanges["overweight"]!!
            else -> "obese" to weightGainRanges["obese"]!!
        }
        
        // Calculate current recommended weight gain based on trimester
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
            gestationalAge <= 13 -> Pair(0.5, 2.0) // First trimester
            gestationalAge <= 27 -> { // Second trimester
                val progress = (gestationalAge - 13) / 14.0
                Pair(
                    2.0 + (totalGainRange.first * 0.4 - 2.0) * progress,
                    2.0 + (totalGainRange.second * 0.4 - 2.0) * progress
                )
            }
            else -> { // Third trimester
                val progress = (gestationalAge - 27) / 13.0
                Pair(
                    totalGainRange.first * 0.4 + (totalGainRange.first * 0.6) * progress,
                    totalGainRange.second * 0.4 + (totalGainRange.second * 0.6) * progress
                )
            }
        }
    }
    
    // Calendar method for fertility prediction
    fun calculateFertilityCalendarMethod(cycleLengths: List<Int>): Pair<Int, Int> {
        if (cycleLengths.size < 2) {
            return Pair(8, 20)
        }
        
        val shortestCycle = cycleLengths.minOrNull() ?: DEFAULT_CYCLE_LENGTH
        val longestCycle = cycleLengths.maxOrNull() ?: DEFAULT_CYCLE_LENGTH
        
        val fertileStart = shortestCycle - 18
        val fertileEnd = longestCycle - 11
        
        return Pair(fertileStart.coerceAtLeast(1), fertileEnd.coerceAtMost(35))
    }
}