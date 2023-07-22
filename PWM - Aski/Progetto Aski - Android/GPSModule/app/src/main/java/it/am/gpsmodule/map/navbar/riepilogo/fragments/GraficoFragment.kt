package it.am.gpsmodule.map.navbar.riepilogo.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.gson.JsonObject
import it.am.gpsmodule.R
import it.am.gpsmodule.databinding.FragmentGraficoBinding
import it.am.gpsmodule.utils.ClientNetwork
import it.am.gpsmodule.utils.DBMSManager
import java.text.SimpleDateFormat

class GraficoFragment : Fragment() {

    private var _binding: FragmentGraficoBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGraficoBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(ClientNetwork.isOnline(this.requireContext())){
        DBMSManager.querySelect("SELECT\n" +
            "    v.Targa as Targa,\n" +
                    "    v.Modello as Modello,\n" +
                    "    v.Marca as Marca,\n" +
                    "    p.Metodo_Pagamento as Metodo,\n" +
                    "    p.Importo as Importo,\n" +
                    "    p.Data_Pagamento as Data_Pagamento,\n" +
                    "    pr.Targa_Veicolo as Targa,\n" +
                    "    pr.Data_Prenotazione_Inizio as Data_inizio,\n" +
                    "    pr.Data_Prenotazione_Fine as Data_fine,\n" +
                    "    ROUND(TIME_TO_SEC(timediff(p.Data_Pagamento, pr.Data_Prenotazione_Fine))/60),\n" +
                    "    YEAR(pr.Data_Prenotazione_Inizio),\n" +
                    "    YEAR(pr.Data_Prenotazione_Fine),\n" +
                    "    DATEDIFF(NOW(),pr.Data_Prenotazione_Inizio),\n" +
                    "    ROUND(TIME_TO_SEC(timediff(p.Data_Pagamento, pr.Data_Prenotazione_Fine))/60),\n" +
                    "    TIME_TO_SEC(timediff(p.Data_Pagamento, pr.Data_Prenotazione_Fine))/60 > 0\n" +
                    "FROM\n" +
                    "    Veicoli v,\n" +
                    "    Prenotazioni pr,\n" +
                    "    Pagamenti p\n" +
                    "WHERE\n" +
                    " v.Targa = pr.Targa_Veicolo\n" +
                    "    AND p.ID_Utente = pr.ID_Utente\n" +
                    "  AND ROUND(TIME_TO_SEC(timediff(p.Data_Pagamento, pr.Data_Prenotazione_Fine))/60) <= 2 AND YEAR(pr.Data_Prenotazione_Fine) =YEAR(NOW()) AND YEAR(pr.Data_Prenotazione_Inizio) = YEAR(NOW()) AND (TIME_TO_SEC(timediff(p.Data_Pagamento, pr.Data_Prenotazione_Fine))/60) > 0 AND pr.ID_Utente = ${DBMSManager.utente!!.id}", {
            requireActivity().runOnUiThread {
                var data:LineData? = null
                var dataSets: ArrayList<ILineDataSet> = ArrayList<ILineDataSet>()
                val entries = ArrayList<Entry>()
                for (i in 0 until it.size()) {
                    //TODO: [ANTONIO] FARE GRAFICO, QUI SOTTO C'E' UN SAMPLE
                    val obj = it[i] as JsonObject
                    val data = obj["Data_Pagamento"].asString.replace("T", " ").split(" ").get(0).split("-")
                    val date = data[2] + "/" + data[1] + "/" + data[0]
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy")
                    Log.e("TAG",dateFormat.parse(date).time.toFloat().toString())
                    entries.add(Entry(dateFormat.parse(date).time.toFloat()/60000,obj["Importo"].asFloat))
                }
                val dataSet = LineDataSet(entries, "Dati")
                dataSet.axisDependency = YAxis.AxisDependency.LEFT
                dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                dataSet.setDrawFilled(true)
                dataSet.setFillColor(Color.parseColor("#6BF3AD"))
                dataSet.setColor(Color.parseColor("#6BF3AD"))
                dataSet.setFillAlpha(255)
                dataSet.setDrawCircles(false)

                dataSets.add(dataSet as ILineDataSet) // Aggiungi il set di dati all'ArrayList

                data = LineData(dataSets) // Crea l'oggetto LineData

                binding.chart1.data = data // Assegna l'oggetto LineData al grafico
                binding.chart1.invalidate()
            }
        },{val dialog = onCreateDialogFailedConn()
        dialog.show()
        dialog.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
    })}else{
    val dialog = onCreateDialogFailed()
    dialog.show()
    dialog.findViewById<TextView>(R.id.errore).text = "Non sei connesso ad Internet!"
}

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
}