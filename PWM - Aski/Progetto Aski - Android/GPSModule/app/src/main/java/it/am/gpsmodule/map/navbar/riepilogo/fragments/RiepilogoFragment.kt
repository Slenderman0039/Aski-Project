package it.am.gpsmodule.map.navbar.riepilogo.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.JsonObject
import it.am.gpsmodule.R
import it.am.gpsmodule.databinding.FragmentRiepilogoBinding
import it.am.gpsmodule.entity.Prenotazione
import it.am.gpsmodule.entity.RiepilogoDati
import it.am.gpsmodule.map.booking.book.BookingActivity
import it.am.gpsmodule.map.navbar.owncar.addcar.brandadapter.BrandAdapter
import it.am.gpsmodule.map.navbar.riepilogo.list.ListAdapter as ListAdapter
import it.am.gpsmodule.map.navbar.riepilogo.Riepilogo
import it.am.gpsmodule.map.navbar.riepilogo.list.RiepilogoModel
import it.am.gpsmodule.utils.ClientNetwork
import it.am.gpsmodule.utils.DBMSManager
import it.am.gpsmodule.utils.FormValidator
import it.am.gpsmodule.utils.SQLiteHelper


class RiepilogoFragment : Fragment() {

    companion object {
        var inst: RiepilogoFragment? = null
        val data = ArrayList<RiepilogoModel>()
    }

    private var _binding: FragmentRiepilogoBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inst = this
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRiepilogoBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedpreferences: SharedPreferences = Riepilogo.inst!!.getSharedPreferences("AskiSharedPreferences", Context.MODE_PRIVATE)

        binding.floatingActionButton2.setOnClickListener {
            var dialog = onCreateDialog(sharedpreferences)
            dialog.show()
        }

        binding.recyclerview.layoutManager = LinearLayoutManager(Riepilogo.inst)
        data.clear()
        if (ClientNetwork.isOnline(this.requireContext())) {
            DBMSManager.querySelect("SELECT\n" +
                    "    v.Targa as Targa,\n" +
                    "    v.Modello as Modello,\n" +
                    "    v.Marca as Marca,\n" +
                    "    p.Metodo_Pagamento as Metodo,\n" +
                    "    p.Importo as Importo,\n" +
                    "    p.Data_Pagamento,\n" +
                    "    pr.Targa_Veicolo as Targa,\n" +
                    "    pr.Data_Prenotazione_Inizio as Data_inizio,\n" +
                    "    pr.Data_Prenotazione_Fine as Data_fine,\n" +
                    "    ROUND(TIME_TO_SEC(timediff(p.Data_Pagamento, pr.Data_Prenotazione_Fine))/60)\n" +
                    "FROM\n" +
                    "    Veicoli v,\n" +
                    "    Prenotazioni pr,\n" +
                    "    Pagamenti p\n" +
                    "WHERE\n" +
                    "  v.Targa = pr.Targa_Veicolo\n" +
                    "    AND p.ID_Utente = pr.ID_Utente\n" +
                    "  AND ROUND(TIME_TO_SEC(timediff(p.Data_Pagamento, pr.Data_Prenotazione_Fine))/60) <= 2 AND (TIME_TO_SEC(timediff(p.Data_Pagamento, pr.Data_Prenotazione_Fine))/60) > 0 AND pr.ID_Utente = ${DBMSManager.utente!!.id}",
                {requireActivity().runOnUiThread {
                    for(i in 0 until it.size()){
                       val obj = it.get(i) as JsonObject
                        var data1 = obj["Data_inizio"].asString.replace("T", " ")
                            .split(" ")
                        var data2 = obj["Data_fine"].asString.replace("T", " ")
                            .split(" ")
                        var data_in =data1.get(0).split("-")
                        var data_fin = data2.get(0).split("-")
                        data.add(
                            RiepilogoModel(
                                obj["Marca"].asString,
                                obj["Modello"].asString,
                                obj["Targa"].asString,
                                if (obj["Importo"].isJsonNull) BookingActivity.calcoloprezzo_tot(BookingActivity.calcoloPrezzo2(obj["Marca"].asString,obj["Modello"].asString,obj["Anno"].asString),data_in[2] + "/" + data_in[1] + "/" + data_in[0]+ " "+data1[1],data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1]) else obj["Importo"].asFloat,
                                if (obj["Metodo"].isJsonNull) sharedpreferences.getString("pref_card","default").toString() else obj["Metodo"].asString,
                                (data_in[2] + "/" + data_in[1] + "/" + data_in[0] + " " + data1[1]),
                                (data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1])
                            )
                        )
                    }
                    DBMSManager.querySelect("SELECT v.Modello as Modello,v.Marca as Marca,v.Anno_Produzione as Anno, v.Targa as Targa, pr.Data_Prenotazione_Inizio as Data_inizio, pr.Data_Prenotazione_Fine as Data_fine\n" +
                            "    FROM Prenotazioni pr, Veicoli v\n" +
                            "    WHERE v.Targa = pr.Targa_Veicolo AND pr.ID_Utente = ${DBMSManager.utente!!.id}",{result ->
                        requireActivity().runOnUiThread {
                            for (i in 0 until result.size()) {
                                val obj = result.get(i) as JsonObject
                                var data1 = obj["Data_inizio"].asString.replace("T", " ")
                                    .split(" ")
                                var data2 = obj["Data_fine"].asString.replace("T", " ")
                                    .split(" ")
                                var data_in = data1.get(0).split("-")
                                var data_fin = data2.get(0).split("-")
                                var flag = true

                                for (j in 0 until data.size){
                                    Log.e("TAG", obj["Marca"].asString + " " + obj["Modello"].asString + " " + obj["Targa"].asString)
                                    if(obj["Marca"].asString.equals(data[j].brand) && obj["Modello"].asString.equals(data[j].modello) && obj["Targa"].asString.equals(data[j].targa) &&  (data_in[2] + "/" + data_in[1] + "/" + data_in[0] + " " + data1[1]).equals(data[j].data_inizio) && (data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1]).equals(data[j].data_fine)){
                                        flag  = false
                                        break
                                    }
                                }
                                if(flag) {
                                    data.add(
                                        RiepilogoModel(
                                            obj["Marca"].asString,
                                            obj["Modello"].asString,
                                            obj["Targa"].asString,
                                            if (obj["Importo"] == null) BookingActivity.calcoloprezzo_tot(
                                                BookingActivity.calcoloPrezzo2(
                                                    obj["Marca"].asString,
                                                    obj["Modello"].asString,
                                                    obj["Anno"].asString
                                                ),
                                                data_in[2] + "/" + data_in[1] + "/" + data_in[0] + " " + data1[1],
                                                data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1]
                                            ) else obj["Importo"].asFloat,
                                            if (obj["Metodo"] == null) sharedpreferences.getString(
                                                "pref_card",
                                                "default"
                                            ).toString() else obj["Metodo"].asString,
                                            (data_in[2] + "/" + data_in[1] + "/" + data_in[0] + " " + data1[1]),
                                            (data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1])
                                        )
                                    )
                                }
                            }
                            val adapter = ListAdapter()
                            binding.recyclerview.adapter = adapter
                        }},{
                        val dialog2 = onCreateDialogFailedConn()
                        dialog2.show()
                        dialog2.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                })

                }},
                {
                    val dialog2 = onCreateDialogFailedConn()
                    dialog2.show()
                    dialog2.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                })
                } else {
            val dialog2 = onCreateDialogFailed()
            dialog2.show()
            dialog2.findViewById<TextView>(R.id.errore).text = "Non sei connesso ad Internet!"
        }


    }
    fun onCreateDialogFailed(): Dialog {
        return (this?.let {
            val builder = AlertDialog.Builder(this.requireContext())
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
            val builder = AlertDialog.Builder(this.requireContext())
            val inflater = layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_failed_registration, null))
                .setPositiveButton("Chiudi") { dialog, id ->
                    dialog.cancel()
                    this.requireActivity().finishAffinity()
                    System.exit(0)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla"))
    }

    fun onCreateDialog(sharedpreferences: SharedPreferences): Dialog {
        return this?.let {
            val builder = AlertDialog.Builder(Riepilogo.inst)
            // Get the layout inflater
            val inflater = this.layoutInflater;
            var dialogview = inflater.inflate(R.layout.dialog_period_filter, null)


            builder.setView(dialogview)
                // Add action buttons
                .setPositiveButton("Conferma",
                    DialogInterface.OnClickListener { dialog, id ->
                       var radio=dialogview.findViewById<RadioGroup>(R.id.radioGroup)
                        var view:View = radio.findViewById<RadioGroup>(radio.checkedRadioButtonId)
                        when(radio.indexOfChild(view)){
                            0->{val size = data.size
                                data.clear()
                                binding.recyclerview.adapter!!.notifyItemRangeRemoved(0, size);/*Todo: QUERY ANNUALE*/
                                if (ClientNetwork.isOnline(this.requireContext())) {
                                    DBMSManager.querySelect("SELECT\n" +
                                            "    v.Targa as Targa,\n" +
                                            "    v.Modello as Modello,\n" +
                                            "    v.Marca as Marca,\n" +
                                            "    p.Metodo_Pagamento as Metodo,\n" +
                                            "    p.Importo as Importo,\n" +
                                            "    p.Data_Pagamento,\n" +
                                            "    pr.Targa_Veicolo as Targa,\n" +
                                            "    pr.Data_Prenotazione_Inizio as Data_inizio,\n" +
                                            "    pr.Data_Prenotazione_Fine as Data_fine,\n" +
                                            "    ROUND(TIME_TO_SEC(timediff(p.Data_Pagamento, pr.Data_Prenotazione_Fine))/60)\n" +
                                            "FROM\n" +
                                            "    Veicoli v,\n" +
                                            "    Prenotazioni pr,\n" +
                                            "    Pagamenti p\n" +
                                            "WHERE\n" +
                                            "  v.Targa = pr.Targa_Veicolo\n" +
                                            "    AND p.ID_Utente = pr.ID_Utente\n" +
                                            "  AND ROUND(TIME_TO_SEC(timediff(p.Data_Pagamento, pr.Data_Prenotazione_Fine))/60) <= 2 AND (TIME_TO_SEC(timediff(p.Data_Pagamento, pr.Data_Prenotazione_Fine))/60) > 0 AND pr.ID_Utente = ${DBMSManager.utente!!.id}",
                                        {requireActivity().runOnUiThread {
                                            for(i in 0 until it.size()){
                                                val obj = it.get(i) as JsonObject
                                                var data1 = obj["Data_inizio"].asString.replace("T", " ")
                                                    .split(" ")
                                                var data2 = obj["Data_fine"].asString.replace("T", " ")
                                                    .split(" ")
                                                var data_in =data1.get(0).split("-")
                                                var data_fin = data2.get(0).split("-")
                                                data.add(
                                                    RiepilogoModel(
                                                        obj["Marca"].asString,
                                                        obj["Modello"].asString,
                                                        obj["Targa"].asString,
                                                        if (obj["Importo"].isJsonNull) BookingActivity.calcoloprezzo_tot(BookingActivity.calcoloPrezzo2(obj["Marca"].asString,obj["Modello"].asString,obj["Anno"].asString),data_in[2] + "/" + data_in[1] + "/" + data_in[0]+ " "+data1[1],data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1]) else obj["Importo"].asFloat,
                                                        if (obj["Metodo"].isJsonNull) sharedpreferences.getString("pref_card","default").toString() else obj["Metodo"].asString,
                                                        (data_in[2] + "/" + data_in[1] + "/" + data_in[0] + " " + data1[1]),
                                                        (data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1])
                                                    )
                                                )
                                            }
                                            DBMSManager.querySelect("SELECT v.Modello as Modello,v.Marca as Marca,v.Anno_Produzione as Anno, v.Targa as Targa, pr.Data_Prenotazione_Inizio as Data_inizio, pr.Data_Prenotazione_Fine as Data_fine\n" +
                                                    "    FROM Prenotazioni pr, Veicoli v\n" +
                                                    "    WHERE v.Targa = pr.Targa_Veicolo AND YEAR(pr.Data_Prenotazione_Fine) =YEAR(NOW()) AND YEAR(pr.Data_Prenotazione_Inizio) = YEAR(NOW()) AND pr.ID_Utente = ${DBMSManager.utente!!.id}",{result ->
                                                requireActivity().runOnUiThread {
                                                    for (i in 0 until result.size()) {
                                                        val obj = result.get(i) as JsonObject
                                                        var data1 = obj["Data_inizio"].asString.replace("T", " ")
                                                            .split(" ")
                                                        var data2 = obj["Data_fine"].asString.replace("T", " ")
                                                            .split(" ")
                                                        var data_in = data1.get(0).split("-")
                                                        var data_fin = data2.get(0).split("-")
                                                        var flag = true

                                                        for (j in 0 until data.size){
                                                            Log.e("TAG", obj["Marca"].asString + " " + obj["Modello"].asString + " " + obj["Targa"].asString)
                                                            if(obj["Marca"].asString.equals(data[j].brand) && obj["Modello"].asString.equals(data[j].modello) && obj["Targa"].asString.equals(data[j].targa) &&  (data_in[2] + "/" + data_in[1] + "/" + data_in[0] + " " + data1[1]).equals(data[j].data_inizio) && (data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1]).equals(data[j].data_fine)){
                                                                flag  = false
                                                                break
                                                            }
                                                        }
                                                        if(flag) {
                                                            data.add(
                                                                RiepilogoModel(
                                                                    obj["Marca"].asString,
                                                                    obj["Modello"].asString,
                                                                    obj["Targa"].asString,
                                                                    if (obj["Importo"] == null) BookingActivity.calcoloprezzo_tot(
                                                                        BookingActivity.calcoloPrezzo2(
                                                                            obj["Marca"].asString,
                                                                            obj["Modello"].asString,
                                                                            obj["Anno"].asString
                                                                        ),
                                                                        data_in[2] + "/" + data_in[1] + "/" + data_in[0] + " " + data1[1],
                                                                        data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1]
                                                                    ) else obj["Importo"].asFloat,
                                                                    if (obj["Metodo"] == null) sharedpreferences.getString(
                                                                        "pref_card",
                                                                        "default"
                                                                    ).toString() else obj["Metodo"].asString,
                                                                    (data_in[2] + "/" + data_in[1] + "/" + data_in[0] + " " + data1[1]),
                                                                    (data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1])
                                                                )
                                                            )
                                                        }
                                                    }
                                                    binding.recyclerview.adapter = ListAdapter()
                                                }},{
                                                val dialog2 = onCreateDialogFailedConn()
                                                dialog2.show()
                                                dialog2.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                                            })

                                        }},
                                        {
                                            val dialog2 = onCreateDialogFailedConn()
                                            dialog2.show()
                                            dialog2.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                                        })
                                } else {
                                    val dialog2 = onCreateDialogFailed()
                                    dialog2.show()
                                    dialog2.findViewById<TextView>(R.id.errore).text = "Non sei connesso ad Internet!"
                                }}
                            1->{val size = data.size
                                data.clear();
                                binding.recyclerview.adapter!!.notifyItemRangeRemoved(0, size);
                                binding.recyclerview.adapter!!  /*Todo: QUERY MENSILE*/
                                if (ClientNetwork.isOnline(this.requireContext())) {
                                    DBMSManager.querySelect("SELECT\n" +
                                            "    v.Targa as Targa,\n" +
                                            "    v.Modello as Modello,\n" +
                                            "    v.Marca as Marca,\n" +
                                            "    p.Metodo_Pagamento as Metodo,\n" +
                                            "    p.Importo as Importo,\n" +
                                            "    p.Data_Pagamento,\n" +
                                            "    pr.Targa_Veicolo as Targa,\n" +
                                            "    pr.Data_Prenotazione_Inizio as Data_inizio,\n" +
                                            "    pr.Data_Prenotazione_Fine as Data_fine,\n" +
                                            "    ROUND(TIME_TO_SEC(timediff(p.Data_Pagamento, pr.Data_Prenotazione_Fine))/60)\n" +
                                            "FROM\n" +
                                            "    Veicoli v,\n" +
                                            "    Prenotazioni pr,\n" +
                                            "    Pagamenti p\n" +
                                            "WHERE\n" +
                                            "  v.Targa = pr.Targa_Veicolo\n" +
                                            "    AND p.ID_Utente = pr.ID_Utente\n" +
                                            "  AND ROUND(TIME_TO_SEC(timediff(p.Data_Pagamento, pr.Data_Prenotazione_Fine))/60) <= 2 AND (TIME_TO_SEC(timediff(p.Data_Pagamento, pr.Data_Prenotazione_Fine))/60) > 0 AND pr.ID_Utente = ${DBMSManager.utente!!.id}",
                                        {requireActivity().runOnUiThread {
                                            for(i in 0 until it.size()){
                                                val obj = it.get(i) as JsonObject
                                                var data1 = obj["Data_inizio"].asString.replace("T", " ")
                                                    .split(" ")
                                                var data2 = obj["Data_fine"].asString.replace("T", " ")
                                                    .split(" ")
                                                var data_in =data1.get(0).split("-")
                                                var data_fin = data2.get(0).split("-")
                                                data.add(
                                                    RiepilogoModel(
                                                        obj["Marca"].asString,
                                                        obj["Modello"].asString,
                                                        obj["Targa"].asString,
                                                        if (obj["Importo"].isJsonNull) BookingActivity.calcoloprezzo_tot(BookingActivity.calcoloPrezzo2(obj["Marca"].asString,obj["Modello"].asString,obj["Anno"].asString),data_in[2] + "/" + data_in[1] + "/" + data_in[0]+ " "+data1[1],data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1]) else obj["Importo"].asFloat,
                                                        if (obj["Metodo"].isJsonNull) sharedpreferences.getString("pref_card","default").toString() else obj["Metodo"].asString,
                                                        (data_in[2] + "/" + data_in[1] + "/" + data_in[0] + " " + data1[1]),
                                                        (data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1])
                                                    )
                                                )
                                            }
                                            DBMSManager.querySelect("SELECT v.Modello as Modello,v.Marca as Marca,v.Anno_Produzione as Anno, v.Targa as Targa, pr.Data_Prenotazione_Inizio as Data_inizio, pr.Data_Prenotazione_Fine as Data_fine\n" +
                                                    "    FROM Prenotazioni pr, Veicoli v\n" +
                                                    "    WHERE v.Targa = pr.Targa_Veicolo AND YEAR(pr.Data_Prenotazione_Fine) =YEAR(NOW()) AND YEAR(pr.Data_Prenotazione_Inizio) = YEAR(NOW()) AND MONTH(pr.Data_Prenotazione_Fine) = MONTH(NOW()) AND MONTH(pr.Data_Prenotazione_Inizio) = MONTH(NOW()) AND pr.ID_Utente = ${DBMSManager.utente!!.id}",{result ->
                                                requireActivity().runOnUiThread {
                                                    for (i in 0 until result.size()) {
                                                        val obj = result.get(i) as JsonObject
                                                        var data1 = obj["Data_inizio"].asString.replace("T", " ")
                                                            .split(" ")
                                                        var data2 = obj["Data_fine"].asString.replace("T", " ")
                                                            .split(" ")
                                                        var data_in = data1.get(0).split("-")
                                                        var data_fin = data2.get(0).split("-")
                                                        var flag = true

                                                        for (j in 0 until data.size){
                                                            Log.e("TAG", obj["Marca"].asString + " " + obj["Modello"].asString + " " + obj["Targa"].asString)
                                                            if(obj["Marca"].asString.equals(data[j].brand) && obj["Modello"].asString.equals(data[j].modello) && obj["Targa"].asString.equals(data[j].targa) &&  (data_in[2] + "/" + data_in[1] + "/" + data_in[0] + " " + data1[1]).equals(data[j].data_inizio) && (data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1]).equals(data[j].data_fine)){
                                                                flag  = false
                                                                break
                                                            }
                                                        }
                                                        if(flag) {
                                                            data.add(
                                                                RiepilogoModel(
                                                                    obj["Marca"].asString,
                                                                    obj["Modello"].asString,
                                                                    obj["Targa"].asString,
                                                                    if (obj["Importo"] == null) BookingActivity.calcoloprezzo_tot(
                                                                        BookingActivity.calcoloPrezzo2(
                                                                            obj["Marca"].asString,
                                                                            obj["Modello"].asString,
                                                                            obj["Anno"].asString
                                                                        ),
                                                                        data_in[2] + "/" + data_in[1] + "/" + data_in[0] + " " + data1[1],
                                                                        data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1]
                                                                    ) else obj["Importo"].asFloat,
                                                                    if (obj["Metodo"] == null) sharedpreferences.getString(
                                                                        "pref_card",
                                                                        "default"
                                                                    ).toString() else obj["Metodo"].asString,
                                                                    (data_in[2] + "/" + data_in[1] + "/" + data_in[0] + " " + data1[1]),
                                                                    (data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1])
                                                                )
                                                            )
                                                        }
                                                    }
                                                    binding.recyclerview.adapter = ListAdapter()
                                                }},{
                                                val dialog2 = onCreateDialogFailedConn()
                                                dialog2.show()
                                                dialog2.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                                            })

                                        }},
                                        {
                                            val dialog2 = onCreateDialogFailedConn()
                                            dialog2.show()
                                            dialog2.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                                        })
                            } else {
                                val dialog2 = onCreateDialogFailed()
                                dialog2.show()
                                dialog2.findViewById<TextView>(R.id.errore).text = "Non sei connesso ad Internet!"
                            }}
                            2->{val size = data.size
                                data.clear();
                                binding.recyclerview.adapter!!.notifyItemRangeRemoved(0, size);/*Todo: QUERY SETTIMANALE*/      if (ClientNetwork.isOnline(this.requireContext())) {
                                    DBMSManager.querySelect("SELECT\n" +
                                            "    v.Targa as Targa,\n" +
                                            "    v.Modello as Modello,\n" +
                                            "    v.Marca as Marca,\n" +
                                            "    p.Metodo_Pagamento as Metodo,\n" +
                                            "    p.Importo as Importo,\n" +
                                            "    p.Data_Pagamento,\n" +
                                            "    pr.Targa_Veicolo as Targa,\n" +
                                            "    pr.Data_Prenotazione_Inizio as Data_inizio,\n" +
                                            "    pr.Data_Prenotazione_Fine as Data_fine,\n" +
                                            "    ROUND(TIME_TO_SEC(timediff(p.Data_Pagamento, pr.Data_Prenotazione_Fine))/60)\n" +
                                            "FROM\n" +
                                            "    Veicoli v,\n" +
                                            "    Prenotazioni pr,\n" +
                                            "    Pagamenti p\n" +
                                            "WHERE\n" +
                                            "  v.Targa = pr.Targa_Veicolo\n" +
                                            "    AND p.ID_Utente = pr.ID_Utente\n" +
                                            "  AND ROUND(TIME_TO_SEC(timediff(p.Data_Pagamento, pr.Data_Prenotazione_Fine))/60) <= 2 AND (TIME_TO_SEC(timediff(p.Data_Pagamento, pr.Data_Prenotazione_Fine))/60) > 0 AND pr.ID_Utente = ${DBMSManager.utente!!.id}",
                                        {requireActivity().runOnUiThread {
                                            for(i in 0 until it.size()){
                                                val obj = it.get(i) as JsonObject
                                                var data1 = obj["Data_inizio"].asString.replace("T", " ")
                                                    .split(" ")
                                                var data2 = obj["Data_fine"].asString.replace("T", " ")
                                                    .split(" ")
                                                var data_in =data1.get(0).split("-")
                                                var data_fin = data2.get(0).split("-")
                                                data.add(
                                                    RiepilogoModel(
                                                        obj["Marca"].asString,
                                                        obj["Modello"].asString,
                                                        obj["Targa"].asString,
                                                        if (obj["Importo"].isJsonNull) BookingActivity.calcoloprezzo_tot(BookingActivity.calcoloPrezzo2(obj["Marca"].asString,obj["Modello"].asString,obj["Anno"].asString),data_in[2] + "/" + data_in[1] + "/" + data_in[0]+ " "+data1[1],data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1]) else obj["Importo"].asFloat,
                                                        if (obj["Metodo"].isJsonNull) sharedpreferences.getString("pref_card","default").toString() else obj["Metodo"].asString,
                                                        (data_in[2] + "/" + data_in[1] + "/" + data_in[0] + " " + data1[1]),
                                                        (data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1])
                                                    )
                                                )
                                            }
                                            DBMSManager.querySelect("SELECT v.Modello as Modello,v.Marca as Marca,v.Anno_Produzione as Anno, v.Targa as Targa, pr.Data_Prenotazione_Inizio as Data_inizio, pr.Data_Prenotazione_Fine as Data_fine\n" +
                                                    "    FROM Prenotazioni pr, Veicoli v\n" +
                                                    "    WHERE v.Targa = pr.Targa_Veicolo AND DATEDIFF(NOW(),pr.Data_Prenotazione_Inizio)<=7 AND DATEDIFF(NOW(),pr.Data_Prenotazione_Inizio)>=0 AND pr.ID_Utente = ${DBMSManager.utente!!.id}",{result ->
                                                requireActivity().runOnUiThread {
                                                    for (i in 0 until result.size()) {
                                                        val obj = result.get(i) as JsonObject
                                                        var data1 = obj["Data_inizio"].asString.replace("T", " ")
                                                            .split(" ")
                                                        var data2 = obj["Data_fine"].asString.replace("T", " ")
                                                            .split(" ")
                                                        var data_in = data1.get(0).split("-")
                                                        var data_fin = data2.get(0).split("-")
                                                        var flag = true

                                                        for (j in 0 until data.size){
                                                            Log.e("TAG", obj["Marca"].asString + " " + obj["Modello"].asString + " " + obj["Targa"].asString)
                                                            if(obj["Marca"].asString.equals(data[j].brand) && obj["Modello"].asString.equals(data[j].modello) && obj["Targa"].asString.equals(data[j].targa) &&  (data_in[2] + "/" + data_in[1] + "/" + data_in[0] + " " + data1[1]).equals(data[j].data_inizio) && (data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1]).equals(data[j].data_fine)){
                                                                flag  = false
                                                                break
                                                            }
                                                        }
                                                        if(flag) {
                                                            data.add(
                                                                RiepilogoModel(
                                                                    obj["Marca"].asString,
                                                                    obj["Modello"].asString,
                                                                    obj["Targa"].asString,
                                                                    if (obj["Importo"] == null) BookingActivity.calcoloprezzo_tot(
                                                                        BookingActivity.calcoloPrezzo2(
                                                                            obj["Marca"].asString,
                                                                            obj["Modello"].asString,
                                                                            obj["Anno"].asString
                                                                        ),
                                                                        data_in[2] + "/" + data_in[1] + "/" + data_in[0] + " " + data1[1],
                                                                        data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1]
                                                                    ) else obj["Importo"].asFloat,
                                                                    if (obj["Metodo"] == null) sharedpreferences.getString(
                                                                        "pref_card",
                                                                        "default"
                                                                    ).toString() else obj["Metodo"].asString,
                                                                    (data_in[2] + "/" + data_in[1] + "/" + data_in[0] + " " + data1[1]),
                                                                    (data_fin[2] + "/" + data_fin[1] + "/" + data_fin[0] + " " + data2[1])
                                                                )
                                                            )
                                                        }
                                                    }
                                                    binding.recyclerview.adapter = ListAdapter()
                                                }},{
                                                val dialog2 = onCreateDialogFailedConn()
                                                dialog2.show()
                                                dialog2.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                                            })

                                        }},
                                        {
                                            val dialog2 = onCreateDialogFailedConn()
                                            dialog2.show()
                                            dialog2.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                                        })
                            } else {
                                val dialog2 = onCreateDialogFailed()
                                dialog2.show()
                                dialog2.findViewById<TextView>(R.id.errore).text = "Non sei connesso ad Internet!"
                            }}
                        }

                    })
                .setNegativeButton("Annulla",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })
            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }
}