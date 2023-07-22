package it.am.gpsmodule.map.navbar.owncar

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import it.am.gpsmodule.R
import it.am.gpsmodule.databinding.ActivityMapOwnCarListBinding
import it.am.gpsmodule.map.navbar.owncar.addcar.addCar
import it.am.gpsmodule.map.navbar.owncar.listadapter.CarAdapter
import it.am.gpsmodule.map.navbar.owncar.listadapter.CarModel
import it.am.gpsmodule.utils.ClientNetwork
import it.am.gpsmodule.utils.DBMSManager

class MapOwnCarActivityList : AppCompatActivity() {
    lateinit var binding: ActivityMapOwnCarListBinding
     var flag = false
    companion object{
        var inst: MapOwnCarActivityList? = null
        val data = ArrayList<CarModel>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapOwnCarListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        inst = this
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        data.clear()
        if(ClientNetwork.isOnline(this)){
        DBMSManager.querySelect("SELECT * FROM Veicoli WHERE ID =${DBMSManager.utente?.id}",{
                result ->
            runOnUiThread{
                if(result.size()>0){
                    binding.vuoto.visibility = View.INVISIBLE
                    for (i in 0 until result.size()){
                        val obj:JsonObject = result.get(i) as JsonObject
                        val resources: Resources = this.applicationContext.resources
                        Log.e("TAG","brand_"+obj.get("Marca").asString.toLowerCase().replace("-","_"))
                        val resourceId: Int = resources.getIdentifier(
                            "brand_"+obj.get("Marca").asString.toLowerCase().replace("-","_"), "drawable",
                            this.applicationContext.packageName
                        )
                        Log.e("TAG",resourceId.toString())
                        data.add(CarModel(resourceId,obj.get("Marca").asString,obj.get("Modello").asString,obj.get("Carburante").asString,obj.get("Targa").asString,"0",obj.get("Disponibile").asInt))
                        Log.e("TAG", data.get(0).marca)
                    }
                }else{
                    binding.vuoto.visibility = View.VISIBLE
                }
                val adapter = CarAdapter()
                binding.recyclerview.adapter = adapter
                binding.floatingActionButton.setOnClickListener {
                    startActivity(Intent(this,addCar::class.java))
                }
            }
        },{
            val dialog = onCreateDialogFailedConn()
            dialog.show()
            dialog.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
        })}else{
            if(!flag){
                val dialog = onCreateDialogFailed()
                dialog.show()
                dialog.findViewById<TextView>(R.id.errore).text = "Non sei connesso ad Internet!"
                flag = true
            }
        }
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
}