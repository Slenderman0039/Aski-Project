package it.am.gpsmodule.auth

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import it.am.gpsmodule.databinding.ActivityRegistrationBinding
import it.am.gpsmodule.entity.UtenteRegistrazione


class Registration : AppCompatActivity() {
    lateinit var binding: ActivityRegistrationBinding
    companion object{
        var root:ConstraintLayout? = null
        var utente: UtenteRegistrazione? = UtenteRegistrazione(null,null,null)
    lateinit var step1: LinearLayout
    lateinit var step2: LinearLayout
    lateinit var step3: LinearLayout
    lateinit var rd: UtenteRegistrazione
    var inst: Registration? = null
}
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //DISATTIVA LA MODALITA' DARK
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        inst = this
        root = binding.root
        step1 = binding.step1
        step2 = binding.step2
        step3 = binding.step3
    }
}