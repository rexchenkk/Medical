package com.medical.expert.data

/**
 * @author zengxianghui@dayuwuxian.com
 * @date 2023/10/28 9:49 AM
 */
data class MedicalData(
    val type: Int,
    val items: MutableList<Item>,
    val isException: Boolean = false
)
