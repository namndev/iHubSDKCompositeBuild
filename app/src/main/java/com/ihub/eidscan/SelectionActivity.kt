package com.ihub.eidscan

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import org.jmrtd.lds.icao.MRZInfo
import androidx.appcompat.widget.AppCompatEditText
import com.ihub.eidscan.databinding.ActivitySelectionBinding
import com.ihub.eidsdk.common.IntentData
import com.ihub.eidsdk.common.getSerializable
import com.ihub.eidsdk.validators.DateRule
import com.ihub.eidsdk.validators.DocumentNumberRule
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import net.sf.scuba.data.Gender

class SelectionActivity : AppCompatActivity(), Validator.ValidationListener {

    private var binding: ActivitySelectionBinding?=null
    private var docInput: AppCompatEditText? = null
    private var dateOfBirthInput : AppCompatEditText? = null
    private var expiredInput: AppCompatEditText? = null
    //
    private var mValidator: Validator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        docInput = binding?.documentNumber
        dateOfBirthInput = binding?.documentDateOfBirth
        expiredInput = binding?.documentExpiration
        //
        mValidator = Validator(this)
        mValidator!!.setValidationListener(this)
        mValidator!!.put(docInput!!, DocumentNumberRule())
        mValidator!!.put(expiredInput!!, DateRule())
        mValidator!!.put(dateOfBirthInput!!, DateRule())
        //
        binding?.buttonReadNfc?.setOnClickListener { validateFields() }
        binding!!.buttonReadTest.visibility = View.GONE
        binding!!.buttonReadTest.setOnClickListener { test() }
        binding!!.radioButtonDataEntry.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonManual -> {
                    binding!!.layoutManual.visibility = View.VISIBLE
                    binding!!.layoutAutomatic.visibility = View.GONE
                }
                R.id.radioButtonOcr -> {
                    binding!!.layoutManual.visibility = View.GONE
                    binding!!.layoutAutomatic.visibility = View.VISIBLE
                    onMrzRequest()
                }
            }
        }
    }
    fun selectManualToggle() {
        binding!!.radioButtonDataEntry.check(R.id.radioButtonManual)
    }
    protected fun test() {
        //Method to test NFC without rely into the Camera
        val TEST_LINE_1 = "<LINE1>"
        val TEST_LINE_2 = "<LINE2>"
        val TEST_LINE_3 = "<LINE3>>"

        val mrzInfo = MRZInfo(TEST_LINE_1 + "\n" + TEST_LINE_2+"\n"+TEST_LINE_3)
        onEidRead(mrzInfo)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var data = data
        if (data == null) {
            data = Intent()
        }
        when (requestCode) {
            REQUEST_MRZ -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        data.getSerializable(IntentData.KEY_MRZ_INFO, MRZInfo::class.java)?.let {
                            onEidRead(it)
                        }
                    }
                    else -> {
                        selectManualToggle()
                    }
                }
            }
            REQUEST_NFC -> {
                selectManualToggle()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun validateFields() {
        try {
            mValidator!!.removeRules(docInput!!)
            mValidator!!.removeRules(expiredInput!!)
            mValidator!!.removeRules(dateOfBirthInput!!)
            mValidator!!.put(docInput!!, DocumentNumberRule())
            mValidator!!.put(dateOfBirthInput!!, DateRule())
            mValidator!!.put(expiredInput!!, DateRule())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mValidator!!.validate()
    }

    override fun onValidationSucceeded() {
        val documentNumber = docInput?.text?.toString() ?: ""
        val dateOfBirth = dateOfBirthInput?.text?.toString() ?: ""
        val documentExpiration = expiredInput?.text?.toString() ?: ""

        val mrzInfo = MRZInfo("P",
            "ESP",
            "DUMMY",
            "DUMMY",
            documentNumber,
            "ESP",
            dateOfBirth,
            Gender.MALE,
            documentExpiration,
            "DUMMY"
        )
        onEidRead(mrzInfo)
    }

    override fun onValidationFailed(errors: List<ValidationError>) {
        for (error in errors) {
            val view = error.view
            val message = error.getCollatedErrorMessage(this)

            // Display error messages ;)
            if (view is EditText) {
                view.error = message
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onEidRead(mrzInfo: MRZInfo) {
        val intent = Intent(this, NfcActivity::class.java)
        intent.putExtra(IntentData.KEY_MRZ_INFO, mrzInfo)
        startActivityForResult(intent, REQUEST_NFC)
    }

    fun onMrzRequest() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivityForResult(intent, REQUEST_MRZ)
    }


    companion object {

        private val TAG = SelectionActivity::class.java.simpleName
        private val REQUEST_MRZ = 12
        private val REQUEST_NFC = 11

        private val TAG_SELECTION_FRAGMENT = "TAG_SELECTION_FRAGMENT"
    }

}