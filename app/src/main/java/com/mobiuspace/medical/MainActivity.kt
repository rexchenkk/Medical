package com.mobiuspace.medical

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.utils.ToastUtils
import com.medical.expert.data.ResultTextData
import com.mobiuspace.medical.viewmodel.MainViewModel
import com.mobiuspace.medical.data.key.DataType
import com.mobiuspace.medical.databinding.ActivityMainBinding
import com.mobiuspace.medical.glide.GlideEngine
import com.mobiuspace.medical.helper.WSManager
import com.mobiuspace.medical.helper.WSManager.WebSocketDataListener
import com.mobiuspace.medical.utils.DeviceUtil
import com.mobiuspace.medical.utils.transparentNavBar
import com.mobiuspace.medical.utils.transparentStatusBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {
  private val TAG = "MainActivity"
  private lateinit var viewModel: MainViewModel
  private lateinit var binding: ActivityMainBinding
  private val listener = WebSocketDataListener { _, data ->
    Log.e(TAG, "收到消息=$data")
    data?.let {
      viewModel.receiveFromGPT(it, Role.Doctor)
    }
  }
  private var conversation: List<ConversationModel> by Delegates.observable(mutableListOf()) { _, old, new ->
    binding.conversation.adapter?.let {
      it.notifyDataSetChanged()
      val linearLayoutManager = binding.conversation.layoutManager as? LinearLayoutManager
      linearLayoutManager?.scrollToPosition(it.itemCount - 1)
//      if (it.itemCount > 1 && linearLayoutManager?.stackFromEnd == false ) {
//        Log.d(TAG, ": stackFromEnd-----setting--")
//        linearLayoutManager.stackFromEnd = true
//      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    transparentStatusBar()
    transparentNavBar()
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    initSocket()
    viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    binding.conversation.apply {
      layoutManager = LinearLayoutManager(this@MainActivity)
      adapter = CommonRecyclerAdapter<ConversationModel> {
        onCount { conversation.size }
        onCreateViewHolder { view, viewType ->
          when(viewType) {
            ViewType.PatientStatement -> PatientStatementViewHolder(view)
            ViewType.PatientPicture -> PatientPictureViewHolder(this@MainActivity, view)
            ViewType.DoctorStatement -> DoctorResponseViewHolder(view)
            else -> throw IllegalArgumentException("Invalid view type!")
          }
        }
        onLayout {
          when(it) {
            ViewType.PatientStatement -> R.layout.item_patient_statement
            ViewType.PatientPicture -> R.layout.item_patient_image
            ViewType.DoctorStatement -> R.layout.item_doctor_statement
            else -> throw IllegalArgumentException("Invalid view type!")
          }
        }
        onItem{ conversation[it] }
        onItemViewType {
          conversation[it].toViewType()
        }
      }
    }
    (binding.conversation.layoutManager as LinearLayoutManager).stackFromEnd = true
    binding.camera.setOnClickListener {
      viewModel.dataType.value = DataType.TEXT
      openPictureSelector()
    }
    binding.send.setOnClickListener {
      // 正在 loading,提示
      if (viewModel.isLoading.value == true) {
        ToastUtils.showToast(applicationContext, "华佗拼命思考中,请稍等！" )
        return@setOnClickListener
      }
      // 发送消息
      sendToGPT()
    }
    viewModel.data.observe(this) {
      WSManager.getInstance(applicationContext).send(it)
      Log.d(TAG, "onCreate: resultJson=${it}")
    }
    viewModel.conversation.observe(this) {
      conversation = it
    }
    viewModel.dataType.observe(this) {
      binding.content.hint = viewModel.getHint(it)
      val tagString = viewModel.getTagString(it)
      binding.tagContainer.isVisible = tagString.isNotBlank()
      binding.tag.text = tagString
    }
    initPanelEvent()
  }

  private fun initPanelEvent() {
    binding.tagClose.setOnClickListener{
      viewModel.dataType.value = DataType.TEXT
    }
    binding.medical.setOnClickListener {
      viewModel.dataType.value = DataType.MEDICAL
    }
    binding.hospital.setOnClickListener {
      viewModel.dataType.value = DataType.HOSPITAL
    }
    binding.food.setOnClickListener {
      viewModel.dataType.value = DataType.FOOD
    }
  }

  private fun sendToGPT() {
    val content = binding.content.text?.trim()
    if (content.isNullOrBlank()) {
      return
    }
    Log.d(TAG, "onCreate: 发送了-$content")
    binding.content.setText("")
    lifecycleScope.launch(Dispatchers.IO) {
      val content1 = content.toString()
      val textData = ResultTextData(
        viewModel.dataType.value?.ordinal ?: DataType.TEXT.ordinal,
        DeviceUtil.getUdid(applicationContext),
        content1
      )
      val toJson = Gson().toJson(textData)
      Log.d(TAG, "sendToGPT: tojson=$toJson")
      WSManager.getInstance(applicationContext).send(toJson)
      viewModel.sendToGPT(content1, Role.Patient)
    }
  }

  private fun initSocket() {
    WSManager.getInstance(applicationContext).registerWSDataListener(listener)
    WSManager.getInstance(applicationContext).init("ws://8.217.208.220.nip.io/chat")
//    WSManager.getInstance(applicationContext).init("ws://47.90.136.35.nip.io/chat")
//    WSManager.getInstance(applicationContext).init("ws://10.128.62.15:8000/chat")
  }


  private fun openPictureSelector() {
    PictureSelector.create(this)
      .openGallery(SelectMimeType.ofImage())
      .setImageEngine(GlideEngine.createGlideEngine())
      .setSelectionMode(SelectModeConfig.SINGLE)
      .forResult(object : OnResultCallbackListener<LocalMedia?> {
        override fun onResult(result: ArrayList<LocalMedia?>?) {
          val path = result?.get(0)?.path
          Log.d(TAG, "onResult: $path")
          path?.let {
            viewModel.fetchMedicalResult(path)
          }?: ToastUtils.showToast(this@MainActivity, "选择图片失败，请重试！")
        }
        override fun onCancel() {}
      })
  }


  override fun onDestroy() {
    WSManager.getInstance(applicationContext).unregisterWSDataListener(listener)
    Log.d(TAG, "onDestroy: ----")
    super.onDestroy()
  }
}