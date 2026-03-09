package com.diyou.games.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.diyou.games.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME    = "diyou_prefs"
        const val KEY_SFX_VOL   = "sfx_volume"
        const val KEY_BGM_VOL   = "bgm_volume"
        const val KEY_SFX_ON    = "sfx_enabled"
        const val KEY_BGM_ON    = "bgm_enabled"
        const val KEY_VIBRATION = "vibration"
    }

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableImmersiveMode()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        loadSettings()
        setupListeners()

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadSettings() {
        binding.seekSfxVolume.progress = (prefs.getFloat(KEY_SFX_VOL, 0.8f) * 100).toInt()
        binding.seekBgmVolume.progress = (prefs.getFloat(KEY_BGM_VOL, 0.6f) * 100).toInt()
        binding.switchSfx.isChecked       = prefs.getBoolean(KEY_SFX_ON, true)
        binding.switchBgm.isChecked       = prefs.getBoolean(KEY_BGM_ON, true)
        binding.switchVibration.isChecked = prefs.getBoolean(KEY_VIBRATION, true)
    }

    private fun setupListeners() {
        binding.seekSfxVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, p: Int, fromUser: Boolean) {
                prefs.edit().putFloat(KEY_SFX_VOL, p / 100f).apply()
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })
        binding.seekBgmVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, p: Int, fromUser: Boolean) {
                prefs.edit().putFloat(KEY_BGM_VOL, p / 100f).apply()
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })
        binding.switchSfx.setOnCheckedChangeListener { _, c ->
            prefs.edit().putBoolean(KEY_SFX_ON, c).apply()
        }
        binding.switchBgm.setOnCheckedChangeListener { _, c ->
            prefs.edit().putBoolean(KEY_BGM_ON, c).apply()
        }
        binding.switchVibration.setOnCheckedChangeListener { _, c ->
            prefs.edit().putBoolean(KEY_VIBRATION, c).apply()
        }
    }

    private fun enableImmersiveMode() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )
    }
}
