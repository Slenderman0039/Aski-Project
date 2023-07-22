package it.am.gpsmodule.map.navbar.payment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import it.am.gpsmodule.R
import it.am.gpsmodule.databinding.ActivityPaymentBinding
import it.am.gpsmodule.entity.Card
import it.am.gpsmodule.map.navbar.payment.fragments.AddCard
import it.am.gpsmodule.map.navbar.payment.fragments.ReloadCard
import it.am.gpsmodule.map.navbar.payment.fragments.RemoveCard
import it.am.gpsmodule.utils.ClientNetwork
import it.am.gpsmodule.utils.DBMSManager
import it.am.gpsmodule.utils.FormValidator
class PaymentActivity : AppCompatActivity() {
    lateinit var binding: ActivityPaymentBinding
    companion object{
        var inst:PaymentActivity? = null
        var cards:ArrayList<Card> = ArrayList()
        var card_1: TextView? = null
        var cardicon_1:ImageView? = null
        var card_2: TextView? = null
        var cardicon_2:ImageView? = null
        var layoutb:LinearLayout?=null
        var complete_card1:String? = null
        var complete_card2:String? = null
        var check:TextView? = null
        var check1:TextView? = null
        var check2:TextView? = null
        var saldo:TextView?=null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        inst = this
        setContentView(binding.root)
        card_1 = binding.card1
        card_2 = binding.card2
        cardicon_1 = binding.cardicon1
        cardicon_2 = binding.cardicon2
        check = binding.Check
        check1 = binding.Check1
        check2 = binding.Check2
        layoutb = binding.layoutBottom
        saldo = binding.saldo


        cards.clear()
        binding.saldo.text = DBMSManager.utente?.credito.toString()+"€"
        if(ClientNetwork.isOnline(this)){
        DBMSManager.querySelect("SELECT * FROM Carte WHERE ID_Utente = ${DBMSManager.utente?.id}",{
                result ->
            runOnUiThread {
                if(result.size() > 0){
                    for (i in 0 until result.size()){
                        val obj: JsonObject = result.get(i) as JsonObject
                        cards.add(Card(obj.get("Codice_Carta").asString,obj.get("CVC").asInt.toString(),obj.get("Anno_Scadenza").asString))
                        for (card in cards)
                            Log.e("CARTA:", card.anno+" "+card.codice+" "+card.cvc)
                        if(i==0){
                            var carta = obj.get("Codice_Carta").asString
                            val str:String = "*".repeat(carta.length-4)
                            binding.card1.text = (str+carta.takeLast(4)).replace("....".toRegex(), "$0 ")
                            var type=FormValidator.checkCard(obj.get("Codice_Carta").asString)
                            val resources: Resources = this.applicationContext.resources
                            val resourceId: Int = resources.getIdentifier(type.toLowerCase()+"_icon", "drawable", this.applicationContext.packageName)
                            complete_card1 = carta
                            binding.cardicon1.setImageResource(resourceId)
                        }else if(i==1){
                            var carta = obj.get("Codice_Carta").asString
                            val str:String = "*".repeat(carta.length-4)
                            complete_card2 = carta
                            binding.card2.text = (str+carta.takeLast(4)).replace("....".toRegex(), "$0 ")
                            var type=FormValidator.checkCard(obj.get("Codice_Carta").asString)
                            val resources: Resources = this.applicationContext.resources
                            val resourceId: Int = resources.getIdentifier(type.toLowerCase()+"_icon", "drawable", this.applicationContext.packageName)
                            binding.cardicon2.setImageResource(resourceId)
                        }
                    }
                }
                val sharedpreferences:SharedPreferences = getSharedPreferences("AskiSharedPreferences", Context.MODE_PRIVATE)
                if(!sharedpreferences.contains("pref_card")){
                    val editor = sharedpreferences.edit()
                    editor.putString("pref_card","default")
                    editor.apply()
                }else{
                    var pref:String = sharedpreferences.getString("pref_card","default").toString()
                    if(pref.equals("default")){
                        binding.Check.visibility = View.VISIBLE
                        binding.Check1.visibility = View.INVISIBLE
                        binding.Check2.visibility = View.INVISIBLE
                    }else{
                        var i:Int = 0
                        for (card in cards) {
                            if (card.codice.toString().equals(pref)) {

                                if (i == 0) {
                                    binding.Check.visibility = View.INVISIBLE
                                    binding.Check1.visibility = View.VISIBLE
                                    binding.Check2.visibility = View.INVISIBLE
                                } else {
                                    binding.Check.visibility = View.INVISIBLE
                                    binding.Check1.visibility = View.INVISIBLE
                                    binding.Check2.visibility = View.VISIBLE
                                }
                            }
                            i++
                        }
                    }
                }
            }
        },{
            val dialog = onCreateDialogFailedConn()
            dialog.show()
            dialog.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
        })}else{
            val dialog = onCreateDialogFailed()
            dialog.show()
            dialog.findViewById<TextView>(R.id.errore).text = "Non sei connesso ad Internet!"
        }

        val sharedpreferences:SharedPreferences = getSharedPreferences("AskiSharedPreferences", Context.MODE_PRIVATE)

        binding.contoaski.setOnClickListener {
            if(sharedpreferences.contains("pref_card") && !sharedpreferences.getString("pref_card","default").equals("default") ) {
                val editor = sharedpreferences.edit()
                editor.putString("pref_card", "default")
                editor.apply()
                binding.Check.visibility = View.VISIBLE
                binding.Check1.visibility = View.INVISIBLE
                binding.Check2.visibility = View.INVISIBLE
            }
        }



        //AGGIUNGI o CANCELLA CARTA1
        binding.addcard1.setOnClickListener {
            if(binding.card1.text.equals("Aggiungi una Carta")){
                if(binding.layoutBottom.visibility == View.INVISIBLE){
                    supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id, AddCard()).commit()
                    binding.layoutBottom.visibility = View.VISIBLE
                }else{
                    binding.layoutBottom.visibility = View.INVISIBLE
                }
            }else{
                if(binding.layoutBottom.visibility == View.INVISIBLE){
                    val mBundle = Bundle()
                    if(complete_card1!=null) {
                        mBundle.putString("carta", complete_card1)
                        mBundle.putInt("numero", 1)
                        val mFragment = RemoveCard()
                        mFragment.arguments = mBundle
                        supportFragmentManager.beginTransaction()
                            .replace(binding.fragmentContainerView.id, mFragment).commit()
                        binding.layoutBottom.visibility = View.VISIBLE
                    }
                }else{
                    binding.layoutBottom.visibility = View.INVISIBLE
                }
            }
        }
        binding.layoutCloseBottom.setOnClickListener {
            if(binding.layoutBottom.visibility == View.VISIBLE){
                binding.layoutBottom.visibility = View.INVISIBLE
            }
        }
        //AGGIUNGI o CANCELLA CARTA2
        binding.addcard2.setOnClickListener {
            if(binding.card2.text.equals("Aggiungi una Carta")){
                if(binding.layoutBottom.visibility == View.INVISIBLE){
                    supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id, AddCard()).commit()
                    binding.layoutBottom.visibility = View.VISIBLE
                }else{
                    binding.layoutBottom.visibility = View.INVISIBLE
                }
            }else{
                if(binding.layoutBottom.visibility == View.INVISIBLE){
                    val mBundle = Bundle()
                    if(complete_card2!=null){
                        mBundle.putString("carta",complete_card2)
                        mBundle.putInt("numero", 2)
                        val mFragment = RemoveCard()
                        mFragment.arguments = mBundle
                        supportFragmentManager.beginTransaction().replace(binding.fragmentContainerView.id, mFragment).commit()
                        binding.layoutBottom.visibility = View.VISIBLE
                    }
                }else{
                    binding.layoutBottom.visibility = View.INVISIBLE
                }
            }
        }
        binding.reload.setOnClickListener {
            if(cards.size != 0) {
                if (binding.layoutBottom.visibility == View.VISIBLE) {
                    binding.layoutBottom.visibility = View.INVISIBLE
                } else {
                    supportFragmentManager.beginTransaction()
                        .replace(binding.fragmentContainerView.id, ReloadCard()).commit()
                    binding.layoutBottom.visibility = View.VISIBLE
                }
            }else{
                var dialog = onCreateDialogFailed()
                dialog.show()
                dialog.findViewById<TextView>(R.id.errore).text="Inserisci prima una carta!"
                PaymentActivity.layoutb?.visibility = View.INVISIBLE
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