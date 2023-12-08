package com.medical.expert.data

import com.google.gson.annotations.SerializedName

/**
 * @author zengxianghui@dayuwuxian.com
 * @date 2023/10/25 4:35 PM
 * 医疗诊断报告
 */
data class HealthReportData(
    @SerializedName("words_result_num") val wordsResultNum: Int,
    @SerializedName("words_result") val wordsResult: MutableList<Item>,
    @SerializedName("log_id") val logId: Long,
): ICommonData
