package com.example.loginapp.utility

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.loginapp.R
import com.google.android.material.snackbar.Snackbar
import java.util.regex.Pattern

class Utils {

    /*companion object is equivalent of static in java
    * we can add any number of methods inside this*/
    companion object {
        /*used - to validate pasword*/
        fun validateEditTextEmpty_password(editText: EditText, passwordStrength:Int, requireActivity: FragmentActivity) {
            if (!requireActivity.isFinishing()) {
                if (editText.text.toString().trim { it <= ' ' }.isEmpty()) {
                    editText.error = "This field is required"
                }
                else {
                    editText.error = null
                }
                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence, start: Int, count: Int, after: Int,
                    ) {
                    }

                    override fun onTextChanged(s:CharSequence,start:Int,before:Int,count:Int) {
                        if (s.length > 1 && passwordStrength != 4) {
                            editText.error = "Password strength must be strong"
                        }
                        else {
                            editText.error = null
                        }
                    }

                    override fun afterTextChanged(s: Editable) {}
                })
            }
        }

        //checking for valid email
        fun isValidEmail(email:String):Boolean {
            val pattern = Pattern.compile("^.+@.+\\..+$")
            val matcher = pattern.matcher(email)
            return matcher.matches()
        }

        /*used - to display snackbar at top*/
        fun displaySnackBarAtTop(activity:Activity,msg:String) {
            Snackbar.make(activity.findViewById(android.R.id.content),msg, Snackbar.LENGTH_LONG).show();
        }

    }
}