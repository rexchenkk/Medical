package com.mobiuspace.medical

class ConversationModel(val time: Long, var content: Content, val owner: Role) {
}

fun ConversationModel.toViewType(): Int = when {
  owner is Role.Doctor && content is Content.Statement -> ViewType.DoctorStatement
  owner is Role.Patient && content is Content.Image -> ViewType.PatientPicture
  owner is Role.Patient && content is Content.Statement -> ViewType.PatientStatement
  else -> throw IllegalArgumentException("Unknown data type!")
}


open class Role {
  object Doctor: Role()
  object Patient: Role()
}

open class Content {
  class Image(val path: String): Content()
  class Statement(val msg: String): Content()
}