package vsu.tp53.onboardapplication.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.springframework.web.client.RestTemplate
import vsu.tp53.onboardapplication.MainActivity
import vsu.tp53.onboardapplication.R
import vsu.tp53.onboardapplication.auth.service.AuthService
import vsu.tp53.onboardapplication.auth.service.Errors
import vsu.tp53.onboardapplication.auth.service.ProfileService
import vsu.tp53.onboardapplication.databinding.FragmentProfileBinding
import vsu.tp53.onboardapplication.model.domain.UserLogInInfo
import vsu.tp53.onboardapplication.model.entity.ChangeReputationEntity
import vsu.tp53.onboardapplication.model.entity.ProfileBanEntity
import vsu.tp53.onboardapplication.model.entity.ProfileInfoEntity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var _authService: AuthService
    private val authService get() = _authService
    private lateinit var _profileService: ProfileService
    private val profileService get() = _profileService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        if (container != null) {
            _authService = AuthService(RestTemplate(), container.context)
            _profileService = ProfileService(RestTemplate(), container.context)
        }

        Log.i("ProfileFragment", "Before check if user is logged or token is expired")
        Log.i("ProfileFragment", authService.checkIfUserLoggedIn().toString() + " log in")
        Log.i("ProfileFragment", authService.checkTokenIsNotExpired().toString() + " token exp")
        if (!authService.checkIfUserLoggedIn() || !authService.checkTokenIsNotExpired()) {
            Log.i("ProfileFragment", "Inside check if user is logged or token is expired")
            Log.i("ProfileFragment", authService.checkIfUserLoggedIn().toString() + " log in")
            Log.i("ProfileFragment", authService.checkTokenIsNotExpired().toString() + " token exp")
            this@ProfileFragment.findNavController().navigate(R.id.pageUnauthorizedFragment)
        } else {
            onStart().apply {
                lifecycleScope.launch {
                    initProfile()
                }
            }
        }

        binding.openUserSessions.setOnClickListener {
            it.findNavController().navigate(R.id.userSessionsFragment)
        }

        binding.blockUser.setOnClickListener {
            lifecycleScope.launch {
                val resp =
                    profileService.banUser(
                        ProfileBanEntity(
                            binding.profileName.text.toString(),
                            ""
                        )
                    )

                if (resp.error != null) {
                    val toast: Toast =
                        Toast.makeText(
                            this@ProfileFragment.context,
                            Errors.getByName(resp.error.toString()),
                            Toast.LENGTH_LONG
                        )
                    toast.show()
                } else {
                    binding.userBlockedInfo.text = "Учётная запись заблокирована"
                }
            }
        }

        binding.unblockUser.setOnClickListener {
            binding.userBlockedInfo.text = ""
            lifecycleScope.launch {
                val resp =
                    profileService.unbanUser(
                        ProfileBanEntity(
                            binding.profileName.text.toString(),
                            ""
                        )
                    )
                if (resp.error != null) {
                    val toast: Toast =
                        Toast.makeText(
                            this@ProfileFragment.context,
                            Errors.getByName(resp.error.toString()),
                            Toast.LENGTH_LONG
                        )
                    toast.show()
                } else {
                    binding.userBlockedInfo.text = "Учётная запись заблокирована"
                }
            }
        }

        binding.editProfileButton.setOnClickListener {
            it.findNavController().navigate(R.id.editProfileFragment)
        }

        binding.increaseRep.setOnClickListener {
            lifecycleScope.launch {
                changeRepPlus()
                initProfile()
            }
        }

        binding.decreaseRep.setOnClickListener {
            lifecycleScope.launch {
                changeRepMinus()
                initProfile()
            }
        }

//        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner,this)
        activity?.onBackPressedDispatcher?.addCallback {
            NavHostFragment.findNavController(this@ProfileFragment).navigateUp()
        }
        return binding.root
    }

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageview = binding.profileImage
        imageview.setImageResource(R.drawable.profile_kitten)
    }

    private suspend fun initProfile() {
        val profileInfo = profileService.getProfileInfo()
        if (profileInfo != null) {
            if (profileInfo.is_banned) {
                this.findNavController().navigate(R.id.fragmentBanned)
                return
            }
            Log.i("ProfileFragment", "Init profile")
            binding.profileName.text = profileService.getUserNickname()
            binding.playerReputation.text = profileInfo.reputation.toString()
            Log.i("ProfileFragment", "rep: ${profileInfo.reputation}")
            binding.userAge.text = profileInfo.age?.toString()
            binding.userGames.text = profileInfo.games
            binding.vkUrl.text = profileInfo.vk
            binding.tgUrl.text = profileInfo.tg

            if (!profileInfo.is_admin) {
                binding.blockUser.isVisible = false
                binding.unblockUser.isVisible = false
            }
        }
    }

    private suspend fun changeRepPlus() {
        val changeRepEntity = ChangeReputationEntity(
            authService.getRowByLogin(profileService.getUserLogin())!!.tokenId,
            binding.profileName.text.toString()
        )
        val rep = profileService.increaseReputation(changeRepEntity)

        if (rep != null) {
            if (rep.error != null) {
                val toast: Toast =
                    Toast.makeText(this.context, Errors.getByName(rep.error!!), Toast.LENGTH_LONG)
                toast.show()
            } else {
                binding.playerReputation.text = rep.new_reputation.toString()
            }
        }
    }

    private suspend fun changeRepMinus() {
        val changeRepEntity = ChangeReputationEntity(
            authService.getRowByLogin(profileService.getUserLogin())!!.tokenId,
            binding.profileName.text.toString()
        )
        val rep = profileService.decreaseReputation(changeRepEntity)

        if (rep != null) {
            if (rep.error != null) {
                val toast: Toast =
                    Toast.makeText(this.context, Errors.getByName(rep.error!!), Toast.LENGTH_LONG)
                toast.show()
            } else {
                binding.playerReputation.text = rep.new_reputation.toString()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun handleOnBackPressed() {
        //Do your job here
        //use next line if you just need navigate up
        NavHostFragment.findNavController(this).navigateUp()
        //Log.e(getClass().getSimpleName(), "handleOnBackPressed");
        return
    }
}