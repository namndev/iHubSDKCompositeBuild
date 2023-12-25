package com.ihub.eidscan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ihub.eidscan.fragments.CameraMLKitFragment
import com.ihub.eidsdk.common.IntentData
import com.ihub.eidsdk.facade.CameraOcrCallback
import com.ihub.eidsdk.facade.HubFacade
import org.jmrtd.lds.icao.MRZInfo

class CameraActivity : AppCompatActivity(), CameraOcrCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        HubFacade.registerOcrListener(this)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, CameraMLKitFragment())
            .commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onEidRead(mrzInfo: MRZInfo) {
        val intent = Intent()
        intent.putExtra(IntentData.KEY_MRZ_INFO, mrzInfo)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onError(e: Exception?) {
    }
}