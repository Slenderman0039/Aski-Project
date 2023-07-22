package it.am.gpsmodule.auth.registration

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import it.am.gpsmodule.R
import it.am.gpsmodule.auth.Registration
import it.am.gpsmodule.databinding.FragmentSoneRegistrationBinding
import it.am.gpsmodule.entity.UtenteRegistrazione
import it.am.gpsmodule.utils.FormValidator
import it.am.gpsmodule.utils.OnSwipeTouchListener


class sone_registration_fragment : Fragment() {

    private var _binding: FragmentSoneRegistrationBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSoneRegistrationBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(Registration.utente!!.nome != null){
            binding.nameRegistration.setText(Registration.utente!!.nome)
            binding.surnameRegistration.setText(Registration.utente!!.cognome)
            binding.dateOfRegistration.setText(Registration.utente!!.data_nascita)
        }


        Registration.root!!.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onSwipeLeft() {
                super.onSwipeLeft()

                if(FormValidator.validateFormNameandSurname(binding.nameRegistration) && FormValidator.validateFormNameandSurname(binding.surnameRegistration) && FormValidator.validateFormDate(binding.dateOfRegistration)){
                    //PASSO I PARAMETRI TRA FRAGMENT TRAMITE BUNDLE
                    val bundle = Bundle()
                    bundle.putString("name", binding.nameRegistration.text.toString())
                    bundle.putString("surname", binding.surnameRegistration.text.toString())
                    bundle.putString("d_b", binding.dateOfRegistration.text.toString())
                    parentFragmentManager.setFragmentResult("step1", bundle)

                    Registration.utente?.nome = binding.nameRegistration.text.toString()
                    Registration.utente?.cognome = binding.surnameRegistration.text.toString()
                    Registration.utente?.data_nascita = binding.dateOfRegistration.text.toString()

                    //ESEGUO LO SWITCH DI FRAGMENT CON ANIMAZIONE
                    _continue()
                }

            }
        })
    }
    private fun _continue() {
        val supportFragmentManager: FragmentManager = getParentFragmentManager()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.left_to_right,R.anim.left_to_right,R.anim.left_to_right,R.anim.left_to_right)

        transaction.replace(R.id.fragmentContainerView, stwo_registration_fragment(),"STWO")
        transaction.commitNow()

        Registration.step1.background = context?.let { ContextCompat.getDrawable(it,R.drawable.rounded_steps_confirmed) }
    }

}