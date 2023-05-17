package com.lysen.smsparsing

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.lysen.smsparsing.api.ApiSender
import com.lysen.smsparsing.databinding.FragmentWarningtBinding

class WarningDialogFragment : DialogFragment(), ApiSender.ApiCallback {
    private lateinit var binding: FragmentWarningtBinding
    lateinit var tokenSendContract: TokenSendContract

    override fun onResume() {
        super.onResume()
        isCancelable = false
        dialog?.window?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.warning_frag_back))

        binding.logOutNo.setOnClickListener {
            if (::tokenSendContract.isInitialized)
                tokenSendContract.onTokenCancel()
            dismiss()
        }

        binding.logOutYes.setOnClickListener {
            if (::tokenSendContract.isInitialized && binding.tokenET.text?.isNotEmpty() == true){
                App.saveToken(binding.tokenET.text.toString())
                ApiSender.sendToApi(apiCallback = this)
                binding.progressShield.visibility = View.VISIBLE
            }
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentWarningtBinding.inflate(LayoutInflater.from(requireContext()))
        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .create()
    }

    override fun onApiSuccess() {
        tokenSendContract.onTokenSend(binding.tokenET.text.toString())
        dismiss()
    }

    override fun onApiError() {
        binding.progressShield.visibility = View.GONE
        Toast.makeText(requireContext(), "Is token correct?", Toast.LENGTH_LONG).show()
    }

}




