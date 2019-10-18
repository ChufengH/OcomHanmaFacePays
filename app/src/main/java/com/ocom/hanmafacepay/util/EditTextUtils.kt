package com.ocom.hanmafacepay.util

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText


object EditTextUtils {
    private val MAX = 5 //最多输入位数
    /**
     * 设置edittext只能输入小数点后两位
     */
    fun afterDotTwo(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var s = s
                // 限制最多能输入5位整数
                if (s.toString().contains(".")) {
                    if (s.toString().indexOf(".") > MAX) {
                        s = s.toString().subSequence(
                            0,
                            MAX
                        ).toString() + s.toString().substring(s.toString().indexOf("."))
                        editText.setText(s)
                        editText.setSelection(MAX)
                    }
                } else {
                    if (s.toString().length > MAX) {
                        s = s.toString().subSequence(0, MAX)
                        editText.setText(s)
                        editText.setSelection(MAX)
                    }
                }
                // 判断小数点后只能输入两位
                if (s.toString().contains(".")) {
                    if (s.length - 1 - s.toString().indexOf(".") > 2) {
                        s = s.toString().subSequence(
                            0,
                            s.toString().indexOf(".") + 3
                        )
                        editText.setText(s)
                        editText.setSelection(s.length)
                    }
                }
                //如果第一个数字为0，第二个不为点，就不允许输入
                if (s.toString().startsWith("0") && s.toString().trim { it <= ' ' }.length > 1) {
                    if (s.toString().substring(1, 2) != ".") {
                        editText.setText(s.subSequence(0, 1))
                        editText.setSelection(1)
                        return
                    }
                }

                //检查多个小数点
                val number= s.length - s.replace(Regex("\\."),"").length
                if ((number) >1){
                    editText.setText(s.subSequence(0,s.length- 1))
                    return
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(s: Editable) {
                if (editText.text.toString().trim { it <= ' ' } != "") {
                    if (editText.text.toString().trim { it <= ' ' }.substring(0, 1) == ".") {
                        editText.setText("0" + editText.text.toString().trim { it <= ' ' })
                        editText.setSelection(2)
                    }
                }
            }
        })
    }

}