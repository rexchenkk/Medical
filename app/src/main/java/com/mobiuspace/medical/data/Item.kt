package com.medical.expert.data

import com.google.gson.annotations.SerializedName

/**
 * @author zengxianghui@dayuwuxian.com
 * @date 2023/10/25 4:27 PM
 */
data class Item(
    @SerializedName("word_name") val wordName: String,
    @SerializedName("word") var word: String,
)
