package com.medical.expert.data

import com.google.gson.annotations.SerializedName

/**
 * @author zengxianghui@dayuwuxian.com
 * @date 2023/10/25 3:53 PM
 * 医疗检查报告单
 */
data class MedicalDetectionData(
    @SerializedName("Item_row_num") val itemRowNum: Int,
    @SerializedName("words_result") val wordsResult: MDWordsResult,
    @SerializedName("CommonData_result_num") val commonDataResultNum: Int,
    @SerializedName("log_id") val logId: Long
): ICommonData
