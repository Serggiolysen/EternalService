package com.lysen.smsparsing

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.lysen.smsparsing.api.ApiSender
import com.lysen.smsparsing.databinding.FragmentImeiBinding

class ImeiDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentImeiBinding
    lateinit var imeiSetContract: ImeiSetContract
    var index=-1

    override fun onResume() {
        super.onResume()
        isCancelable = false
        dialog?.window?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.warning_frag_back))

        binding.tvGetImei.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel: *${Uri.encode("#")}06${Uri.encode("#")}")))
        }

        binding.logOutNo.setOnClickListener { dismiss() }

        binding.logOutYes.setOnClickListener {
            if (::imeiSetContract.isInitialized && binding.emeiET.text?.isNotEmpty() == true && binding.tokenImeiET.text?.isNotEmpty() == true) {
                imeiSetContract.onImeiSet(imei = binding.emeiET.text.toString(), token = binding.tokenImeiET.text.toString(), index = index)
                dismiss()
            }
        }

        if (App.getToken()?.isNotEmpty() == true) {
            binding.tokenImeiET.setText(App.getToken().toString())
            binding.tokenImeiET.isEnabled = false
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentImeiBinding.inflate(LayoutInflater.from(requireContext()))
        return AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .create()
    }

}




