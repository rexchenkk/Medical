package com.mobiuspace.medical.viewmodel

import android.app.Application
import android.text.TextUtils
import android.widget.Switch
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.google.gson.Gson
import com.luck.picture.lib.utils.ToastUtils
import com.medical.expert.data.key.IndicatorKey
import com.medical.expert.data.HealthReportData
import com.medical.expert.data.Item
import com.medical.expert.data.MedicalData
import com.medical.expert.data.MedicalDetectionData
import com.medical.expert.data.ResultData
import com.medical.expert.helper.DataHelper
import com.medical.expert.helper.RequestHelper
import com.medical.expert.utils.RegexUtil
import com.mobiuspace.medical.Content
import com.mobiuspace.medical.ConversationDao
import com.mobiuspace.medical.ConversationDataBase
import com.mobiuspace.medical.ConversationModel
import com.mobiuspace.medical.Role
import com.mobiuspace.medical.data.key.DataType
import com.mobiuspace.medical.utils.DeviceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @author zengxianghui@dayuwuxian.com
 * @date 2023/12/08 11:49 AM
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
  private val conversationDao: ConversationDao by lazy {
    Room.databaseBuilder(
      getApplication<Application>().applicationContext,
      ConversationDataBase::class.java,
      "conversation"
    ).build().conversationDao()
  }
  val data = MutableLiveData<String>()
  val conversation: MutableLiveData<List<ConversationModel>> = MutableLiveData(
    mutableListOf()
  )
  val isLoading = MutableLiveData<Boolean>(false)
  val dataType = MutableLiveData<DataType>(DataType.TEXT)

  init {
    viewModelScope.launch(Dispatchers.IO) {
      (conversationDao.getAllConversation().takeIf { it.isNotEmpty() } ?: mutableListOf(
        ConversationModel(
          System.currentTimeMillis(),
          Content.Statement("你好，我是人工智能生成的华佗医生，能够解答医疗健康问题，请问有什么可以帮到你？"),
          Role.Doctor
        ).store()
      )).let {
        conversation.postValue(it)
      }
    }
  }

  private fun ConversationModel.store(): ConversationModel {
    viewModelScope.launch(Dispatchers.IO) { conversationDao.addConversation(this@store) }
    return this
  }

  fun receiveFromGPT(statement: String, role: Role) {
    val conversationModels = conversation.value ?: emptyList()
    if (conversationModels.isEmpty()) {
       return
    }
    val last = (conversation.value ?: emptyList()).last()
    if (((last.content) as Content.Statement).msg.isBlank()) {
      isLoading.value = false
      last.content = Content.Statement(statement)
      last.store()
    }
    conversation.postValue((conversation.value ?: emptyList()))
  }

  fun sendToGPT(statement: String, role: Role) {
    conversation.postValue(
      (conversation.value ?: emptyList()).plus(
        ConversationModel(
          System.currentTimeMillis(),
          Content.Statement(statement),
          role
        ).store()
      ).plus(
        ConversationModel(
          System.currentTimeMillis(),
          Content.Statement(""),
          Role.Doctor
        )
      )
    )
    isLoading.postValue(true)
  }

  // ocr 识别报告，先识别检验单，如果没有指标相关信息，再去识别c
  // 检验单:RequestHelper.MEDICAL_REPORT_DETECTION
  // 诊断报告:RequestHelper.HEALTH_REPORT,
  fun fetchMedicalResult(imagePath: String) {
    isLoading.postValue(true)
    conversation.postValue(
      (conversation.value ?: emptyList()).plus(
        ConversationModel(
          System.currentTimeMillis(),
          Content.Image(imagePath),
          Role.Patient
        ).store()
      ).plus(
        ConversationModel(
          System.currentTimeMillis(),
          Content.Statement(""),
          Role.Doctor
        )
      )
    )
    MainScope().launch(Dispatchers.IO) {
      val deviceId = DeviceUtil.getUdid(getApplication())
      var result = RequestHelper.doMedicalRequest(
        getApplication<Application>().applicationContext,
        RequestHelper.MEDICAL_REPORT_DETECTION,
        imagePath
      )
      if (haveMedicalIndicators(result)) {
        val dealResult = dealResult(RequestHelper.MEDICAL_REPORT_DETECTION, result)
        val resultData = ResultData(DataType.INDICATOR.ordinal, deviceId, dealResult)
        data.postValue(Gson().toJson(resultData))
        return@launch
      }
      // 请求诊断报告
      result = RequestHelper.doMedicalRequest(
        getApplication<Application>().applicationContext,
        RequestHelper.HEALTH_REPORT,
        imagePath
      )
      if (result == null) {
        ToastUtils.showToast(
          getApplication<Application>().applicationContext,
          "暂不支持该报告"
        )
        return@launch
      }
      val dealResult = dealResult(RequestHelper.HEALTH_REPORT, result)
      val resultData = ResultData(DataType.INFO.ordinal, deviceId, dealResult)
      data.postValue(Gson().toJson(resultData))
    }
  }


  /**
   * 判断检验报告单是否有指标数据
   */
  private fun haveMedicalIndicators(result: Any?): Boolean {
    val medicalDetectionData = result as? MedicalDetectionData
    val item = medicalDetectionData?.wordsResult?.item
    if (item.isNullOrEmpty()) {
      return false
    }
    val filterList = item.filter { innerItemList ->
      isResultNotEmpty(innerItemList)
    }
    return filterList.isNotEmpty()
  }

  /**
   * TODO 是返回所有的指标？还是异常指标？检查的记录，比如图片是应该要保存到 db,后面可以查看（报告名称，没有就显示医疗检验单或医疗诊断报告）
   * 检验单（年龄、性别、异常指标数值及范围、报告名称）
   * 诊断报告(年龄、性别、检查所见、检查提示、报告名称)
   */
  private fun dealResult(pathUrl: String, result: Any?): MutableList<MedicalData> {
    val list = mutableListOf<MedicalData>()
    if (TextUtils.equals(pathUrl, RequestHelper.MEDICAL_REPORT_DETECTION)) {
      // 检验单
      val medicalDetectionData = result as? MedicalDetectionData
      val commonData = medicalDetectionData?.wordsResult?.commonData
      // 只保留年龄、性别、报告名称
      val filterItem = DataHelper.filterItem(commonData)
      if (!filterItem.isNullOrEmpty()) {
        list.add(MedicalData(DataType.INFO.ordinal, filterItem))
      }
      val item = medicalDetectionData?.wordsResult?.item
      item?.let {
        it.forEach { indicator ->
          val isException = isIndicatorException(indicator)
          // 只添加异常指标
          if (isException) {
            val filterIndicatorItem = DataHelper.filterIndicatorItem(indicator)
            list.add(MedicalData(DataType.INDICATOR.ordinal, filterIndicatorItem, isException))
          }
        }
      }
    } else {
      // 诊断报告
      val healthReportData = result as? HealthReportData
      val wordsResult = healthReportData?.wordsResult
      val filterItem = DataHelper.filterItem(wordsResult)
      if (!filterItem.isNullOrEmpty()) {
        list.add(MedicalData(DataType.INFO.ordinal, filterItem))
      }
    }
    return list
  }

  private fun isResultNotEmpty(innerItemList: MutableList<Item>): Boolean {
    val result =
      innerItemList.find { TextUtils.equals(it.wordName, IndicatorKey.RESULT.keyName) }?.word
    return result?.isNotBlank() == true
  }

  private fun isIndicatorException(indicator: MutableList<Item>): Boolean {
    val result =
      indicator.find { TextUtils.equals(it.wordName, IndicatorKey.RESULT.keyName) }?.word
    val range =
      indicator.find { TextUtils.equals(it.wordName, IndicatorKey.RANGE.keyName) }?.word
    result ?: return false
    range ?: return false
    return !RegexUtil.isInRange(result, range, indicator)
  }

  fun getHint(it: DataType?): CharSequence {
    it?: return "请输入..."
    return when(it) {
      DataType.MEDICAL -> "输入症状推荐可能治疗的药物"
      DataType.HOSPITAL -> "输入疾病/科室推荐医院"
      DataType.FOOD -> "输入疾病，获取专属饮食食谱和建议"
      else -> "请输入..."
    }
  }
  fun getTagString(it: DataType?): CharSequence {
    it?: return ""
    return when(it) {
      DataType.MEDICAL -> "【药物查询】"
      DataType.HOSPITAL -> "【医院科室推荐】"
      DataType.FOOD -> "【饮食建议】"
      else -> ""
    }
  }
}