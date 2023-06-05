package vsu.tp53.onboardapplication.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import kotlinx.coroutines.launch
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.R
import vsu.tp53.onboardapplication.auth.service.AuthService
import vsu.tp53.onboardapplication.auth.service.ProfileService
import vsu.tp53.onboardapplication.databinding.FragmentEditProfileBinding
import vsu.tp53.onboardapplication.model.entity.ChangeProfile

/**
 * A simple [Fragment] subclass.
 * Use the [EditProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditProfileFragment : Fragment() {
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var editText: EditText
    private lateinit var _authService: AuthService
    private val authService get() = _authService
    private lateinit var _profileService: ProfileService
    private val profileService get() = _profileService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        if (container != null) {
            _authService = AuthService(RestTemplate(), container.context)
            _profileService = ProfileService(RestTemplate(), container.context)
        }

        lifecycleScope.launch {
            init()
        }

        editText = binding.ageInput
        editText.inputFilterNumberRange(12..100)
        binding.saveChangesButton.setOnClickListener {
            Log.i("EditProfileFrag", "Save changes button is clicked...")
            changeProfile()
            it.findNavController().navigate(R.id.profileFragment)
        }

        binding.quitSystem.setOnClickListener {
            authService.logOut()
            it.findNavController().navigate(R.id.homeFragment)
        }

        binding.chooseAvatarButton.setOnClickListener {
//            val assetManager: AssetManager = requireActivity().assets
//            val fileList: Array<String> = assetManager.list("/avatars") ?: arrayOf()
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                putExtra(
                    DocumentsContract.EXTRA_INITIAL_URI,
                    Uri.parse("content://com.android.externalstorage.documents/tree/assets/avatars")
                )
            }
            startActivityForResult(intent, 1)
        }

        return binding.root
    }

    private suspend fun init() {
        val profileInfo = profileService.getProfileInfo()
        if (profileInfo != null) {
            Log.i("ProfileFragment", "Init profile")
            binding.profileName.text = profileService.getUserNickname()
            Log.i("ProfileFragment", "rep: ${profileInfo.reputation}")
            binding.ageInput.setText(profileInfo.age?.toString())
            binding.editFavGames.setText(profileInfo.games)
            binding.userVk.setText(profileInfo.vk)
            binding.userTg.setText(profileInfo.tg)
        }
    }

    private fun changeProfile() {
        var dialog: AlertDialog
        binding.changePasswordButton.setOnClickListener {
            val taskEditText = EditText(this.context)
            dialog = AlertDialog.Builder(this.context)
                .setTitle("Смена пароля")
                .setView(taskEditText)
                .setPositiveButton("Изменить", null)
                .setNegativeButton("Отменить", null)
                .create()
            dialog.show()
        }
        val changeProfileEntity = ChangeProfile(
            0,
            binding.editFavGames.text.toString(),
            binding.userVk.text.toString(),
            binding.userTg.text.toString()
        )
        if (binding.ageInput.text.toString() != "") {
            changeProfileEntity.age = binding.ageInput.text.toString().toInt()
        }

        Log.i("EditProfFragment", binding.ageInput.text.toString() + " age")

        lifecycleScope.launch {
            profileService.changeProfile(changeProfileEntity)
        }
    }

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageview = binding.profileImage
        imageview.setImageResource(R.drawable.profile_kitten)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            val fileName = uri?.lastPathSegment
            Toast.makeText(requireContext(), "Выбран файл $fileName", Toast.LENGTH_SHORT).show()
        }
    }

    // extension function to filter edit text number range
    private fun EditText.inputFilterNumberRange(range: IntRange) {
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
            } catch (e: NumberFormatException) {
            }
            return ""
        }
    }


    // extension function to filter edit text minimum number
    private fun EditText.filterMin(min: Int) {
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
    private fun EditText.setTextMin(min: Int) {
        try {
            val value = text.toString().toInt()
            setUnderlineColor(Color.GREEN)
            if (value < min) {
                setText("$min")
                setUnderlineColor(Color.RED)
            }
        } catch (e: Exception) {
            setUnderlineColor(Color.RED)
            setText("$min")
        }
    }

    // extension function to hide soft keyboard programmatically
    private fun Context.hideSoftKeyboard(editText: EditText) {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }


    // extension function to set/change edit text underline color
    private fun EditText.setUnderlineColor(color: Int) {
        background.mutate().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                colorFilter = BlendModeColorFilter(color, BlendMode.SRC_IN)
            } else {
                setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        }
    }
}