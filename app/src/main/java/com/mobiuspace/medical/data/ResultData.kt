package com.medical.expert.data

/**
 * @author zengxianghui@dayuwuxian.com
 * @date 2023/12/8 5:15 PM
 */
data class ResultData(val type: Int, val deviceId: String , val data: MutableList<MedicalData>)
