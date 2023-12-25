package com.ihub.eidscan.fragments

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ihub.eidscan.R
import com.ihub.eidscan.databinding.FragmentNfcBinding
import com.ihub.eidsdk.common.IntentData
import com.ihub.eidsdk.common.getSerialize
import com.ihub.eidsdk.data.Passport
import com.ihub.eidsdk.facade.EidCallback
import com.ihub.eidsdk.facade.HubFacade
import com.ihub.eidsdk.nfc.NfcListener
import io.reactivex.disposables.CompositeDisposable
import net.sf.scuba.smartcards.CardServiceException
import net.sf.scuba.smartcards.ISO7816
import org.jmrtd.BACDeniedException
import org.jmrtd.PACEException
import org.jmrtd.lds.icao.MRZInfo
import java.security.Security

class NfcFragment: androidx.fragment.app.Fragment() {

    private var mrzInfo: MRZInfo? = null
    private var nfcListener: NfcListener? = null
    private var binding: FragmentNfcBinding? = null

    internal var mHandler = Handler(Looper.getMainLooper())
    var disposable = CompositeDisposable()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNfcBinding.inflate(inflater, container, false)
        binding?.lheader?.tvTitleHeader?.text = getString(R.string.step_2_scan_nfc)
        binding!!.lheader.mcvBack.setOnClickListener {
            activity?.finish()
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val arguments = arguments
        if (arguments!!.containsKey(IntentData.KEY_MRZ_INFO)) {
            mrzInfo = arguments.getSerialize(IntentData.KEY_MRZ_INFO, MRZInfo::class.java)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = activity
        if (activity is NfcListener) {
            nfcListener = activity
        }
    }
    override fun onDetach() {
        nfcListener = null
        super.onDetach()
    }

    override fun onResume() {
        super.onResume()
        binding?.valuePassportNumber?.text =
            getString(R.string.doc_number, mrzInfo!!.documentNumber)
        binding?.valueDOB?.text = getString(R.string.doc_dob, mrzInfo!!.dateOfBirth)
        binding?.valueExpirationDate?.text = getString(R.string.doc_expiry, mrzInfo!!.dateOfExpiry)
        nfcListener?.onEnableNfc()
    }

    override fun onPause() {
        super.onPause()
        nfcListener?.onDisableNfc()
    }
    override fun onDestroyView() {
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
        binding = null
        super.onDestroyView()
    }

    private fun onNFCReadStart() {
        Log.d(TAG, "onNFCSReadStart")
        mHandler.post {
            binding?.progressBar?.visibility = View.VISIBLE
        }
    }

    private fun onNFCReadFinish() {
        Log.d(TAG, "onNFCReadFinish")
        mHandler.post { binding?.progressBar?.visibility = View.GONE }
    }


    fun onEidRead(passport: Passport?) {
        mHandler.post {
            nfcListener?.onEidRead(passport)
        }
    }

    private fun onCardException(cardException: Exception?) {
        mHandler.post {
           nfcListener?.onCardException(cardException)
        }
    }

    fun handleNfcTag(intent: Intent?) {
        if (intent == null || intent.extras == null) {
            return
        }
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return

        val subscribe = HubFacade.handleDocumentNfcTag(
            requireContext(),
            tag,
            mrzInfo!!,
            object : EidCallback {

                override fun onEidReadStart() {
                    onNFCReadStart()
                }

                override fun onEidReadFinish() {
                    onNFCReadFinish()
                }

                override fun onEidRead(passport: Passport?) {
                    this@NfcFragment.onEidRead(passport)
                }

                override fun onAccessDeniedException(exception: org.jmrtd.AccessDeniedException) {
                    Toast.makeText(
                        context,
                        getString(R.string.warning_authentication_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                    exception.printStackTrace()
                    this@NfcFragment.onCardException(exception)
                }

                override fun onBACDeniedException(exception: BACDeniedException) {
                    Toast.makeText(context, exception.toString(), Toast.LENGTH_SHORT).show()
                    onCardException(exception)
                }

                override fun onPACEException(exception: PACEException) {
                    Toast.makeText(context, exception.toString(), Toast.LENGTH_SHORT).show()
                    this@NfcFragment.onCardException(exception)
                }

                override fun onCardException(exception: CardServiceException) {
                    val sw = exception.sw.toShort()
                    when (sw) {
                        ISO7816.SW_CLA_NOT_SUPPORTED -> {
                            Toast.makeText(
                                context,
                                getString(R.string.warning_cla_not_supported),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            Toast.makeText(context, exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                    this@NfcFragment.onCardException(exception)
                }

                override fun onGeneralException(exception: Exception?) {
                    Toast.makeText(context, exception!!.toString(), Toast.LENGTH_SHORT).show()
                    this@NfcFragment.onCardException(exception)
                }
            })
        disposable.add(subscribe)
    }


    companion object {
        private val TAG = NfcFragment::class.java.simpleName

        init {
            Security.insertProviderAt(org.spongycastle.jce.provider.BouncyCastleProvider(), 1)
        }

        fun newInstance(mrzInfo: MRZInfo): NfcFragment {
            val myFragment = NfcFragment()
            val args = Bundle()
            args.putSerializable(IntentData.KEY_MRZ_INFO, mrzInfo)
            myFragment.arguments = args
            return myFragment
        }
    }
}