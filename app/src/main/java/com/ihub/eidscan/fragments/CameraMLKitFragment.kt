package com.ihub.eidscan.fragments

import android.content.pm.PackageManager
import android.graphics.Outline
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import com.ihub.eidscan.R
import com.ihub.eidscan.databinding.FragmentCameraBinding
import com.ihub.eidsdk.facade.HubFacade
import io.fotoapparat.preview.FrameProcessor
import io.fotoapparat.view.CameraView

class CameraMLKitFragment : CameraFragment() {

    private var binding: FragmentCameraBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        binding!!.lheader.tvTitleHeader.text = getString(R.string.step_1_scan_mrz)
        binding!!.framePreview.clipToOutline = true
        binding!!.framePreview.outlineProvider = object: ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                view?.let {
                    outline?.setRoundRect(0, 0, it.width, (view.height), 16F)
                }
            }
        }
        binding!!.lheader.mcvBack.setOnClickListener {
            activity?.finish()
        }
        return binding?.root
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override val callbackFrameProcessor: FrameProcessor
        get() = HubFacade.getCallbackFrameProcessor(0)

    override val cameraPreview: CameraView
        get() = binding?.cameraPreview!!

    override val requestedPermissions: ArrayList<String>
        get() = ArrayList()

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.permission_camera_rationale))
                    .show(childFragmentManager, FRAGMENT_DIALOG)
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
    override fun onRequestPermissionsResult(
        permissionsDenied: ArrayList<String>,
        permissionsGranted: ArrayList<String>
    ) {
    }

    companion object {
        private val TAG = CameraMLKitFragment::class.java.simpleName

        private val REQUEST_CAMERA_PERMISSION = 1
        private val FRAGMENT_DIALOG = "CameraMLKitFragment"

        fun newInstance(): CameraMLKitFragment {
            return CameraMLKitFragment()

        }
    }
}