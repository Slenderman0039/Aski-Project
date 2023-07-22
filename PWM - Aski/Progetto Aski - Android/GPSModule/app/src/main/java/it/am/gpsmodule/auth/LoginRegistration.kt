package it.am.gpsmodule.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import it.am.gpsmodule.databinding.ActivityLoginRegistrationBinding
import it.am.gpsmodule.databinding.ActivityRegistrationBinding

class LoginRegistration : AppCompatActivity() {
    lateinit var binding: ActivityLoginRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //DISATTIVA LA MODALITA' DARK
        super.onCreate(savedInstanceState)
        binding = ActivityLoginRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registrati.setOnClickListener {
            startActivity(Intent(this, Registration::class.java))
            finish()
        }
        binding.autenticati.setOnClickListener{
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }
}