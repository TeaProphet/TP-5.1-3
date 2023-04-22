package vsu.tp53.onboardapplication.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        val np = binding.agePicker
        np.maxValue = 120
        np.minValue = 12

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

}