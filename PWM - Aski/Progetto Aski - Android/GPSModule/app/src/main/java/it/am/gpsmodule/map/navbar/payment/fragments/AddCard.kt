package it.am.gpsmodule.map.navbar.payment.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import it.am.gpsmodule.R
import it.am.gpsmodule.databinding.FragmentAddCardBinding
import it.am.gpsmodule.entity.Card
import it.am.gpsmodule.map.navbar.payment.PaymentActivity
import it.am.gpsmodule.utils.ClientNetwork
import it.am.gpsmodule.utils.DBMSManager
import it.am.gpsmodule.utils.FormValidator


class AddCard : Fragment() {
    private var _binding: FragmentAddCardBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddCardBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.carta.addTextChangedListener {
            if (FormValidator.checkCard(binding.carta.text.toString()).equals("Visa")) {
                binding.layoutCard.startIconDrawable = resources.getDrawable(R.drawable.visa_icon)
            } else if (FormValidator.checkCard(binding.carta.text.toString())
                    .equals("Mastercard")
            ) {
                binding.layoutCard.startIconDrawable =
                    resources.getDrawable(R.drawable.mastercard_icon)
            } else {
                binding.layoutCard.startIconDrawable =
                    resources.getDrawable(R.drawable.card_icon)
            }
        }
        binding.conferma.setOnClickListener {
            var dialog = onCreateDialogFailed()
            if(FormValidator.validateFormCard(binding.carta) && FormValidator.validateFormCVV(binding.cvv) && FormValidator.validateFormScadenzaCard(binding.scadenza)){
                if(ClientNetwork.isOnline(this.requireActivity())){
                     if(PaymentActivity.cards.size == 2){
                        dialog.show()
                        dialog.findViewById<TextView>(R.id.errore).text = "Non puoi inserire più di due Carte!"
                    }else{
                        var errore:Boolean = false
                         val dati:ArrayList<String> = binding.scadenza.text.toString().split("/") as ArrayList<String>
                         val data_card = dati.get(1).toString() + "-" + dati.get(0).toString() + "-" +  "01"
                         var card = (Card(binding.carta.text.toString(),binding.cvv.text.toString(),data_card))
                         Log.e("CARTA:", card.anno+" "+card.codice+" "+card.cvc)
                         for (cardlist in PaymentActivity.cards) {
                             if (cardlist.equals(card)) {
                                 dialog.show()
                                 dialog.findViewById<TextView>(R.id.errore).text =
                                     "La carta già esiste!"
                                 errore = true
                                 break
                             }
                         }

                        if(!errore){
                        DBMSManager.queryInsert("INSERT INTO Carte VALUES(${DBMSManager.utente?.id},'${binding.carta.text.toString()}',${binding.cvv.text.toString()},'${data_card}')",{
                                result ->
                            this.requireActivity().runOnUiThread {
                                PaymentActivity.cards.add(Card(binding.carta.toString(),binding.cvv.text.toString(),data_card))
                                val str: String =
                                    "*".repeat(binding.carta.text.toString().length - 4)
                                if (PaymentActivity.card_1!!.text.toString()
                                        .equals("Aggiungi una Carta")
                                ) {
                                    PaymentActivity.complete_card1 = binding.carta.text.toString()
                                    PaymentActivity.card_1!!.text =
                                        (str + binding.carta.text.toString()
                                            .takeLast(4)).replace("....".toRegex(), "$0 ")
                                    PaymentActivity!!.cardicon_1!!.setImageDrawable(binding.layoutCard.startIconDrawable)
                                } else {
                                    PaymentActivity.complete_card2 = binding.carta.text.toString()
                                    PaymentActivity.card_2!!.text =
                                        (str + binding.carta.text.toString()
                                            .takeLast(4)).replace("....".toRegex(), "$0 ")
                                    PaymentActivity!!.cardicon_2!!.setImageDrawable(binding.layoutCard.startIconDrawable)
                                }

                                PaymentActivity.layoutb!!.visibility = View.INVISIBLE
                                val imm: InputMethodManager = PaymentActivity.inst!!.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                                if (imm.isActive)
                                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)

                                val toast = Toast.makeText(
                                    PaymentActivity.inst!!,
                                    (str + binding.carta.text.toString().takeLast(4)).replace(
                                        "....".toRegex(),
                                        "$0 "
                                    ) + " aggiunta!",
                                    Toast.LENGTH_SHORT
                                )
                                toast.show()
                                if (imm.isActive){imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)}
                            }
                            },{
                            val dialog = onCreateDialogFailedConn()
                            dialog.show()
                            dialog.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                        })
                        }else{
                            binding.carta.setError("La carta già esiste!")

                        }
                    }
                }else{
                    val dialog = onCreateDialogFailed()
                    dialog.show()
                    dialog.findViewById<TextView>(R.id.errore).text = "Non sei Connesso ad Internet!"
                }
            }
        }
    }
    fun onCreateDialogFailed(): Dialog {
        return (this?.let {
            val builder = AlertDialog.Builder(PaymentActivity.inst!!)
            val inflater = this.layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_failed_registration, null))
                .setPositiveButton("Chiudi") { dialog, id ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla"))
    }
    fun onCreateDialogFailedConn(): Dialog {
        return (this?.let {
            val builder = AlertDialog.Builder(PaymentActivity.inst!!)
            val inflater = this.layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_failed_registration, null))
                .setPositiveButton("Chiudi") { dialog, id ->
                    dialog.cancel()
                    this.activity?.finishAffinity()
                    System.exit(0)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla"))
    }
    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}