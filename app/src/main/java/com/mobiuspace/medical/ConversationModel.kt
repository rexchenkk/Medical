package com.mobiuspace.medical

class ConversationModel(val time: Long, val content: Content, val owner: Role) {
}

fun ConversationModel.toViewType(): Int = when {
  owner is Role.Doctor && content is Content.Text -> ViewType.DoctorResponse
  owner is Role.Patient && content is Content.Image -> ViewType.PatientPicture
  owner is Role.Patient && content is Content.Text -> ViewType.PatientStatement
  else -> throw IllegalArgumentException("Unknown data type!")
}


open class Role {
  object Doctor: Role()
  object Patient: Role()
}

open class Content {
  class Image(val path: String): Content()
  class Text(val msg: String): Content()
}