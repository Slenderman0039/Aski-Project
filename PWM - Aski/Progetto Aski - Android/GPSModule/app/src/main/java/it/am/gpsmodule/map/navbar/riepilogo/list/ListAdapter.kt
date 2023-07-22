package it.am.gpsmodule.map.navbar.riepilogo.list

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import it.am.gpsmodule.R
import it.am.gpsmodule.databinding.CardriepilogoBinding
import it.am.gpsmodule.entity.Prenotazione
import it.am.gpsmodule.map.AskiMap
import it.am.gpsmodule.map.navbar.owncar.MapOwnCarActivityList
import it.am.gpsmodule.map.navbar.riepilogo.Riepilogo
import it.am.gpsmodule.map.navbar.riepilogo.fragments.RiepilogoFragment
import it.am.gpsmodule.utils.ClientNetwork
import it.am.gpsmodule.utils.DBMSManager
import it.am.gpsmodule.utils.NotificationReceiver
import it.am.gpsmodule.utils.fcm.FCMNotification
import it.am.gpsmodule.utils.fcm.FCMNotificationData
import it.am.gpsmodule.utils.fcm.FCMService
import okhttp3.ResponseBody
import retrofit2.Call
import java.text.SimpleDateFormat
import java.util.*

class ListAdapter: RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    class ViewHolder(binding: CardriepilogoBinding) : RecyclerView.ViewHolder(binding.root) {
        val auto = binding.auto
        val targa = binding.targa
        val prezzo = binding.prezzo
        val carta = binding.card
        val inizio = binding.inizio
        val fine = binding.fine
        val stato = binding.stato
        val box = binding.boxstato
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ViewHolder {
        val view = CardriepilogoBinding.inflate(
            LayoutInflater.from(parent.
            context), parent, false
        )
        return ListAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return RiepilogoFragment.data.size
    }

    override fun onBindViewHolder(holder: ListAdapter.ViewHolder, position: Int) {
        val ItemsViewModel = RiepilogoFragment.data[position]
        holder.auto.text = ItemsViewModel.brand+" - "+ ItemsViewModel.modello
        holder.targa.text = ItemsViewModel.targa
        holder.prezzo.text = ItemsViewModel.costo.toString()
        holder.carta.text = ItemsViewModel.carta
        holder.inizio.text = ItemsViewModel.data_inizio
        holder.fine.text = ItemsViewModel.data_fine

        val c = Calendar.getInstance()

        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)+1
        val day = c.get(Calendar.DAY_OF_MONTH)

        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        val second = c.get(Calendar.SECOND)
        var format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        var data = format.parse(ItemsViewModel.data_fine)
        var datainizio = format.parse(ItemsViewModel.data_inizio)
        var current = format.parse(day.toString()+"/"+month.toString()+"/"+year.toString() + " " + hour.toString()+":"+minute.toString()+":"+second.toString())
        Log.e("PROVA",current.toString())
        Log.e("PROVA2",data.toString())
        Log.e("PROVA2",current.compareTo(data).toString())
        when(current.compareTo(data)){
            1->{  holder.stato.text = "TERMINATO"; holder.box.setBackgroundColor(Color.parseColor("#0000FF"))}
            0->{ holder.stato.text = "APPENA TERMINATO"; holder.box.setBackgroundColor(Color.parseColor("#FF7F50"))}
        }

        when(current.compareTo(datainizio)){
            -1 -> { holder.box.setOnClickListener {
                var dialog = onCreateDialogFailed(ItemsViewModel)
                dialog.show()
                dialog.findViewById<TextView>(R.id.errore).text = "Vuoi eliminare la tua prenotazione?"

            }}
        }


        Log.e("PROVA",day.toString()+"/"+month.toString()+"/"+year.toString() + " " + hour.toString()+":"+minute.toString()+":"+second.toString())


    }


    fun onCreateDialogFailed(Dati:RiepilogoModel): Dialog {
        return (this?.let {
            val builder = AlertDialog.Builder(RiepilogoFragment.inst!!.context)
            val inflater = RiepilogoFragment.inst!!.layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_failed_registration, null))
                .setPositiveButton("Si") { dialog, id ->
                    if(ClientNetwork.isOnline(RiepilogoFragment.inst!!.requireContext())){
                        val data = Dati.data_inizio!!.split(" ")
                        val data_in = data.get(0).split("/")
                    val data2 = Dati.data_fine!!.split(" ")
                    val data_fi = data2.get(0).split("/")
                        val pren = Prenotazione(DBMSManager.utente!!.id,Dati.targa,data_in[2] + "-" +data_in[1] + "-" + data_in[0] + "T" + data[1],data_fi[2] + "-" +data_fi[1] + "-" + data_fi[0] + "T" + data2[1])
                        Log.e("TAG", "ID: " + DBMSManager.utente!!.id + " Targa: " + Dati.targa + " Data_in: "  + data_in[2] + "-" +data_in[1] + "-" + data_in[0] + "T" + data[1] + " Data_end: " + data_fi[2] + "-" +data_fi[1] + "-" + data_fi[0] + "T" + data2[1])
                        for (i in 0 until AskiMap!!.pren_future.size){
                            Log.e("TAG", "ID: " + AskiMap!!.pren_future[i].id + " Targa: " + AskiMap!!.pren_future[i].targa_veicolo + " Data_in: "  + AskiMap!!.pren_future[i].data_prenotazione_inizio + " Data_end: " + AskiMap!!.pren_future[i].data_prenotazione_fine)
                            if(pren.id!!.equals(AskiMap!!.pren_future[i].id) && pren.targa_veicolo!!.equals(AskiMap!!.pren_future[i].targa_veicolo) && pren.data_prenotazione_fine!!.equals(AskiMap!!.pren_future[i].data_prenotazione_fine) && pren.data_prenotazione_inizio!!.equals(AskiMap!!.pren_future[i].data_prenotazione_inizio)){
                                AskiMap.pren_future.removeAt(i)
                                Log.e("TAG", "RIMOSSO")
                            }
                        }
                    DBMSManager.queryDelete("DELETE FROM Prenotazioni WHERE  ID_Utente = ${DBMSManager.utente!!.id} AND Targa_Veicolo = '${Dati.targa}' AND Data_Prenotazione_Inizio = '${data_in[2] + "-" +data_in[1] + "-" + data_in[0] + " " + data[1]}' AND Data_Prenotazione_Fine = '${data_fi[2] + "-" +data_fi[1] + "-" + data_fi[0] + " " + data2[1]}'" ,{
                        RiepilogoFragment.inst!!.requireActivity().runOnUiThread {
                           val index = RiepilogoFragment.data.indexOf(Dati)
                            remove(index)
                            DBMSManager.querySelect("SELECT n.Token as Token FROM Veicoli v, Notifiche n WHERE v.ID = n.ID AND v.Targa='${Dati.targa}'",{
                                RiepilogoFragment.inst!!.requireActivity().runOnUiThread {
                                    val obj = (it[0] as JsonObject)["Token"].asString
                                    val fcmService = FCMService.create()
                                    val token = obj
                                    val title = "Aski"
                                    val message = "E' stata rimossa la prenotazione programmata per giorno ${Dati.data_inizio} - ${Dati.data_fine}  del tuo veicolo targato ${Dati.targa}."
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
                                    val alarmManager = Riepilogo.inst!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                    val intent = Intent(Riepilogo.inst!!, NotificationReceiver::class.java)
                                    val pendingIntent = PendingIntent.getBroadcast(Riepilogo.inst!!, 0, intent, 0)
                                    alarmManager.cancel(pendingIntent)
                                }
                            },{
                                val dialog2 = onCreateDialogFailedConn()
                                dialog2.show()
                                dialog2.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                            })

                        }
                    },{
                        val dialog2 = onCreateDialogFailedConn()
                        dialog2.show()
                        dialog2.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                    }) }
                    //TODO: QUERY DI CANCELLAZIONE PRENOTAZIONE E INVIO NOTIFICA POPRIETARIO E CANCELLAZIONE NOTIFICA CLENTE
                }.setNegativeButton("No"){ dialog, id ->

                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla"))
    }
    fun onCreateDialogFailedConn(): Dialog {
        return (this?.let {
            val builder = AlertDialog.Builder(RiepilogoFragment.inst!!.context)
            val inflater = RiepilogoFragment.inst!!.layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_failed_registration, null))
                .setPositiveButton("Chiudi") { dialog, id ->
                    dialog.cancel()
                    RiepilogoFragment.inst!!.requireActivity().finishAffinity()
                    System.exit(0)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla"))
    }
    fun  remove(position: Int) {
        RiepilogoFragment.data.removeAt(position)
        notifyItemChanged(position)
        notifyItemRangeRemoved(position, 1)
    }
}