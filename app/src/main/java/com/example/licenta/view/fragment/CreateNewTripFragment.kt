package com.example.licenta.view.fragment

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.licenta.R
import com.example.licenta.Utils.EMPTY_FIELD
import com.example.licenta.Utils.convertDateToLong
import com.example.licenta.Utils.convertLongToTime
import com.example.licenta.Utils.toPx
import com.example.licenta.model.Trip
import com.example.licenta.model.TripState
import com.example.licenta.model.User
import com.example.licenta.viewmodel.TripViewModel
import kotlinx.android.synthetic.main.fragment_new_trip.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class CreateNewTripFragment : Fragment() {
    private lateinit var mView: View
    private lateinit var linearLayout: LinearLayout
    private val participantsEditText: ArrayList<EditText> = ArrayList()
    private var date: Long = System.currentTimeMillis()
    private lateinit var tripViewModel: TripViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mView = inflater.inflate(R.layout.fragment_new_trip, container, false)
        linearLayout = mView.findViewById(R.id.participants_linear_layout)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tripViewModel = ViewModelProvider(this).get(
            TripViewModel::class.java
        )

        val dateTextView: TextView = mView.findViewById(R.id.date_text)
        dateTextView.text = convertLongToTime(date)

        date_button.setOnClickListener { makeCalendar(dateTextView) }

        add_participants_button.setOnClickListener { makeNewEditText() }

        create_new_trip_button.setOnClickListener { makeNewTrip() }
    }

    private fun makeCalendar(dateTextView: TextView) {
        val cal: Calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            OnDateSetListener { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val monthAgain = month + 1
                val s = "$year/$monthAgain/$dayOfMonth"
                date = convertDateToLong(s)
                dateTextView.text = s
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
    }

    private fun makeNewEditText() {
        participants_error_message.visibility = View.GONE
        val horizontalLinearLayout = LinearLayout(context)
        horizontalLinearLayout.layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val editText = EditText(context)
        editText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        editText.layoutParams = RelativeLayout.LayoutParams(
            275.toPx(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        editText.addTextChangedListener(object : TextWatcher {
            var ignoreChange = false
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!ignoreChange) {
                    ignoreChange = true
                    tripViewModel.checkIfUserExists(p0.toString())
                    tripViewModel.userLiveData?.observe(viewLifecycleOwner, Observer { t ->
                        if (t) {
                            participantsEditText.add(editText)
                        } else {
                            participantsEditText.remove(editText)
                            editText.error = "User does not exist!"
                        }
                        ignoreChange = false
                    })
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                //NOP
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //NOP
            }
        })
        horizontalLinearLayout.addView(editText)

        val imageButton = ImageButton(context)
        imageButton.layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        imageButton.setBackgroundResource(R.drawable.ic_cancel_grey_24dp)
        imageButton.setOnClickListener { linearLayout.removeView(horizontalLinearLayout) }

        horizontalLinearLayout.addView(imageButton)

        linearLayout.addView(horizontalLinearLayout)
    }

    private fun makeNewTrip() {
        var everyThingNotEmpty = true
        if (textInputEditTextTitle.text.isNullOrEmpty()) {
            textInputEditTextTitle.error = EMPTY_FIELD
            everyThingNotEmpty = false
        }
        if (textInputEditTextAddress.text.isNullOrEmpty()) {
            textInputEditTextAddress.error = EMPTY_FIELD
            everyThingNotEmpty = false
        }
        if (textInputEditTextDescription.text.isNullOrEmpty()) {
            textInputEditTextDescription.error = EMPTY_FIELD
            everyThingNotEmpty = false
        }
        val participantsStringArray = ArrayList<String>()
        if (participantsEditText.isEmpty()) {
            participants_error_message.text =
                getString(R.string.empty_part_list_error_message)
            participants_error_message.visibility = View.VISIBLE
            everyThingNotEmpty = false
        } else {
            participantsEditText.forEach {
                participantsStringArray.add(it.text.toString())
            }
        }

        if (everyThingNotEmpty) {
            val trip = Trip(
                textInputEditTextTitle.text.toString(),
                textInputEditTextAddress.text.toString(),
                date,
                textInputEditTextDescription.text.toString(),
                participantsStringArray
            )

            tripViewModel.addNewTrip(trip, requireContext())
            tripViewModel.newTripLiveData?.observe(
                viewLifecycleOwner,
                Observer {
                    if (it.isNotEmpty()) {
                        if (it.contains(TripState.WRONG_ADDRESS)) textInputEditTextAddress.error =
                            "Address is wrong!"
                        if (it.contains(TripState.PART_HAS_ORGANIZER)) {
                            participants_error_message.text =
                                getString(R.string.organizer_in_participants_error_message)
                            participants_error_message.visibility = View.VISIBLE
                        }
                        if (it.contains(TripState.SUCCESS) && it.size == 1) {
                            Toast.makeText(
                                context,
                                "Trip successfully created",
                                Toast.LENGTH_LONG
                            ).show()
                            Navigation.findNavController(
                                requireActivity(),
                                R.id.my_nav_host_fragment
                            )
                                .navigate(R.id.action_createNewTripFragment_to_futureTripsFragment)
                        }
                    }
                }
            )
        }

    }
}
