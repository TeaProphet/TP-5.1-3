package vsu.tp53.onboardapplication.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import vsu.tp53.onboardapplication.databinding.FragmentDiceBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class DiceFragment : Fragment() {

    private lateinit var binding: FragmentDiceBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDiceBinding.inflate(inflater, container, false)
        binding.button.setOnClickListener {
            if (binding.diceNum.text.isNotEmpty() && binding.diceSides.text.isNotEmpty()
                && binding.diceMod.text.isNotEmpty()
            ){
                if (binding.diceResultLabel.visibility == View.INVISIBLE && binding.diceResult.visibility == View.INVISIBLE){
                    binding.diceResult.visibility = View.VISIBLE
                    binding.diceResultLabel.visibility = View.VISIBLE
                }
                binding.errorLabel.visibility = View.INVISIBLE
                val result = (binding.diceNum.text.toString().toInt()..binding.diceNum.text.toString().toInt() *
                        binding.diceSides.text.toString().toInt()).random() +
                        binding.diceMod.text.toString().toInt()
                binding.diceResult.text = result.toString()
            } else {
                binding.errorLabel.visibility = View.VISIBLE
                binding.diceResult.visibility = View.INVISIBLE
                binding.diceResultLabel.visibility = View.INVISIBLE
            }
        }
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}