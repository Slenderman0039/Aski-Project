package it.am.gpsmodule.map.navbar.guida

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import it.am.gpsmodule.databinding.ActivityBookingBinding
import it.am.gpsmodule.databinding.ActivityGuidaBinding
import it.am.gpsmodule.databinding.FragmentInformationCarBinding

class Guida : AppCompatActivity() {
    lateinit var binding: ActivityGuidaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGuidaBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}