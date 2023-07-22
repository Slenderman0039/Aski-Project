package it.am.gpsmodule.map.navbar.owncar.listadapter

import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData.Item
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import it.am.gpsmodule.R
import it.am.gpsmodule.databinding.ActivityCardOwnCarBinding
import it.am.gpsmodule.map.AskiMap
import it.am.gpsmodule.map.navbar.owncar.MapOwnCarActivityList
import it.am.gpsmodule.utils.ClientNetwork
import it.am.gpsmodule.utils.DBMSManager
import it.am.gpsmodule.utils.fcm.FCMNotification
import it.am.gpsmodule.utils.fcm.FCMNotificationData
import it.am.gpsmodule.utils.fcm.FCMService
import okhttp3.ResponseBody
import retrofit2.Call


class CarAdapter() : RecyclerView.Adapter<CarAdapter.ViewHolder>() {
    class ViewHolder(binding: ActivityCardOwnCarBinding) : RecyclerView.ViewHolder(binding.root) {
        val marcamodello = binding.marcamodello
        val disponibilita = binding.avaible
        val cancella = binding.cancella
        val img = binding.img
        val targa = binding.targa
        val carburante = binding.carburante
        val valutazione = binding.rating
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ActivityCardOwnCarBinding.inflate(
            LayoutInflater.from(parent.
            context), parent, false
        )
        return ViewHolder(view)
    }
    override fun getItemCount(): Int {
        return MapOwnCarActivityList.data.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ItemsViewModel = MapOwnCarActivityList.data[position]


        holder.marcamodello.text = ItemsViewModel.marca + " - " + ItemsViewModel.modello
        val myDrawable: Drawable = MapOwnCarActivityList.inst?.getResources()!!.getDrawable(ItemsViewModel.img)
        holder.img.setImageDrawable(myDrawable)
        holder.carburante.text= "Carburante: "+ItemsViewModel.carburante
        holder.targa.text = "Targa: " + ItemsViewModel.targa
        holder.valutazione.text = "Valutazione: " + ItemsViewModel.rating+"/5"
        if(ItemsViewModel.disponibilita == 1) {
            holder.disponibilita.setBackgroundColor(Color.parseColor("#008000"))
            holder.disponibilita.text = "ONLINE"
        }else{
            holder.disponibilita.setBackgroundColor(Color.parseColor("#007bff"))
            holder.disponibilita.text = "OFFLINE"
        }
        holder.disponibilita.setOnClickListener {
            if (holder.disponibilita.text.toString().equals("ONLINE")) {
                if (ClientNetwork.isOnline(MapOwnCarActivityList.inst!!)) {
                    DBMSManager.queryUpdate(
                        "UPDATE Veicoli SET Disponibile=0 WHERE Targa ='${ItemsViewModel.targa}'",
                        { result ->
                            MapOwnCarActivityList.inst!!.runOnUiThread {
                                val toast = Toast.makeText(
                                    MapOwnCarActivityList.inst!!,
                                    "${ItemsViewModel.targa} è adesso non disponibile!",
                                    Toast.LENGTH_SHORT
                                )
                                toast.show()

                                holder.disponibilita.setBackgroundColor(Color.parseColor("#007bff"))
                                holder.disponibilita.text = "OFFLINE"
                            }
                        },
                        {
                            val dialog = onCreateDialogFailedConn()
                            dialog.show()
                            dialog.findViewById<TextView>(R.id.errore).text =
                                "Errore connessione al server!"
                        })
                } else {
                    var dialog = onCreateDialogFailed()
                    dialog.show()
                    dialog.findViewById<TextView>(R.id.errore).text =
                        "Non sei Connesso ad Internet!"
                }
            } else {
                if (ClientNetwork.isOnline(MapOwnCarActivityList.inst!!)) {
                    DBMSManager.queryUpdate(
                        "UPDATE Veicoli SET Disponibile=1 WHERE Targa ='${ItemsViewModel.targa}'",
                        { result ->
                            MapOwnCarActivityList.inst!!.runOnUiThread {
                                val toast = Toast.makeText(
                                    MapOwnCarActivityList.inst!!,
                                    "${ItemsViewModel.targa} è adesso sulla mappa!",
                                    Toast.LENGTH_SHORT
                                )
                                toast.show()
                                holder.disponibilita.setBackgroundColor(Color.parseColor("#008000"))
                                holder.disponibilita.text = "ONLINE"
                            }
                        },
                        {
                            val dialog = onCreateDialogFailedConn()
                            dialog.show()
                            dialog.findViewById<TextView>(R.id.errore).text =
                                "Errore connessione al server!"
                        })
                } else {
                    var dialog = onCreateDialogFailed()
                    dialog.show()
                    dialog.findViewById<TextView>(R.id.errore).text =
                        "Non sei Connesso ad Internet!"
                }
            }


        }

        holder.cancella.setOnClickListener {
            if(ClientNetwork.isOnline(MapOwnCarActivityList.inst!!)) {
                var dialog = onCreateDialogConfirm(ItemsViewModel)
                dialog.show()
                dialog.findViewById<TextView>(R.id.errore).text = "Sei sicuro di voler rimuovere ${ItemsViewModel.marca + "-" + ItemsViewModel.modello} ?"
            }else{
                var dialog = onCreateDialogFailed()
                dialog.show()
                dialog.findViewById<TextView>(R.id.errore).text = "Non sei Connesso ad Internet!"
            }
        }
    }
    fun onCreateDialogFailed(): Dialog {
        return (this?.let {
            val builder = AlertDialog.Builder(MapOwnCarActivityList.inst!!)
            val inflater = MapOwnCarActivityList.inst!!.layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_failed_registration, null))
                .setPositiveButton("Chiudi") { dialog, id ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla"))
    }
    fun onCreateDialogConfirm(car: CarModel): Dialog {
        return (this?.let {
            val builder = AlertDialog.Builder(MapOwnCarActivityList.inst!!)
            val inflater = MapOwnCarActivityList.inst!!.layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_failed_registration, null))
                .setNegativeButton("Chiudi") { dialog, id ->
                    dialog.cancel()
                }
                .setPositiveButton("Conferma"){
                        dialog,id ->
                    DBMSManager.querySelect("SELECT * FROM Veicoli v, Prenotazioni p WHERE v.Targa = p.Targa_Veicolo AND (NOW() BETWEEN p.Data_Prenotazione_Inizio AND p.Data_Prenotazione_Fine) AND v.Targa = '${car.targa}'", {
                        MapOwnCarActivityList.inst!!.runOnUiThread {
                            if(it.size() <= 0){
                                DBMSManager.querySelect("SELECT n.ID,n.Token as Token \n" +
                                        "FROM Prenotazioni pr, Notifiche n\n" +
                                        "WHERE pr.Targa_Veicolo = '${car.targa}'\n" +
                                        "AND NOW() < Data_Prenotazione_Inizio AND n.ID = pr.ID_Utente",{ result2 ->
                                    for(i in 0 until result2.size()) {
                                        val obj = result2[i] as JsonObject
                                        val fcmService = FCMService.create()
                                        val token = obj["Token"].asString
                                        val title = "Aski"
                                        val message =
                                            "E' stata eliminata la prenotazione per la ${car.marca} - ${car.modello}"
                                        val notification =
                                            FCMNotification(
                                                to = token,
                                                notification = FCMNotificationData(
                                                    title = title,
                                                    body = message
                                                ),
                                                data = emptyMap() // Dati aggiuntivi opzionali
                                            )
                                        val call = fcmService.sendNotification(notification)
                                        call.enqueue(object : retrofit2.Callback<ResponseBody> {
                                            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                                                if (response.isSuccessful) {

                                                } else {
                                                    // Errore nell'invio della notifica
                                                    Log.e("CALL",call.toString()+" "+response.message())
                                                }
                                            }

                                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                                Log.e("ERRORE",call.toString()+ " "+t.toString())
                                                // Gestisci l'errore di invio della notifica
                                            }
                                        })
                                    }




                                    MapOwnCarActivityList.inst!!.runOnUiThread{
                                        DBMSManager.queryDelete("DELETE\n" +
                                                "FROM Prenotazioni \n" +
                                                "WHERE Targa_Veicolo = '${car.targa}'\n" +
                                                "  AND Targa_Veicolo IN (\n" +
                                                "    SELECT pr.Targa_Veicolo\n" +
                                                "    FROM Prenotazioni pr\n" +
                                                "    WHERE pr.Targa_Veicolo = Targa_Veicolo\n" +
                                                "      AND NOW() < Data_Prenotazione_Inizio\n" +
                                                "  );",
                                            { result ->
                                                MapOwnCarActivityList.inst!!.runOnUiThread {
                                                    DBMSManager.queryDelete("DELETE FROM Veicoli WHERE Targa = '${car.targa}'",
                                                        {
                                                            //TODO:Invio Notifca prenotazione cancellata
                                                            MapOwnCarActivityList.inst!!.runOnUiThread {
                                                                val toast = Toast.makeText(
                                                                    MapOwnCarActivityList.inst!!,
                                                                    "${car.targa} cancellata!",
                                                                    Toast.LENGTH_SHORT
                                                                )
                                                                dialog.cancel()
                                                                toast.show()
                                                                val index = MapOwnCarActivityList.data.indexOf(car)
                                                                remove(index)
                                                            }
                                                        },{
                                                            val dialog = onCreateDialogFailedConn()
                                                            dialog.show()
                                                            dialog.findViewById<TextView>(R.id.errore).text =
                                                                "Errore connessione al server!"
                                                        })
                                                }
                                            },
                                            {
                                                val dialog = onCreateDialogFailedConn()
                                                dialog.show()
                                                dialog.findViewById<TextView>(R.id.errore).text =
                                                    "Errore connessione al server!"
                                            })} },{
                                    val dialog = onCreateDialogFailedConn()
                                    dialog.show()
                                    dialog.findViewById<TextView>(R.id.errore).text =
                                        "Errore connessione al server!"
                                })
                            }else{
                                var dialog = onCreateDialogFailed()
                                dialog.show()
                                dialog.findViewById<TextView>(R.id.errore).text =
                                    "Non puoi eliminare l'auto durante la prenotazione di un Utente!"
                            }
                        }
                    },{
                        val dialog = onCreateDialogFailedConn()
                        dialog.show()
                        dialog.findViewById<TextView>(R.id.errore).text =
                            "Errore connessione al server!"
                    })
                }
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla"))
    }
    fun onCreateDialogFailedConn(): Dialog {
        return (this?.let {
            val builder = AlertDialog.Builder(MapOwnCarActivityList.inst!!)
            val inflater = MapOwnCarActivityList.inst!!.layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_failed_registration, null))
                .setPositiveButton("Chiudi") { dialog, id ->
                    dialog.cancel()
                    MapOwnCarActivityList.inst!!.finishAffinity()
                    System.exit(0)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla"))
    }
    fun  remove(position: Int) {
        MapOwnCarActivityList.data.removeAt(position)
        notifyItemChanged(position)
        notifyItemRangeRemoved(position, 1)
    }

}