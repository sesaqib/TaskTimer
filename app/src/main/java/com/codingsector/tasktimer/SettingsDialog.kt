package com.codingsector.tasktimer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import kotlinx.android.synthetic.main.settings_dialog.*
import java.util.*

private const val TAG = "SettingsDialog"

const val SETTINGS_FIRST_DAY_OF_WEEK = "FirstDay"
const val SETTINGS_IGNORE_LESS_THAN = "IgnoreLessThan"
const val SETTINGS_DEFAULT_IGNORE_LESS_THAN = 0

private val delta = intArrayOf(
//  0  1  2   3   4   5   6   7   8   9   10  11  12  13   14   15   16   17   18   19   20   21
    0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 120, 180, 240, 300, 360, 420, 480, 540, 600,
//  22   23    24
    900, 1800, 2700
)

class SettingsDialog : AppCompatDialogFragment() {

    private val defaultFirstDayOfWeek = GregorianCalendar(Locale.getDefault()).firstDayOfWeek
    private var firstDay = defaultFirstDayOfWeek
    private var ignoreLessThan = SETTINGS_DEFAULT_IGNORE_LESS_THAN

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: called")
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.SettingsDialogStyle)

        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: called")
        return inflater.inflate(R.layout.settings_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: called")
        super.onViewCreated(view, savedInstanceState)

        dialog?.setTitle(R.string.action_settings)
        okButton.setOnClickListener {
            saveValues()
            dismiss()
        }

        ignoreSeconds.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (progress < 12) {
                    ignoreSecondsTitle.text = getString(
                        R.string.settingsIgnoredSecondsTitle,
                        delta[progress],
                        resources.getQuantityString(
                            R.plurals.settingsLittleUnits,
                            delta[progress]
                        )
                    )
                } else {
                    val minutes = delta[progress] / 60
                    ignoreSecondsTitle.text = getString(
                        R.string.settingsIgnoredSecondsTitle,
                        minutes,
                        resources.getQuantityString(R.plurals.settingsBigUnits, minutes)
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}     // We don't need this

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}      // Nor this
        })

        cancelButton.setOnClickListener { dismiss() }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        Log.d(TAG, "savedInstanceState: called")

        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState == null) {
            readValues()

            firstDaySpinner.setSelection(firstDay - GregorianCalendar.SUNDAY)     // spinner are zero-based

            // convert seconds into an index into the time values array
            val seekBarValue = delta.binarySearch(ignoreLessThan)
            if (seekBarValue < 0) {
                // this shouldn't happen, the programmer's made a mistake
                throw IndexOutOfBoundsException("Values $seekBarValue not found in deltas array")
            }

            ignoreSeconds.max = delta.size - 1
            Log.d(TAG, "onViewStateRestored: setting slider to $seekBarValue")
            ignoreSeconds.progress = seekBarValue

            if (ignoreLessThan < 0) {
                ignoreSecondsTitle.text = getString(
                    R.string.settingsIgnoredSecondsTitle,
                    ignoreLessThan,
                    resources.getQuantityString(R.plurals.settingsLittleUnits, ignoreLessThan)
                )
            } else {
                val minutes = ignoreLessThan / 60
                ignoreSecondsTitle.text = getString(
                    R.string.settingsIgnoredSecondsTitle,
                    minutes,
                    resources.getQuantityString(R.plurals.settingsBigUnits, minutes)
                )
            }
        }
    }

    private fun readValues() {
        with(getDefaultSharedPreferences(context)) {
            firstDay = getInt(SETTINGS_FIRST_DAY_OF_WEEK, defaultFirstDayOfWeek)
            ignoreLessThan = getInt(SETTINGS_IGNORE_LESS_THAN, SETTINGS_DEFAULT_IGNORE_LESS_THAN)
        }
        Log.d(TAG, "Retrieving first day = $firstDay, ignoreLessThan = $ignoreLessThan")
    }

    private fun saveValues() {
        val newFirstDay = firstDaySpinner.selectedItemPosition + GregorianCalendar.SUNDAY
        val newIgnoreLessThan = delta[ignoreSeconds.progress]

        Log.d(TAG, "Saving first day = $newFirstDay, ignore seconds = $newIgnoreLessThan")

        with(getDefaultSharedPreferences(context).edit()) {
            if (newFirstDay != firstDay) {
                putInt(SETTINGS_FIRST_DAY_OF_WEEK, newFirstDay)
            }
            if (newIgnoreLessThan != ignoreLessThan) {
                putInt(SETTINGS_IGNORE_LESS_THAN, newIgnoreLessThan)
            }
            apply()
        }
    }
}