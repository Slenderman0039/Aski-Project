package it.am.gpsmodule.map.filtri

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Resources
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import it.am.gpsmodule.R
import it.am.gpsmodule.databinding.FragmentAddCardBinding
import it.am.gpsmodule.databinding.FragmentFilterBinding
import it.am.gpsmodule.map.AskiMap
import it.am.gpsmodule.map.navbar.owncar.addcar.BrandModel
import it.am.gpsmodule.map.navbar.owncar.addcar.brandadapter.BrandAdapter
import it.am.gpsmodule.utils.FormValidator
import it.am.gpsmodule.utils.SQLiteHelper


class filter : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!
    var dialog:Dialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.kmslider.setValues(AskiMap.kmFilter.toFloat())
        binding.filterBrand.setText(AskiMap.brandFilter)
        binding.title.text = "FILTRO ${AskiMap.kmFilter.toFloat().toString()} KM"

        binding.kmslider.addOnChangeListener { slider, value, fromUser ->
            binding.title.setText("FILTRO ${value.toString()} KM")
            AskiMap.kmFilter = value.toInt()
        }

        binding.filterBrand.setOnClickListener {
           //IMPLEMENTARE DIALOG
            dialog = onCreateDialog(R.layout.dialog_brand_filter)
            dialog!!.show()
        }

        binding.removeFilters.setOnClickListener {
            AskiMap.kmFilter = 1
            AskiMap.brandFilter = "SELEZIONA"
            binding.filterBrand.setText(AskiMap.brandFilter)
            binding.kmslider.setValues(AskiMap.kmFilter.toFloat())
            binding.title.text = "FILTRO ${AskiMap.kmFilter.toFloat().toString()} KM"
            AskiMap.instances()!!.deleteAnnotations()
        }

    }


    private fun createBrandList(): ArrayList<BrandModel> {
        val array:ArrayList<BrandModel> = ArrayList()
        var db: SQLiteHelper? = SQLiteHelper.getInstance(AskiMap.instances()!!)
        val dbopen: SQLiteDatabase? = db?.writableDatabase
        var c: Cursor? = null
        c = dbopen?.rawQuery("SELECT DISTINCT(Marca) FROM ModelliAuto ORDER BY Marca ASC",null)
        while (c!!.moveToNext()){
            val resources: Resources = AskiMap.instances()!!.resources
            val resourceId: Int = resources.getIdentifier(
                "brand_"+c.getString(0).toLowerCase().replace("-","_"), "drawable",
                AskiMap.instances()!!.packageName
            )

            array.add(BrandModel(c.getString(0),resourceId))
        }
        return array
    }

    fun onCreateDialog(layout:Int): Dialog {
        return this?.let {
            val builder = AlertDialog.Builder(AskiMap.aski)
            // Get the layout inflater
            val inflater = this.layoutInflater;
            var dialogview = inflater.inflate(layout, null)


            lateinit var recyclerView: RecyclerView

                recyclerView = dialogview.findViewById<RecyclerView>(R.id.recyclerview)
                recyclerView.apply {

                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = BrandAdapter(createBrandList()) { brand, position ->
                        binding.filterBrand.setText(brand.name)
                        AskiMap.brandFilter = brand.name
                        Toast.makeText(AskiMap.aski,"Hai impostato il brand: ${brand.name}",Toast.LENGTH_SHORT).show()
                        dialog!!.cancel()
                        AskiMap.instances()?.deleteAnnotations()
                    }
                }


            builder.setView(dialogview)
            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }



}