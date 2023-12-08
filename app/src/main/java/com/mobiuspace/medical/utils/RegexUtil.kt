package com.medical.expert.utils

import java.util.regex.Pattern

/**
 * @author zengxianghui@dayuwuxian.com
 * @date 2023/11/12 11:22 AM
 */
object RegexUtil {
    // 整数或小数
    private val numPattern = Pattern.compile("\\d+(\\.\\d+)?")

    /**
     * 获取取值范围
     * size -> 0, 不包含数值
     * size -> 1, 只包含一下数值，基本上就是最高值
     * size -> 2, 包含两个数值，第一个是最低值，另一个是最高值
     */
    private fun getRange(range: String): MutableList<Double> {
        val rangeList = mutableListOf<Double>()
        val matcher = numPattern.matcher(range)
        while (matcher.find()) {
            val group = matcher.group().toDoubleOrNull()
            group?.let {
                rangeList.add(it)
            }
        }
        return rangeList
    }

    private fun getResult(result: String?): Double? {
        result ?: return null
        val matcher = numPattern.matcher(result)
        while (matcher.find()) {
            return matcher.group().toDoubleOrNull()
        }
        return null
    }

    private fun isContainEach(range: String, result: String): Boolean {
        return result.contains(range) || range.contains(result)
    }
    /**
     * rangeList
     * size -> 0, 不包含数值，则是其它一些阳性/阴性的指标
     * size -> 1, 只包含一下数值，基本上就是最高值
     * size -> 2, 包含两个数值，第一个是最低值，另一个是最高值
     */
    fun isInRange(result: String, range: String): Boolean {
        val rangeList = getRange(range)
        when (rangeList.size) {
            0 -> return isContainEach(range, result)
            1 -> {
                getResult(result)?.let {
                    return it <= rangeList[0]
                } ?: return false
            }
            else -> {
                getResult(result)?.let {
                    return it >= rangeList[0] && it <= rangeList[1]
                } ?: return false
            }
        }
    }

}