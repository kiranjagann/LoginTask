package com.example.loginapp.views

import android.os.Bundle
import android.os.Handler
import android.text.method.PasswordTransformationMethod
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.loginapp.R
import com.example.loginapp.utility.Utils
import com.example.loginapp.databinding.LoginBinding
import java.util.regex.Pattern

class LoginFragment : Fragment() {

    private var mIsPasswordVisible: Boolean = true
    private lateinit var mBinding: LoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //init
        mBinding = LoginBinding.inflate(inflater, container, false)
        setOnClickListeners()
        return mBinding.root
    }

    /*View binding click's listeners for all click in this class*/
    private fun setOnClickListeners() {
        mBinding.btnSignin.setOnClickListener {
            signIn()
        }
        mBinding.viewPasswordImgView.setOnClickListener {
            passwordVisibility(mBinding.viewPasswordImgView)
            val position = mBinding.edtPassword.length()
            mBinding.edtPassword.setSelection(position)
        }
        mBinding.imgInfoPassword.setOnClickListener {
            passwordInfo(it)
        }
    }

    /*
     * used - to show and hide password
     */
    private fun passwordVisibility(imageView:ImageView) {
        if (mIsPasswordVisible) {
            mIsPasswordVisible = false
            imageView.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.ic_icon_visibility_on
                )
            )
            mBinding.edtPassword.setTransformationMethod(null)
        } else {
            mIsPasswordVisible = true
            imageView.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.ic_icon_visibility_off
                )
            )
            mBinding.edtPassword.setTransformationMethod(PasswordTransformationMethod())
        }
    }

    /*used - to execute sign in*/
    private fun signIn() {
        if (isMandatoryFieldsNotFilled()) {
            displayErrorMessages()
        } else {
            if(evaluatePasswordCases() != 4){
                Utils.displaySnackBarAtTop(requireActivity(), "Password Strength must be Strong")
            }else{
                navigateToMapsScreen()
            }
        }
    }

    /*used - to show error messages if mandatory fields are not filled*/
    private fun displayErrorMessages() {
        if (isNotValidEmail()) {
            Utils.displaySnackBarAtTop(requireActivity(), "Please Enter Valid Email Address")
        }
        Utils.validateEditTextEmpty_password(
            mBinding.edtPassword,
            evaluatePasswordCases(),
            requireActivity()
        )
    }

    /**
     * Used For : checking all mandatory fields
     */
    private fun isMandatoryFieldsNotFilled(): Boolean {
        return (mBinding.edtPassword.getText().toString().trim().isEmpty()
                || isNotValidEmail())
    }

    /*used - to check whether email radio button selecyed & email entered and is valid email*/
    private fun isNotValidEmail() = !Utils.isValidEmail(
        mBinding.edtEmailOrPhone.getText().toString().trim()
    )

    private fun navigateToMapsScreen() {
        val bundle = Bundle()
        bundle.putString("login_id",mBinding.edtEmailOrPhone.text.toString())
        val navController = Navigation.findNavController(requireActivity(),
            R.id.nav_host_fragment_content_main
        )
        navController.navigate(R.id.action_login_to_maps,bundle)
    }

    /**
     * Method Name : passwordInfo
     * Used For : to show password info
     *
     * @param view
     */
    private fun passwordInfo(view:View?) {
        evaluatePasswordCases()
        mBinding.passwordInfo.setVisibility(View.VISIBLE)
        val handler = Handler()
        handler.postDelayed({ mBinding.passwordInfo.setVisibility(View.GONE) }, 5000)
    }

    /**
     * Used For : to show user which all conditions are correct
     *
     * @param
     */
    private fun evaluatePasswordCases(): Int {
        var count = 0
        val inputRegexes = arrayOfNulls<Pattern>(4)
        inputRegexes[0] = Pattern.compile(".*[A-Z].*")
        inputRegexes[1] = Pattern.compile(".*[0-9].*")
        inputRegexes[2] = Pattern.compile(".*[@!#&*%].*")
        if (inputRegexes[0]!!.matcher(mBinding.edtPassword.getText().toString()).find()) {
            mBinding.atleastOneUppercase.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_password_check_green),
                null
            )
            count++
        } else {
            mBinding.atleastOneUppercase.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_password_check_red),
                null
            )
            count--
        }
        if (inputRegexes[1]!!.matcher(mBinding.edtPassword.getText().toString()).find()) {
            mBinding.atleastOneDigit.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_password_check_green),
                null
            )
            count++
        } else {
            mBinding.atleastOneDigit.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_password_check_red),
                null
            )
            count--
        }
        if (inputRegexes[2]!!.matcher(mBinding.edtPassword.getText().toString()).find()) {
            mBinding.atleastOneSpclChar.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_password_check_green),
                null
            )
            count++
        } else {
            mBinding.atleastOneSpclChar.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_password_check_red),
                null
            )
            count--
        }
        if (mBinding.edtPassword.getText().toString().length >= 8) {
            mBinding.minimumEightChars.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_password_check_green),
                null
            )
            count++
        } else {
            mBinding.minimumEightChars.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_password_check_red),
                null
            )
            count--
        }
        return count
    }

}