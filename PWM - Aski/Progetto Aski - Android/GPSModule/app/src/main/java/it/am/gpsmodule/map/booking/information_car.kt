package it.am.gpsmodule.map.booking

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import it.am.gpsmodule.R
import it.am.gpsmodule.databinding.FragmentInformationCarBinding
import it.am.gpsmodule.databinding.FragmentSoneRegistrationBinding
import it.am.gpsmodule.entity.MapCar
import it.am.gpsmodule.map.AskiMap
import it.am.gpsmodule.map.booking.book.BookingActivity
import it.am.gpsmodule.utils.DBMSManager
import it.am.gpsmodule.utils.FormValidator
import it.am.gpsmodule.utils.SQLiteHelper
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.math.absoluteValue

class information_car : Fragment() {

    private var _binding: FragmentInformationCarBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInformationCarBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var car: MapCar = arguments?.getParcelable<MapCar>("car")!!
        binding.brandmodello.text = car.brand + " - " + car.modello
        var id: Int? = arguments?.getInt("img", 0)
        if (id != null) {
            binding.brand.setImageResource(id)
        }
        var prezzoh =
            BookingActivity.calcoloPrezzo2(car.brand!!, car.modello!!, car.anno!!).toString()
        binding.prezzoeuro.text = prezzoh + "€/h"

        binding.prenota.setOnClickListener {
            var prenotazione: Intent = Intent(AskiMap.aski, BookingActivity::class.java)
            prenotazione.putExtra("car", car)
            prenotazione.putExtra("prezzoh", prezzoh)
            startActivity(prenotazione)
        }

    }
}