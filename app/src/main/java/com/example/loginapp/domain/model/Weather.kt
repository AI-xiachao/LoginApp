package com.example.loginapp.domain.model

/**
 * 天气领域模型
 */
data class Weather(
    val temperature: Double,
    val weatherCode: Int,
    val weatherDescription: String,
    val uvIndex: Double,
    val precipitationProbability: Int,
    val city: String,
    val humidity: Int
) {
    /**
     * 获取出行建议列表
     */
    fun getTravelAdvice(): List<String> {
        val advice = mutableListOf<String>()

        // UV 防晒建议
        when {
            uvIndex >= 11 -> advice.add("紫外线极强，必须做好防晒")
            uvIndex >= 8 -> advice.add("紫外线很强，建议涂抹防晒霜")
            uvIndex >= 6 -> advice.add("紫外线较强，注意防晒")
            uvIndex >= 3 -> advice.add("紫外线中等，可适当防晒")
        }

        // 降雨建议
        when {
            precipitationProbability >= 70 -> advice.add("降雨概率高，记得带伞")
            precipitationProbability >= 50 -> advice.add("可能下雨，建议带伞")
            precipitationProbability >= 30 -> advice.add("有降雨可能，注意天气变化")
        }

        // 温度建议
        when {
            temperature <= 0 -> advice.add("天气寒冷，注意保暖")
            temperature <= 10 -> advice.add("气温较低，建议穿厚外套")
            temperature >= 35 -> advice.add("高温天气，注意防暑降温")
            temperature >= 30 -> advice.add("天气较热，注意补水")
        }

        return advice.ifEmpty { listOf("天气适宜出行") }
    }
}

/**
 * 天气代码映射
 */
fun getWeatherDescription(code: Int): String = when (code) {
    0 -> "晴朗"
    1, 2, 3 -> "多云"
    45, 48 -> "雾"
    51, 53, 55 -> "毛毛雨"
    56, 57 -> "冻雨"
    61, 63, 65 -> "下雨"
    66, 67 -> "冻雨"
    71, 73, 75 -> "下雪"
    77 -> "雪粒"
    80, 81, 82 -> "阵雨"
    85, 86 -> "阵雪"
    95 -> "雷雨"
    96, 99 -> "雷暴"
    else -> "未知"
}
