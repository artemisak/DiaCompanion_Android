package com.almazov.diacompanion.base

import android.content.Context
import android.content.res.Resources
import com.almazov.diacompanion.R
import com.almazov.diacompanion.data.MealWithFood
import com.almazov.diacompanion.meal.FoodInMealItem
import com.almazov.diacompanion.model.util.FVec
import kotlinx.android.synthetic.main.fragment_meal_add_record.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException


fun checkGI(listOfFood: List<FoodInMealItem>): Boolean {
    for (food in listOfFood) {
        if (food.foodEntity.gi!! > 55) {
            return true
        }
    }
    return false
}

fun checkCarbs(mealType: String, listOfFood: List<FoodInMealItem>): Boolean {
    var sumCarbs = 0.0
    val  breakfastString = Resources.getSystem().getString(R.string.Breakfast)
    for (food in listOfFood) {
        sumCarbs += food.foodEntity.carbo!! * food.weight /100
    }
    if (sumCarbs > 30 && mealType == breakfastString) {
        return true
    } else if (sumCarbs > 60) {
        return true
    }
    return false
}

fun checkSLBefore(sugarLevel: Double): Boolean {
    if (sugarLevel > 6.7) {
        return true
    }
    return false
}

fun checkPV(listOfFood: List<FoodInMealItem>, sumPVToday: Double, sumPVYesterday: Double): Boolean {
    var sumPV = 0.0
    for (food in listOfFood) {
        sumPV += food.foodEntity.pv!! * food.weight / 100
    }
    if (sumPV < 8) {
        return true
    } else if (sumPV + sumPVToday < 20)
    {
        return true
    } else if (sumPV + sumPVYesterday < 28) return true
    return false
}

suspend fun predictSL(
    context: Context,
    BG0: Double?,
    glCarbsKr: List<Double?>,
    protein: Double?,
    mealType: String?,
    bmi: Double?
): Double {
   /* val breakfast = Resources.getSystem().getString(R.string.Breakfast)
    val lunch = Resources.getSystem().getString(R.string.Lunch)
    val dinner = Resources.getSystem().getString(R.string.Dinner)
    val snack = Resources.getSystem().getString(R.string.Snack)*/

    val breakfast = "Завтрак"
    val lunch = "Обед"
    val dinner = "Ужин"
    val snack = "Перекус"

    var t1 = 0.0
    var t2 = 0.0
    var t3 = 0.0
    var t4 = 0.0

    when (mealType) {
        breakfast -> t1 = 1.0
        lunch -> t2 = 1.0
        dinner -> t3 = 1.0
        snack -> t4 = 1.0
    }

    val modelPath: String = context.getDatabasePath("model.model").path
    val predictor = com.almazov.diacompanion.model.Predictor(
        FileInputStream(modelPath)
    )

    val denseArray = doubleArrayOf(
        BG0!!, glCarbsKr[0]!!, glCarbsKr[1]!!,
        protein!!, t1, t2, t3, t4, glCarbsKr[2]!!, bmi!!
    )
    val fVecDense: FVec = FVec.Transformer.fromArray(denseArray, false)

    return predictor.predictSingle(fVecDense)
}

fun getProtein(listOfFood: List<MealWithFood>): Double {
    var protein = 0.0
    for (food in listOfFood) {
        protein += food.food.prot!! * food.weight!! / 100
    }
    return protein
}

fun getGLCarbsKr(listOfFood: List<FoodInMealItem>): List<Double> {
    var gl = 0.0
    var carbs = 0.0
    var krs = 0.0
    for (food in listOfFood) {
        val gi = food.foodEntity.gi ?: 0.0
        val carb = food.foodEntity.carbo ?: 0.0
        val kr = food.foodEntity.kr ?: 0.0
        gl += gi * carb * food.weight / 100
        carbs += carb * food.weight / 100
        krs += kr * food.weight / 100
    }
    gl /= 100
    return listOf(gl, carbs, krs)
}

fun getProteinFatCarb(listOfFood: List<FoodInMealItem>): List<Double> {
    var proteins = 0.0
    var fats = 0.0
    var carbs = 0.0
    for (food in listOfFood) {
        val protein = food.foodEntity.prot ?: 0.0
        val fat = food.foodEntity.fat ?: 0.0
        val carb = food.foodEntity.carbo ?: 0.0
        proteins += protein * food.weight / 100
        fats += fat * food.weight / 100
        carbs += carb * food.weight / 100
    }
    return listOf(proteins, fats, carbs)
}

fun getMessage(highGI: Boolean, manyCarbs: Boolean, highBGBefore: Boolean,
               lowPV: Boolean, bgPredict: Double): String {
    var txtInt: Int? = null
    if (highGI && bgPredict > 6.8) {
        txtInt = R.string.HighGI
    } else if (manyCarbs && bgPredict > 6.8) {
        txtInt = R.string.ManyCarbs
    } else if (highBGBefore && bgPredict > 6.8) {
        txtInt = R.string.HighBGBefore
    } else if (lowPV && bgPredict > 6.8) {
        txtInt = R.string.LowPV
    } else if (bgPredict > 6.8) {
        txtInt = R.string.BGPredict
    }
    return if (txtInt != null)
        Resources.getSystem().getString(txtInt)
    else ""
}

@Throws(IOException::class)
fun getFileFromAssets(context: Context, fileName: String): File = File(context.cacheDir, fileName)
    .also {
        if (!it.exists()) {
            it.outputStream().use { cache ->
                context.assets.open(fileName).use { inputStream ->
                    inputStream.copyTo(cache)
                }
            }
        }
    }