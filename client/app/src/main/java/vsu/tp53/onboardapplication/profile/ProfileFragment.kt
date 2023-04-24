package vsu.tp53.onboardapplication.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import vsu.tp53.onboardapplication.R
import vsu.tp53.onboardapplication.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.openUserSessions.setOnClickListener {
            it.findNavController().navigate(R.id.userSessionsFragment)
        }

        binding.blockUser.setOnClickListener {
            binding.userBlockedInfo.text = "Учётная запись заблокирована"
        }

        binding.unblockUser.setOnClickListener {
            binding.userBlockedInfo.text = ""
        }

        binding.editProfileButton.setOnClickListener {
            it.findNavController().navigate(R.id.editProfileFragment)
        }
        var isChangedPositive = false
        var isChangedNegative = false
        binding.increaseRep.setOnClickListener {
            var rep: Int = binding.playerReputation.text.toString().toInt()
            if (!isChangedPositive) {
                rep += 1
                isChangedPositive = true
                if (isChangedNegative){
                    isChangedNegative = false
                    rep += 1
                }
            }
            binding.playerReputation.text = rep.toString()
        }

        binding.decreaseRep.setOnClickListener {
            var rep: Int = binding.playerReputation.text.toString().toInt()
            if (!isChangedNegative) {
                rep -= 1
                isChangedNegative = true
                if (isChangedPositive){
                    isChangedPositive = false
                    rep -= 1
                }
            }
            binding.playerReputation.text = rep.toString()
        }

        return binding.root
    }

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageview = binding.profileImage
        imageview.setImageResource(R.drawable.profile_kitten)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}