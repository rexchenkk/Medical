package com.mobiuspace.medical

import android.app.Activity
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.wgw.photo.preview.PhotoPreview

object ViewType {
  const val DoctorStatement = 0
  const val PatientPicture = 1
  const val PatientStatement = 2
}

class DoctorResponseViewHolder(private val view: View) :
  CommonViewHolder<ConversationModel>(view, ViewType.DoctorStatement) {
  override fun onBind(position: Int, viewType: Int, item: ConversationModel) {
    val statement = itemView.findViewById<TextView>(R.id.statement)
    val loading = itemView.findViewById<ImageView>(R.id.loading)
    val msg = (item.content as Content.Statement).msg
    if (msg.isBlank()) {
      statement.isVisible = false
      loading.isVisible = true
      Glide.with(loading)
        .load(R.mipmap.ic_loading)
        .into(loading)
    } else {
      loading.isVisible = false
      statement?.isVisible = true
      statement?.apply {
        text = msg
      }
    }
  }
}

class PatientPictureViewHolder(private val activity: Activity, private val view: View) :
  CommonViewHolder<ConversationModel>(view, ViewType.DoctorStatement) {
  override fun onBind(position: Int, viewType: Int, item: ConversationModel) {
    itemView.findViewById<AppCompatImageView>(R.id.image)?.apply {
      (item.content as Content.Image).let { image ->
        Glide.with(this).load(Uri.parse(image.path)).override(600, 600).into(this)
        setOnClickListener {
          PhotoPreview.with(activity)
            .sources(image.path)
            .imageLoader { _, source, imageView ->
              Glide.with(this).load(Uri.parse(source as String)).into(imageView)
            }.build().show(this)
        }
      }
    }
  }
}

class PatientStatementViewHolder(private val view: View) :
  CommonViewHolder<ConversationModel>(view, ViewType.DoctorStatement) {
  override fun onBind(position: Int, viewType: Int, item: ConversationModel) {
    itemView.findViewById<TextView>(R.id.statement)?.apply {
      text = (item.content as Content.Statement).msg
    }
  }
}