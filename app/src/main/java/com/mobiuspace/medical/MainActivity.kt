package com.mobiuspace.medical

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
import com.medical.expert.data.ResultData
import com.medical.expert.data.ResultTextData
import com.medical.expert.viewmodel.MainViewModel
import com.mobiuspace.medical.data.key.DataType
import com.mobiuspace.medical.databinding.ActivityMainBinding
import com.mobiuspace.medical.glide.GlideEngine
import com.mobiuspace.medical.helper.WSManager
import com.mobiuspace.medical.helper.WSManager.WebSocketDataListener
import com.mobiuspace.medical.utils.DeviceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
  private val TAG = "MainActivity"
  private lateinit var viewModel: MainViewModel
  private lateinit var binding: ActivityMainBinding
  private val listener = object : WebSocketDataListener {
    override fun onWebSocketData(type: Int, data: String?) {
       Log.e(TAG, "收到消息=$data")
      ToastUtils.showToast(applicationContext, data)
    }
  }
  private var conversation: MutableList<ConversationModel> = mutableListOf()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
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
            ViewType.PatientPicture -> PatientPictureViewHolder(view)
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
        onItemViewType {
          conversation[it].toViewType()
        }
      }
    }
    binding.camera.setOnClickListener {
      openPictureSelector()
    }
    binding.send.setOnClickListener {
      // 发送消息
      sendToGPT()
    }
    viewModel.data.observe(this) {
      WSManager.getInstance(applicationContext).send(it)
      Log.d(TAG, "onCreate: resultJson=${it}")
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
        DataType.TEXT.ordinal,
        DeviceUtil.getAndroidId(applicationContext),
        content1
      )
      WSManager.getInstance(applicationContext).send(Gson().toJson(textData))
      conversation.add(ConversationModel(System.currentTimeMillis(), Content.Statement(content1),
        Role.Patient))
      withContext(Dispatchers.Main) {
        binding.conversation.adapter?.notifyDataSetChanged()
      }
    }
  }

  private fun initSocket() {
    WSManager.getInstance(applicationContext).registerWSDataListener(listener)
//    WSManager.getInstance(applicationContext).init("ws://47.90.136.35.nip.io/chat")
    WSManager.getInstance(applicationContext).init("ws://10.128.62.15:8000/chat")
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