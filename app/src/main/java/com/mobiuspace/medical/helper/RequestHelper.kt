package com.medical.expert.helper

import android.app.Application
import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.luck.picture.lib.utils.ToastUtils
import com.medical.expert.data.HealthReportData
import com.medical.expert.data.ICommonData
import com.medical.expert.data.MedicalDetectionData
import com.mobiuspace.medical.utils.FileUtil
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

/**
 * @author zengxianghui@dayuwuxian.com
 * @date 2023/12/08 11:52 AM
 */
object RequestHelper {
    private const val TAG = "RequestHelper"
    private const val API_KEY = "qTUsu5xsPhEIHHoh5rzukZK0"
    private const val SECRET_KEY = "wEF0Yf4OjUBlQmFUCXh1DvDbxODRFDdl"
    private val HTTP_CLIENT = OkHttpClient().newBuilder().build()
    private const val MEDIA_TYPE = "application/x-www-form-urlencoded"
    private const val OCR_BASE_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/"

    // 医疗诊断报告单
    // 数据格式：https://ai.baidu.com/ai-doc/OCR/El59es47z
    const val HEALTH_REPORT = "health_report"

    // 医疗检验单
    // 数据格式：https://ai.baidu.com/ai-doc/OCR/Ekvakju92
    const val MEDICAL_REPORT_DETECTION = "medical_report_detection"

    /**
     * 请求
     */
    fun doMedicalRequest(context: Context, pathUrl: String, imagePath: String): Any? {
        var accessToken: String? = null
        try {
            accessToken = getAccessToken()
        } catch (e: Exception) {
            Log.e(TAG, "doMedicalRequest: ", e)
        }
        accessToken ?: ToastUtils.showToast(context, "获取鉴权信息失败，请重试")
        // image 图像数据，base64编码后进行urlencode，要求base64编码和urlencode后大小不超过4M，最短边至少15px，最长边最大4096px，支持jpg/jpeg/png/bmp格式
        val mediaType = MEDIA_TYPE.toMediaTypeOrNull()
        val imageFileParam = FileUtil.getImageFileParam(context, imagePath)
        if (TextUtils.isEmpty(imageFileParam)) {
            ToastUtils.showToast(context, "读取图片失败，请重试！")
            return null
        }
        val body: RequestBody =
            RequestBody.create(mediaType, "$imageFileParam&location=false&probability=false")
        val builder = Request.Builder()
            .url("${OCR_BASE_URL}$pathUrl?access_token=" + accessToken)
            .method("POST", body)
            .addHeader("Content-Type", MEDIA_TYPE)
        if (TextUtils.equals(pathUrl, MEDICAL_REPORT_DETECTION)) {
            builder.addHeader("Accept", "application/json")
        }
        val response = HTTP_CLIENT.newCall(builder.build()).execute()
        return if (response.isSuccessful) {
            val string = response.body?.string()
            Log.d(TAG, "doMedicalRequest: $string")
            string?.let {
                val clazz = if (TextUtils.equals(pathUrl, HEALTH_REPORT)) HealthReportData::class.java else MedicalDetectionData::class.java
                Gson().fromJson(it, clazz)
            }
        } else {
            Log.e(TAG, "doMedicalRequest: code=${response.code}, message=${response.message}")
            null
        }
    }

    /**
     * 从用户的AK，SK生成鉴权签名（Access Token）
     *
     * @return 鉴权签名（Access Token）
     * @throws IOException IO异常
     */
    @Throws(IOException::class)
    fun getAccessToken(): String? {
        val mediaType: MediaType? = MEDIA_TYPE.toMediaTypeOrNull()
        val body: RequestBody = RequestBody.create(
            mediaType, "grant_type=client_credentials&client_id=$API_KEY&client_secret=$SECRET_KEY"
        )
        val request: Request = Request.Builder()
            .url("https://aip.baidubce.com/oauth/2.0/token")
            .method("POST", body)
            .addHeader("Content-Type", MEDIA_TYPE)
            .build()
        val response: Response = HTTP_CLIENT.newCall(request).execute()
        return JSONObject(response.body!!.string()).getString("access_token")
    }
}