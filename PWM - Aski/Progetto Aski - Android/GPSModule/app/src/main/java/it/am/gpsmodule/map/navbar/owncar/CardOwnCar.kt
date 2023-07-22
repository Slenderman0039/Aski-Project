package it.am.gpsmodule.map.navbar.owncar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import it.am.gpsmodule.databinding.ActivityCardOwnCarBinding
import it.am.gpsmodule.databinding.ActivityLoginBinding

class CardOwnCar : AppCompatActivity() {
    lateinit var binding: ActivityCardOwnCarBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardOwnCarBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}