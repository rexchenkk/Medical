package com.mobiuspace.medical
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Entity(tableName = "conversation")
@TypeConverters(RoleConverters::class, ContentConverters::class)
class ConversationModel(
  @ColumnInfo(name = "time")
  val time: Long,
  @ColumnInfo(name = "content")
  var content: Content,
  @ColumnInfo(name = "owner")
  val owner: Role,
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  val id: Int = 0
)

fun ConversationModel.toViewType(): Int = when {
  owner is Role.Doctor && content is Content.Statement -> ViewType.DoctorStatement
  owner is Role.Patient && content is Content.Image -> ViewType.PatientPicture
  owner is Role.Patient && content is Content.Statement -> ViewType.PatientStatement
  else -> throw IllegalArgumentException("Unknown data type!")
}

open class Role {
  object Doctor : Role()
  object Patient : Role()
}

class RoleConverters{
  @TypeConverter
  fun stringToRole(value: String): Role {
    return when(value) {
      "Doctor" -> Role.Doctor
      else -> Role.Patient
    }
  }

  @TypeConverter
  fun roleToString(role: Role): String {
    return when(role) {
      is Role.Patient -> "Patient"
      else -> "Doctor"
    }
  }
}

open class Content {
  class Image(val path: String) : Content()
  class Statement(val msg: String) : Content()
}

class ContentConverters{
  @TypeConverter
  fun stringToContent(value: String): Content {
    return when {
      value.startsWith("path") -> Content.Image(value.removePrefix("path#"))
      else -> Content.Statement(value.removePrefix("statement#"))
    }
  }

  @TypeConverter
  fun contentToString(content: Content): String {
    return when(content) {
      is Content.Image -> "path#${content.path}"
      is Content.Statement -> "statement#${content.msg}"
      else -> throw IllegalArgumentException("Unknown content!")
    }
  }
}