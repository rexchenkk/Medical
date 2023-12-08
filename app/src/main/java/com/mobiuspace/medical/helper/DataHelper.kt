package com.medical.expert.helper

import android.text.TextUtils
import com.medical.expert.data.Item
import com.medical.expert.data.MedicalData
import com.medical.expert.data.key.CommonDataKey
import com.medical.expert.data.key.IndicatorKey

/**
 * @author zengxianghui@dayuwuxian.com
 * @date 2023/10/28 10:31 AM
 */
object DataHelper {

    fun getText(key: String, data: MedicalData?): String {
        val filter = data?.items?.filter { TextUtils.equals(key, it.wordName) }
        filter ?: return ""
        return if (filter.isEmpty()) {
            ""
        } else {
            filter[0].word
        }
    }

//    fun getInfos(data: MutableList<Item>?): String {
//
//        return
//    }

    fun getInfo(data: MedicalData?): String {
        data?.items ?: return ""
        if (data.items.isEmpty()) return ""
        val builder = java.lang.StringBuilder()
        data.items.forEach {
            if (it.word.isNotBlank()) {
                builder.append(it.wordName)
                    .append(":")
                    .append("\n")
                    .append(it.word)
                    .append("\n")
            }
        }
        return builder.toString()
    }

    /**
     * 移除一些不必要的信息
     * 保留姓名、年龄、报告名称、报告单名称
     */
    fun filterItem(itemList: MutableList<Item>?): MutableList<Item>? {
        if (itemList.isNullOrEmpty()) {
            return null
        }
        val iterator = itemList.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.word.isEmpty() || !isCommonDataKeyName(next.wordName)) {
                iterator.remove()
            }
        }
        return itemList
    }

    private fun isCommonDataKeyName(wordName: String): Boolean {
        CommonDataKey.values().forEach {
            if (TextUtils.equals(it.keyName, wordName)) {
                return true
            }
        }
        return false
    }

    /**
     * 过滤一些不需要的检验单指标信息
     */
    fun filterIndicatorItem(item: MutableList<Item>): MutableList<Item> {
        val iterator = item.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.word.isBlank()
                || TextUtils.equals(next.wordName, IndicatorKey.NAME_CODE.keyName)
                || TextUtils.equals(next.wordName, IndicatorKey.DEVICE_TYPE.keyName)
                || TextUtils.equals(next.wordName, IndicatorKey.TEST_METHOD.keyName)
                || TextUtils.equals(next.wordName, IndicatorKey.RESULT_TIPS.keyName)) {
                iterator.remove()
            }
        }
        return item
    }

    /**
     * 参考区间与 result 是不同值，则把参考区间值修改为 ""
     */
    fun filterUnNormalRange(result: String, range: String, indicator: MutableList<Item>, ) {
        val resultValue = result.toDoubleOrNull()
        val rangeValue = range.toDoubleOrNull()
        if((resultValue != null && rangeValue == null) || (resultValue == null && rangeValue != null)) {
            indicator.find { TextUtils.equals(it.word, range) }?.let {
                it.word = ""
            }
        }

    }


}