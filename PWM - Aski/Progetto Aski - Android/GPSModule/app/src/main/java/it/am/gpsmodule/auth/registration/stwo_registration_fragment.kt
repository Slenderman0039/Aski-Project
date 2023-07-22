package it.am.gpsmodule.auth.registration

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import it.am.gpsmodule.R
import it.am.gpsmodule.auth.Registration
import it.am.gpsmodule.databinding.FragmentSoneRegistrationBinding
import it.am.gpsmodule.databinding.FragmentStwoRegistrationBinding
import it.am.gpsmodule.utils.FormValidator
import it.am.gpsmodule.utils.OnSwipeTouchListener
class stwo_registration_fragment : Fragment() {
    private var _binding: FragmentStwoRegistrationBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStwoRegistrationBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(Registration.utente?.indirizzo != null){
            binding.addressRegistration.setText(Registration.utente!!.indirizzo)
            binding.capRegistration.setText(Registration.utente!!.cap)
            binding.cityRegistration.setText(Registration.utente!!.citta)
        }

        Registration.root!!.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onSwipeLeft() {
                super.onSwipeLeft()
                if(FormValidator.validateFormAndSpecialChar(binding.addressRegistration) && FormValidator.validateFormCap(binding.capRegistration) && FormValidator.validateFormCity(binding.cityRegistration)){
                    val bundle = Bundle()
                    bundle.putString("address", binding.addressRegistration.text.toString())
                    bundle.putString("cap", binding.capRegistration.text.toString())
                    bundle.putString("city", binding.cityRegistration.text.toString())
                    parentFragmentManager.setFragmentResult("step2",bundle)
                    Registration.utente?.indirizzo = binding.addressRegistration.text.toString()
                    Registration.utente?.cap = binding.capRegistration.text.toString()
                    Registration.utente?.citta = binding.cityRegistration.text.toString()
                    _continue()
                }
            }
            override fun onSwipeRight() {
                super.onSwipeRight()
                _back()
            }
        })



    }
    private fun _back() {

        val supportFragmentManager: FragmentManager = getParentFragmentManager()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.right_to_left,R.anim.right_to_left,R.anim.right_to_left,R.anim.right_to_left)
        transaction.replace(R.id.fragmentContainerView, sone_registration_fragment(),"SONE")
        transaction.commitNow()

        Registration.step1.background = context?.let { ContextCompat.getDrawable(it,R.drawable.rounded_steps) }

    }

    private fun _continue() {
        val supportFragmentManager: FragmentManager = getParentFragmentManager()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.left_to_right,R.anim.left_to_right,R.anim.left_to_right,R.anim.left_to_right)
        transaction.replace(R.id.fragmentContainerView, sthree_registration_fragment(),"STHREE")
        transaction.commitNow()
        Registration.step2.background = context?.let { ContextCompat.getDrawable(it,R.drawable.rounded_steps_confirmed) }
    }
}