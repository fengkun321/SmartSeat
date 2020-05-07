package com.ai.nuralogix.anura.sample.activities

import ai.nuralogix.anurasdk.utils.AnuLogUtil
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.ai.nuralogix.anura.sample.activities.MeasurementActivity
import com.ai.nuralogix.anura.sample.settings.DfxPipeConfigurationFragment
import com.ai.nuralogix.anura.sample.utils.BundleUtils
import com.smartCarSeatProject.R

class SettingsActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SettingsActivity"

        const val INTENT_ARG_FRAGMENT_NAME = "INTENT_ARG_FRAGMENT_NAME"

        fun createIntent(context: Context, fragmentName: String): Intent {
            val intent = Intent(context, SettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.putExtra(INTENT_ARG_FRAGMENT_NAME, fragmentName)
            return intent
        }

        fun extractSettingFragmentTag(intent: Intent?): String? {
            var fragmentName: String? = null
            if (intent != null) {
                val bundle = intent.extras
                if (bundle != null) {
                    fragmentName = bundle.getString(INTENT_ARG_FRAGMENT_NAME, null)
                }
            }
            return fragmentName
        }

        fun extractSettingBundle(intent: Intent?, bundleKey: String): Bundle? {
            var configBundle: Bundle? = null
            if (intent != null) {
                val bundle = intent.extras
                if (bundle != null) {
                    configBundle = bundle.getBundle(bundleKey)
                    AnuLogUtil.d(TAG, "extract config bundle $configBundle")
                }
            }
            return configBundle
        }

        fun createFragment(fragmentManager: FragmentManager, fragmentTag: String?, configBundle: Bundle?) {
            val fragmentTransaction = fragmentManager.beginTransaction()
            var fragment: Fragment? = fragmentManager.findFragmentByTag(fragmentTag)
            if (fragment == null) {
                when (fragmentTag) {
                    DfxPipeConfigurationFragment::class.java.name -> {
                        fragment = DfxPipeConfigurationFragment()
                        fragment?.arguments = configBundle
                    }
                }
                if (fragment == null) {
                    // nothing to do in this level
                    return
                }
            }

            fragmentTransaction.replace(R.id.fragment_container, fragment, fragmentTag)
            fragmentTransaction.commit()
        }
    }

    var configBundle: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        configBundle = extractSettingBundle(intent, BundleUtils.DFX_BUNDLE_KEY)
        val fragmentTag = extractSettingFragmentTag(intent)

        createFragment(supportFragmentManager, fragmentTag, configBundle)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                AnuLogUtil.d(TAG, "Back to home: $configBundle")
                val intent = Intent(this, MeasurementActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(BundleUtils.DFX_BUNDLE_KEY, configBundle)
                startActivity(intent)
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, MeasurementActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(BundleUtils.DFX_BUNDLE_KEY, configBundle)
        startActivity(intent)
        finish()
        super.onBackPressed()
    }
}