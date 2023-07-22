package it.am.gpsmodule.map.navbar.riepilogo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import it.am.gpsmodule.databinding.ActivityLoginBinding
import it.am.gpsmodule.databinding.ActivityRiepilogoBinding
import it.am.gpsmodule.map.navbar.payment.PaymentActivity
import it.am.gpsmodule.map.navbar.riepilogo.fragments.Adapter


class Riepilogo : AppCompatActivity() {

    companion object{
        var inst: Riepilogo? = null
    }
    lateinit var binding: ActivityRiepilogoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRiepilogoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        inst = this
        val sections = arrayOf(
            "RIEPILOGO",
            "GRAFICO"
        )

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        val adapter = Adapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = sections[position]
        }.attach()
    }
}