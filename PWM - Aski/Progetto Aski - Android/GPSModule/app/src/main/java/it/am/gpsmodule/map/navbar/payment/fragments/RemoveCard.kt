package it.am.gpsmodule.map.navbar.payment.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import it.am.gpsmodule.R
import it.am.gpsmodule.databinding.FragmentAddCardBinding
import it.am.gpsmodule.databinding.FragmentRemoveCardBinding
import it.am.gpsmodule.entity.Card
import it.am.gpsmodule.map.navbar.payment.PaymentActivity
import it.am.gpsmodule.utils.ClientNetwork
import it.am.gpsmodule.utils.DBMSManager
import it.am.gpsmodule.utils.FormValidator


class RemoveCard : Fragment() {

    private var _binding: FragmentRemoveCardBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRemoveCardBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = arguments
        val carta = bundle!!.getString("carta")
        val str:String = "*".repeat(carta!!.length-4)
        binding.carta.setText((str+carta.takeLast(4)).replace("....".toRegex(), "$0 "))
        var type= FormValidator.checkCard(carta)
        val resources: Resources = PaymentActivity.inst!!.resources
        val resourceId: Int = resources.getIdentifier(type.toLowerCase()+"_icon", "drawable", PaymentActivity.inst!!.packageName)
        binding.layoutCard.startIconDrawable = resources.getDrawable(resourceId)
        val sharedpreferences: SharedPreferences = PaymentActivity.inst!!.getSharedPreferences("AskiSharedPreferences", Context.MODE_PRIVATE)

        binding.rimuovi.setOnClickListener {
            if(ClientNetwork.isOnline(this.requireContext())) {
                DBMSManager.queryDelete("DELETE FROM Carte WHERE ID_Utente=${DBMSManager.utente?.id} AND Codice_Carta=${carta}",
                    {
                        if (sharedpreferences.contains("pref_card") && sharedpreferences.getString(
                                "pref_card",
                                "default"
                            ).equals(carta.toString())
                        ) {
                            val editor = sharedpreferences.edit()
                            editor.putString("pref_card", "default")
                            editor.apply()
                            PaymentActivity.check!!.visibility = View.VISIBLE
                            PaymentActivity.check1!!.visibility = View.INVISIBLE
                            PaymentActivity.check2!!.visibility = View.INVISIBLE
                        }
                        //RIMOZIONE CARTA AL LIVELLO GRAFICO:
                        if (bundle.getInt("numero") == 1) {
                            PaymentActivity.complete_card1 = null
                            PaymentActivity.cards.removeAt(0)
                            PaymentActivity.card_1!!.text = "Aggiungi una Carta"
                            var myDrawable: Drawable = PaymentActivity!!.inst!!.getResources()
                                .getDrawable(R.drawable.card_icon_small);
                            PaymentActivity.cardicon_1!!.setImageDrawable(myDrawable)
                        } else {
                            PaymentActivity.complete_card2 = null
                            PaymentActivity.cards.removeAt(1)
                            PaymentActivity.card_2!!.text = "Aggiungi una Carta"
                            var myDrawable: Drawable = PaymentActivity!!.inst!!.getResources()
                                .getDrawable(R.drawable.card_icon_small);
                            PaymentActivity.cardicon_2!!.setImageDrawable(myDrawable)

                        }
                        PaymentActivity.layoutb!!.visibility = View.INVISIBLE
                    },{
                        var dialog = onCreateDialogFailedConn()
                        dialog.show()
                        dialog.findViewById<TextView>(R.id.errore).text = "Errore connessione al server! L'app si chiuderà!"
                    })
            }else{
                var dialog = onCreateDialogFailed()
                dialog.show()
                dialog.findViewById<TextView>(R.id.errore).text = "Non sei Connesso ad Internet!"
            }
        }

        binding.predefinita.setOnClickListener {
            if(sharedpreferences.contains("pref_card")){
                val editor = sharedpreferences.edit()
                editor.putString("pref_card",carta.toString())
                editor.apply()
                if(bundle.getInt("numero") == 1){
                    PaymentActivity.check!!.visibility = View.INVISIBLE
                    PaymentActivity.check1!!.visibility = View.VISIBLE
                    PaymentActivity.check2!!.visibility = View.INVISIBLE
                }else{
                    PaymentActivity.check!!.visibility = View.INVISIBLE
                    PaymentActivity.check1!!.visibility = View.INVISIBLE
                    PaymentActivity.check2!!.visibility = View.VISIBLE
                }
            }
            PaymentActivity.layoutb!!.visibility = View.INVISIBLE
        }

    }
    fun onCreateDialogFailed(): Dialog {
        return (this?.let {
            val builder = AlertDialog.Builder(this.context)
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
            val builder = AlertDialog.Builder(this.context)
            val inflater = layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_failed_registration, null))
                .setPositiveButton("Chiudi") { dialog, id ->
                    this.requireActivity().finishAffinity()
                    System.exit(0)
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla"))
    }
}