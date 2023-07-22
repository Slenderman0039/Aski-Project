package it.am.gpsmodule.map.navbar.owncar.addcar


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Resources
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.drawable.InsetDrawable
import android.location.LocationManager.GPS_PROVIDER
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import it.am.gpsmodule.R
import it.am.gpsmodule.databinding.ActivityAddCarBinding
import it.am.gpsmodule.entity.Car
import it.am.gpsmodule.map.navbar.owncar.addcar.brandadapter.BrandAdapter
import it.am.gpsmodule.utils.*


class addCar : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    lateinit var binding: ActivityAddCarBinding
    companion object{
        var inst: addCar? = null
    }
    var car:Car? = Car(null,null,null,null,null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        inst = this

        //BRAND - MODELLO
        binding.step1.setOnClickListener {
            onCreateDialog(R.layout.dialog_owncar_brandmodello,"brandmodello").show()
        }
        //TARGA
        binding.step2.setOnClickListener {
            onCreateDialog(R.layout.dialog_owncar_targa,"targa").show()
        }
        //ANNO IMMATRICOLAZIONE
        binding.step3.setOnClickListener {
            onCreateDialog(R.layout.dialog_owncar_year,"anno").show()
        }
        //CARBURANTE
        binding.step4.setOnClickListener {
            onCreateDialog(R.layout.dialog_owncar_fuel,"carburante").show()

        }

        binding.conferma.setOnClickListener {
            if(car!!.completo()) {
                Log.e(
                    "TAG",
                    LocationGpsManager.getLastKnownLocation(this, this.applicationContext)
                        .toString()
                )

                if (!LocationGpsManager.locationManager!!.isProviderEnabled(GPS_PROVIDER) || LocationGpsManager.currentPosition == null) {
                    val mySnackbar = Snackbar.make(
                        binding.root,
                        "ABILITA IL GPS PER REGISTRARE IL VEICOLO!",
                        Snackbar.LENGTH_SHORT
                    )
                    mySnackbar.show()
                } else {
                    val mySnackbar = Snackbar.make(
                        binding.root,
                        LocationGpsManager.currentPosition.toString(),
                        Snackbar.LENGTH_SHORT
                    )
                    mySnackbar.show()
                    if (ClientNetwork.isOnline(this.applicationContext)) {
                        DBMSManager.queryInsert(
                            "INSERT INTO Veicoli VALUES(${DBMSManager.utente?.id},'${car?.targa?.toUpperCase()}','${car?.modello}','${car?.brand}','${car?.anno}','${car?.carburante}',${
                                LocationGpsManager.getLastKnownLocation(
                                    this,
                                    this.applicationContext
                                )?.latitude()
                            },${
                                LocationGpsManager.getLastKnownLocation(
                                    this,
                                    this.applicationContext
                                )
                                    ?.longitude()
                            },DEFAULT)", { result ->
                                runOnUiThread {
                                    Log.e("TAG", result.toString())
                                }
                            }, {
                                val dialog = onCreateDialogFailedConn()
                                dialog.show()
                                dialog.findViewById<TextView>(R.id.errore).text =
                                    "Errore connessione al server!"
                            })
                        binding.anno.text = "Anno"
                        binding.brand.text = "Brand"
                        binding.modello.text = "Modello"
                        binding.targa.text = "Targa"
                        binding.carburante.text = "Carburante"
                        car?.reset()
                    } else {
                        val dialog = onCreateDialogFailed()
                        dialog.show()
                        dialog.findViewById<TextView>(R.id.errore).text =
                            "Non sei Connesso ad Internet!"
                    }
                }

                }else{
                    val mySnackbar = Snackbar.make(
                        binding.root,
                        "Ci sono alcuni Campi Vuoti!",
                        Snackbar.LENGTH_SHORT
                    )
                    mySnackbar.show()
                }

        }



    }

     var temp_brand: String? = null
    fun onCreateDialog(layout:Int, scelta:String): Dialog {
        return this?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = this.layoutInflater;
            var dialogview = inflater.inflate(layout, null)

            lateinit var targaEditText: TextInputEditText
            lateinit var yearEditText: TextInputEditText
            lateinit var recyclerView: RecyclerView
            lateinit var button: Button
            lateinit var spinner: Spinner

            if(scelta.equals("targa"))
                targaEditText = dialogview.findViewById<TextInputEditText>(R.id.targa)
            else if(scelta.equals("brandmodello")){
                recyclerView = dialogview.findViewById<RecyclerView>(R.id.recyclerview)
                spinner = dialogview.findViewById<Spinner>(R.id.modello)

                var modelli:ArrayList<String> = ArrayList()
                modelli.add("Modello")

                var spinner_adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                    applicationContext,
                    android.R.layout.simple_spinner_dropdown_item,
                    modelli
                )
                spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = spinner_adapter

                spinner.onItemSelectedListener = this
                spinner.isEnabled = false
                recyclerView.apply {

                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = BrandAdapter(createBrandList()) { brand, position ->
                        temp_brand = brand.name
                        car?.brand = brand.name
                        spinner.isEnabled = true
                        var db:SQLiteHelper? = SQLiteHelper.getInstance(context)
                        val dbopen:SQLiteDatabase? = db?.writableDatabase
                        var c:Cursor? = null
                        c = dbopen?.rawQuery("SELECT DISTINCT(Modello) FROM ModelliAuto WHERE Marca = '${brand.name}'",null)
                       modelli.clear()
                            while (c!!.moveToNext()){
                                modelli.add(c.getString(0))
                            }
                         spinner_adapter = ArrayAdapter<String>(
                            applicationContext,
                            android.R.layout.simple_spinner_dropdown_item,
                            modelli
                        )
                        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = spinner_adapter

                    }
                }
            }else if(scelta.equals("anno")){
                yearEditText = dialogview.findViewById<TextInputEditText>(R.id.year)
            }else if(scelta.equals("carburante")){
                button = dialogview.findViewById<Button>(R.id.menu_button)
                button.setOnClickListener {
                    var popup = showMenu(it,R.menu.nav_carburante)
                    popup.setOnMenuItemClickListener {
                        button.text = it.toString()
                        true
                    }
                    popup.show()
                }
            }

            builder.setView(dialogview)
                // Add action buttons
                .setPositiveButton("Conferma",
                    DialogInterface.OnClickListener { dialog, id ->
                        if(scelta.equals("targa")){
                            if(FormValidator.validateFormTarga(targaEditText)){
                                car?.targa = targaEditText.text.toString()
                                binding.targa.text = targaEditText.text
                            }else{
                                val mySnackbar = Snackbar.make(binding.root, "Targa non valida", Snackbar.LENGTH_SHORT)
                                mySnackbar.setAction("Correggi", View.OnClickListener {  onCreateDialog(layout,scelta).show()})
                                mySnackbar.show()
                            }
                        }
                        else if(scelta.equals("brandmodello")){
                            if (temp_brand.isNullOrEmpty()) {
                                dialog.cancel()
                                val mySnackbar = Snackbar.make(binding.root, "Brand non valido", Snackbar.LENGTH_SHORT)
                                mySnackbar.setAction("Correggi", View.OnClickListener {  onCreateDialog(layout,scelta).show()})
                                mySnackbar.show()
                            }else if(spinner.selectedItem.equals("Modello")){
                                dialog.cancel()
                                val mySnackbar = Snackbar.make(binding.root, "Modello non valido", Snackbar.LENGTH_SHORT)
                                mySnackbar.setAction("Correggi", View.OnClickListener {  onCreateDialog(layout,scelta).show()})
                                mySnackbar.show()
                            }else{
                                binding.brand.text = temp_brand
                                binding.modello.text = spinner.selectedItem.toString()
                                car?.modello = spinner.selectedItem.toString()
                            }
                        }else if(scelta.equals("anno")){
                            if(FormValidator.validateFormAnno(yearEditText)){
                                binding.anno.text = yearEditText.text
                                car?.anno = yearEditText.text.toString()
                            }else{
                                dialog.cancel()
                                val mySnackbar = Snackbar.make(binding.root, "Anno immatricolazione non valido", Snackbar.LENGTH_SHORT)
                                mySnackbar.setAction("Correggi", View.OnClickListener {  onCreateDialog(layout,scelta).show()})
                                mySnackbar.show()
                            }
                        }else if(scelta.equals("carburante")){
                            if(button.text.equals("SCEGLI CARBURANTE")){
                                val mySnackbar = Snackbar.make(binding.root, "Carburante non valido", Snackbar.LENGTH_SHORT)
                                mySnackbar.setAction("Correggi", View.OnClickListener {  onCreateDialog(layout,scelta).show()})
                                mySnackbar.show()
                            }else{
                                binding.carburante.text = button.text
                                car?.carburante = button.text.toString()
                            }
                        }
                    })
                .setNegativeButton("Annulla",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })
            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }


    private fun createBrandList(): ArrayList<BrandModel> {
        val array:ArrayList<BrandModel> = ArrayList()
        var db:SQLiteHelper? = SQLiteHelper.getInstance(applicationContext)
        val dbopen:SQLiteDatabase? = db?.writableDatabase
        var c:Cursor? = null
        c = dbopen?.rawQuery("SELECT DISTINCT(Marca) FROM ModelliAuto ORDER BY Marca ASC",null)
        while (c!!.moveToNext()){
            val resources: Resources = this.applicationContext.resources
            val resourceId: Int = resources.getIdentifier(
                "brand_"+c.getString(0).toLowerCase().replace("-","_"), "drawable",
                this.applicationContext.packageName
            )

            array.add(BrandModel(c.getString(0),resourceId))
        }
        return array
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }




    //In the showMenu function from the previous example:
    @SuppressLint("RestrictedApi")
    private fun showMenu(v: View, @MenuRes menuRes: Int): PopupMenu {
        val popup = PopupMenu(addCar.inst!!, v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        if (popup.menu is MenuBuilder) {
            val menuBuilder = popup.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
            for (item in menuBuilder.visibleItems) {
                val iconMarginPx =
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics)
                        .toInt()
                if (item.icon != null) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx,0)
                    } else {
                        item.icon =
                            @SuppressLint("RestrictedApi")
                            object : InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0) {
                                override fun getIntrinsicWidth(): Int {
                                    return intrinsicHeight + iconMarginPx + iconMarginPx
                                }
                            }
                    }
                }
            }
        }
       return popup
    }
    fun onCreateDialogFailed(): Dialog {

        return (this?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_failed_registration, null))
                .setPositiveButton("Chiudi") { dialog, id ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla"))
    }
    fun onCreateDialogFailedConn(): Dialog {
        return (this?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_failed_registration, null))
                .setPositiveButton("Chiudi") { dialog, id ->
                    dialog.cancel()
                    this.finishAffinity()
                    System.exit(0)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla"))
    }
}

