package com.mobiuspace.medical

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.utils.ToastUtils
import com.medical.expert.viewmodel.MainViewModel
import com.mobiuspace.medical.databinding.ActivityMainBinding
import com.mobiuspace.medical.glide.GlideEngine
import com.mobiuspace.medical.helper.WSManager
import com.mobiuspace.medical.helper.WSManager.WebSocketDataListener

class MainActivity : AppCompatActivity() {
  private val TAG = "MainActivity"
  private lateinit var viewModel: MainViewModel
  private lateinit var binding: ActivityMainBinding
  private val listener = object : WebSocketDataListener {
    override fun onWebSocketData(type: Int, data: String?) {
       Log.e(TAG, "收到消息=$data")
    }
  }
  private var conversation: List<ConversationModel> = mutableListOf()

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
//        onLayout {
//
//        }
//        onItemViewType {
//          conversation.get(it)
//        }
      }
    }
    binding.camera.setOnClickListener {
      openPictureSelector()
    }
    viewModel.data.observe(this) {
      WSManager.getInstance(applicationContext).send(it)
      Log.d(TAG, "onCreate: resultJson=${it}")
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
    WSManager.getInstance(applicationContext).disconnect(1000, "页面销毁")
    WSManager.getInstance(applicationContext).unregisterWSDataListener(listener)
    Log.d(TAG, "onDestroy: ----")
    super.onDestroy()
  }
}