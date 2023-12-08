package com.mobiuspace.medical

import android.view.View

object ViewType {
  const val DoctorResponse = 0
  const val PatientPicture = 1
  const val PatientStatement = 2
}

class DoctorResponseViewHolder(private val view: View): CommonViewHolder(view, ViewType.DoctorResponse) {
}

class PatientPictureViewHolder(private val view: View): CommonViewHolder(view, ViewType.DoctorResponse) {
}

class PatientStatementViewHolder(private val view: View): CommonViewHolder(view, ViewType.DoctorResponse) {
}