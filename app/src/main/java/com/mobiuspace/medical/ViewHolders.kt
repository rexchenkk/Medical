package com.mobiuspace.medical

import android.view.View
import android.widget.TextView

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
    TODO("Not yet implemented")
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