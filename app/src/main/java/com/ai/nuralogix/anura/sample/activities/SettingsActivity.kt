package ai.nuralogix.anura.sample.activities

import ai.nuralogix.anura.sample.settings.CameraConfigurationFragment
import ai.nuralogix.anura.sample.settings.DfxPipeConfigurationFragment
import ai.nuralogix.anurasdk.utils.AnuLogUtil
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
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

                    CameraConfigurationFragment::class.java.name -> {
                        fragment = CameraConfigurationFragment()
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

    private var configBundle: Bundle? = null
    private var fragmentTag: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fragmentTag = extractSettingFragmentTag(intent)
        when (fragmentTag) {
            DfxPipeConfigurationFragment::class.java.name -> {
                configBundle = extractSettingBundle(intent, BundleUtils.DFX_BUNDLE_KEY)
            }

            CameraConfigurationFragment::class.java.name -> {
                configBundle = extractSettingBundle(intent, BundleUtils.CAMERA_BUNDLE_KEY)
            }
        }

        createFragment(supportFragmentManager, fragmentTag, configBundle)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                AnuLogUtil.d(TAG, "Back to home: $configBundle")
                backToHome()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        backToHome()
        super.onBackPressed()
    }

    private fun backToHome() {
        var intent: Intent
        when (fragmentTag) {
            DfxPipeConfigurationFragment::class.java.name -> {
                intent = Intent(this, MeasurementActivity::class.java)
                intent.putExtra(BundleUtils.DFX_BUNDLE_KEY, configBundle)
            }
            CameraConfigurationFragment::class.java.name -> {
                intent = Intent(this, MainActivity::class.java)
                intent.putExtra(BundleUtils.CAMERA_BUNDLE_KEY, configBundle)
            }
            else -> {
                intent = Intent(this, MeasurementActivity::class.java)
            }
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        startActivity(intent)
        finish()
    }
}