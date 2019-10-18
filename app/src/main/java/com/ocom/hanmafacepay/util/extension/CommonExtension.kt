package com.ocom.hanmafacepay.util.extension

import android.util.Base64
import com.google.gson.Gson
import com.ocom.hanmafacepay.FacePayApplication
import com.ocom.hanmafacepay.network.AutoEnumValue
import com.ocom.hanmafacepay.network.AutoField
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.reflect.Field
import java.nio.charset.Charset

/**
 * 将base64转为数组
 */
fun String.base64ToByteArray(): ByteArray? {
    if (this.isEmpty()) {
        return null
    }
    val file = File(FacePayApplication.INSTANCE.filesDir, this)
    val stream = FileInputStream(file)
    val b = ByteArray(stream.available())
    stream.read(b)
    val content = b.toString(Charset.forName("UTF-8"))
    stream.close()
    val imgBase64 = content.replaceFirst("data:image/jpeg;base64,", "")
    return Base64.decode(imgBase64, Base64.DEFAULT)
}

fun InputStream.toFileAsync(path: String) = GlobalScope.async {
    val stream = File(path).outputStream()
    try {
        copyTo(stream, 4096)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        stream.close()
        close()
    }
}

/**
 * 直接转为json格式
 */
inline fun <reified T> T.autoBody():RequestBody{
    val json = Gson().toJson(this, T::class.java)
//    log("请求json: $json")
    return RequestBody.create(MediaType.parse("application/json"),
        json
    );
}




/**
 * 将注解直接解释为列表
 */
inline fun <reified T> T.autoPart(): List<MultipartBody.Part>? {
    val handlerType: Class<T> = T::class.java
    val fields = handlerType.declaredFields
    if (fields.isNotEmpty()) {
        Field.setAccessible(fields, true)
        val parts = ArrayList<MultipartBody.Part>()
        for (field in fields) {
            try {
                val autoField = field.getAnnotation(AutoField::class.java)
                if (autoField != null) {
                    val value = field.get(this) ?: continue
                    val key = autoField.value
                    if (value is Enum<*>) {
                        //如果对象是枚举
                        val declaredFields = value.javaClass.declaredFields
                        Field.setAccessible(declaredFields, true)
                        for (enumField in declaredFields) {
                            val annotation = enumField.getAnnotation(AutoEnumValue::class.java)
                            if (annotation != null) {
                                val enumValue = enumField.get(value)
                                if (enumValue != null) {
                                    parts.add(MultipartBody.Part.createFormData(key, enumValue.toString()))
                                    break
                                }
                            }
                        }
                    } else {
                        parts.add(MultipartBody.Part.createFormData(key, value.toString()))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return parts
    }
    return null
}