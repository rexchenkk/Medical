package com.mobiuspace.medical

import android.net.Uri
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide

object ViewType {
  const val DoctorStatement = 0
  const val PatientPicture = 1
  const val PatientStatement = 2
}

class DoctorResponseViewHolder(private val view: View) :
  CommonViewHolder<ConversationModel>(view, ViewType.DoctorStatement) {
  override fun onBind(position: Int, viewType: Int, item: ConversationModel) {
    itemView.findViewById<TextView>(R.id.statement)?.apply {
      text = (item.content as Content.Statement).msg
    }
  }
}

class PatientPictureViewHolder(private val view: View) :
  CommonViewHolder<ConversationModel>(view, ViewType.DoctorStatement) {
  override fun onBind(position: Int, viewType: Int, item: ConversationModel) {
    itemView.findViewById<AppCompatImageView>(R.id.image)?.apply {
      (item.content as Content.Image).let {
        Glide.with(this).load(Uri.parse(it.path)).override(600, 600).into(this)
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