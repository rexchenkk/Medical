package com.medical.expert.data

import com.google.gson.annotations.SerializedName

/**
 * @author zengxianghui@dayuwuxian.com
 * @date 2023/12/08 4:11 PM
 */
data class MDWordsResult(
    @SerializedName("Item") val item : MutableList<MutableList<Item>>,
    @SerializedName("CommonData") val commonData : MutableList<Item>
)
