package vsu.tp53.onboardapplication.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import vsu.tp53.onboardapplication.R
import vsu.tp53.onboardapplication.databinding.FragmentEditProfileBinding
import vsu.tp53.onboardapplication.databinding.FragmentUserSessionsBinding

/**
 * A simple [Fragment] subclass.
 * Use the [EditProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditProfileFragment : Fragment() {
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var editText: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        editText = binding.ageInput
        editText.inputFilterNumberRange(12..100)
        binding.saveChangesButton.setOnClickListener {
            it.findNavController().navigate(R.id.profileFragment)
        }
        
        return binding.root
    }

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageview = binding.profileImage
        imageview.setImageResource(R.drawable.profile_kitten)
    }

    // extension function to filter edit text number range
    fun EditText.inputFilterNumberRange(range: IntRange){
        filterMin(range.first)
        filters = arrayOf<InputFilter>(InputFilterMax(range.last))
    }


    // class to input filter maximum number
    class InputFilterMax(private var max: Int) : InputFilter {
        override fun filter(
            p0: CharSequence, p1: Int, p2: Int, p3: Spanned?, p4: Int, p5: Int
        ): CharSequence? {
            try {
                val replacement = p0.subSequence(p1, p2).toString()
                val newVal = p3.toString().substring(0, p4) +
                        replacement + p3.toString()
                    .substring(p5, p3.toString().length)
                val input = newVal.toInt()
                if (input <= max) return null
            } catch (e: NumberFormatException) { }
            return ""
        }
    }


    // extension function to filter edit text minimum number
    fun EditText.filterMin(min: Int){
        onFocusChangeListener = View.OnFocusChangeListener { view, b ->
            if (!b) {
                // set minimum number if inputted number less than minimum
                setTextMin(min)
                // hide soft keyboard on edit text lost focus
                context.hideSoftKeyboard(this)
            }
        }

        setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // set minimum number if inputted number less than minimum
                setTextMin(min)

                // hide soft keyboard on press action done
                context.hideSoftKeyboard(this)
            }
            false
        }
    }


    // extension function to set edit text minimum number
    fun EditText.setTextMin(min: Int){
        try {
            val value = text.toString().toInt()
            setUnderlineColor(Color.GREEN)
            if (value < min){
                setText("$min")
                setUnderlineColor(Color.RED)
            }
        }catch (e: Exception){
            setUnderlineColor(Color.RED)
            setText("$min")
        }
    }
    // extension function to hide soft keyboard programmatically
    fun Context.hideSoftKeyboard(editText: EditText){
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }


    // extension function to set/change edit text underline color
    fun EditText.setUnderlineColor(color: Int){
        background.mutate().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                colorFilter = BlendModeColorFilter(color, BlendMode.SRC_IN)
            }else{
                setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        }
    }
}