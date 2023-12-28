package com.ihub.eidscan

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonNull
import com.ihub.eidscan.fragments.NfcFragment
import com.ihub.eidsdk.common.IntentData
import com.ihub.eidsdk.common.OnboardDataManager
import com.ihub.eidsdk.common.getSerializable
import com.ihub.eidsdk.data.Passport
import com.ihub.eidsdk.facade.HubFacade
import com.ihub.eidsdk.network.HubService
import com.ihub.eidsdk.network.models.PassportModel
import com.ihub.eidsdk.nfc.NfcListener
import com.ihub.eidsdk.network.RestCallback
import com.ihub.eidsdk.utils.StringUtils
import org.jmrtd.VerificationStatus
import org.jmrtd.lds.icao.MRZInfo

class NfcActivity : AppCompatActivity(), NfcListener {

    private var mrzInfo: MRZInfo? = null
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc)
        HubService.getInstance().init("<BaseUrl>","<api-key>", "<CUS_CODE>")
        if (intent.hasExtra(IntentData.KEY_MRZ_INFO)) {
            mrzInfo = intent.getSerializable(IntentData.KEY_MRZ_INFO, MRZInfo::class.java)
        } else {
            onBackPressed()
        }
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, getString(R.string.warning_no_nfc), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE)
        } else{
            PendingIntent.getActivity(this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_IMMUTABLE)
        }
        if (null == savedInstanceState) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, NfcFragment.newInstance(mrzInfo!!), TAG_NFC)
                .commit()
        }
    }
    public override fun onNewIntent(intent: Intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action || NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            // drop NFC events
            handleIntent(intent)
        }else{
            super.onNewIntent(intent)
        }
    }
    private fun handleIntent(intent: Intent) {
        val fragmentByTag = supportFragmentManager.findFragmentByTag(TAG_NFC)
        if (fragmentByTag is NfcFragment) {
            fragmentByTag.handleNfcTag(intent)
        }
    }
    override fun onEnableNfc() {
        if (nfcAdapter != null) {
            if (!nfcAdapter!!.isEnabled)
                showWirelessSettings()

            nfcAdapter!!.enableForegroundDispatch(this, pendingIntent, null, null)
        }
    }

    override fun onDisableNfc() {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter.disableForegroundDispatch(this)
    }

    override fun onEidRead(passport: Passport?) {
        passport?.let { it ->
            val eidNumber = it.personOptionalDetail?.eidNumber
            val dsCert = StringUtils.encodeToBase64String(it.sodFile?.docSigningCertificate!!)
            val province = StringUtils.getProvince(it.personOptionalDetail?.placeOfOrigin)
            HubService.getInstance().verifyEid(eidNumber, dsCert, province, object : RestCallback<PassportModel> {
                override fun onSuccess(model: PassportModel?) {
                    if (model?.isValidIdCard == true) {
                        it.verificationStatus?.setDS(VerificationStatus.Verdict.SUCCEEDED, "DS_CERT Invalid")
                    } else {
                        it.verificationStatus?.setDS(VerificationStatus.Verdict.FAILED, "DS_CERT Valid")
                    }
                    val respondsMsg = Gson().toJson(model?.respond ?: JsonNull.INSTANCE)
                    val signature = model?.signature ?: ""
                    if (!HubFacade.verifyRsaSignatureDefault(baseContext, respondsMsg, signature)) {
                        it.verificationStatus?.setCS(VerificationStatus.Verdict.FAILED, "SIGNATURE Invalid", null)
                    } else {
                        it.verificationStatus?.setCS(VerificationStatus.Verdict.SUCCEEDED, "SIGNATURE Valid", null)
                    }
                    OnboardDataManager.shared.passport = it;
                    val intent = Intent(this@NfcActivity, ConfirmPersonalInfoActivity::class.java)
                    startActivity(intent)
                }
                override fun onError(error: String) {
                    Toast.makeText(this@NfcActivity, error, Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    override fun onCardException(cardException: Exception?) {
        Toast.makeText(this, cardException.toString(), Toast.LENGTH_SHORT).show();
        onBackPressed();
    }

    private fun showWirelessSettings() {
        Toast.makeText(this, getString(R.string.warning_enable_nfc), Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        startActivity(intent)
    }


    companion object {
        private val TAG = NfcActivity::class.java.simpleName
        private const val TAG_NFC = "TAG_NFC"
    }
}