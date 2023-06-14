package vsu.tp53.onboardapplication.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import kotlinx.coroutines.launch
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.R
import vsu.tp53.onboardapplication.databinding.FragmentCreateSessionBinding
import vsu.tp53.onboardapplication.model.SessionEntity
import vsu.tp53.onboardapplication.service.AuthService
import vsu.tp53.onboardapplication.service.Errors
import vsu.tp53.onboardapplication.service.ProfileService
import vsu.tp53.onboardapplication.service.SessionService
import vsu.tp53.onboardapplication.util.InputFilterMinMax
import vsu.tp53.onboardapplication.util.Validators
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class CreateSessionFragment : Fragment() {

    private lateinit var _authService: AuthService
    private val authService get() = _authService
    private lateinit var _profileService: ProfileService
    private val profileService get() = _profileService
    private lateinit var _sessionService: SessionService
    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCreateSessionBinding.inflate(inflater, container, false)
        _authService = AuthService(RestTemplate(), requireContext())
        _profileService = ProfileService(RestTemplate(), requireContext())
        _sessionService = SessionService(RestTemplate(), requireContext())

        binding.createSessionButton.setOnClickListener {
            Log.i("CreateSession", "ButtonPressed")
            lifecycleScope.launch {
                Log.i("CreateSession", "Inside scope")
                if (createSession(binding))
                    it.findNavController().navigate(R.id.homeFragment)
            }
        }
        binding.dateInput.apply {
            keyListener = null
            isFocusable = false
            isClickable = true
            setOnClickListener {
                showDateTimePickerDialog(binding.dateInput)
            }
        }
        binding.playersNumberInput.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(2), InputFilterMinMax(0, 16))
        return binding.root
    }

    private suspend fun createSession(binding: FragmentCreateSessionBinding): Boolean {
        Log.i("CreateSession", "Create session method")
        if (binding.nameInput.text.toString() == "" || binding.addressInput.text.toString() == "" ||
                binding.gamesInput.text.toString() == "" || binding.playersNumberInput.text.toString() == "" ||
                binding.dateInput.text.toString() == "") {
            Toast.makeText(context, "Заполните все поля", Toast.LENGTH_LONG).show()
            return false
        }
        if (binding.playersNumberInput.text.toString().toInt() < 2){
            Toast.makeText(context, "Минимальное количество игроков: 2", Toast.LENGTH_LONG).show()
            return false
        }
        if (!Validators.checkDateAndTime(binding.dateInput.text.toString())) {
            Toast.makeText(context, "Неверный формат даты", Toast.LENGTH_LONG).show()
            return false
        }
        val df: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val localDate: LocalDateTime =
            LocalDateTime.parse("${binding.dateInput.text}", df)
        val sessionEntity = SessionEntity(
            "",
            binding.nameInput.text.toString(),
            binding.addressInput.text.toString(),
            binding.gamesInput.text.toString(),
            localDate.toString(),
            arrayOf(profileService.getUserNickname()),
            binding.playersNumberInput.text.toString().toInt()
        )
        val resp = _sessionService.createSession(sessionEntity)
        if (resp != null) {
            if (Errors.getByName(resp.error) != "") {
                Toast.makeText(context, Errors.getByName(resp.error), Toast.LENGTH_LONG)
                    .show()
                return false
            } else {
                Toast.makeText(context, "Произошла ошибка", Toast.LENGTH_LONG)
                    .show()
                return false
            }
        } else {
            return true
        }
    }

    private fun showDatePickerDialog(dateInput: EditText) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
            dateInput.setText(formattedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun showDateTimePickerDialog(dateTimeInput: EditText) {
        val currentDate = calendar.timeInMillis
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
            val selectedDate = selectedCalendar.timeInMillis

            if (selectedDate >= currentDate) {
                val timePickerDialog = TimePickerDialog(requireContext(), { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                    val selectedDateTime = LocalDateTime.of(selectedYear, selectedMonth + 1, selectedDay, selectedHour, selectedMinute)
                    val currentDateTime = LocalDateTime.now()

                    if (selectedDateTime.isAfter(currentDateTime.plusMinutes(1))) {
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                        val formattedDateTime = selectedDateTime.format(formatter)
                        dateTimeInput.setText(formattedDateTime)
                    } else {
                        Toast.makeText(context, "Выберите не прошедшее время", Toast.LENGTH_LONG).show()
                    }
                }, hour, minute, true)

                timePickerDialog.show()
            } else {
                Toast.makeText(context, "Выберите дату, которая еще не прошла", Toast.LENGTH_LONG).show()
            }
        }, year, month, day)

        datePickerDialog.datePicker.minDate = currentDate
        datePickerDialog.show()
    }
}
