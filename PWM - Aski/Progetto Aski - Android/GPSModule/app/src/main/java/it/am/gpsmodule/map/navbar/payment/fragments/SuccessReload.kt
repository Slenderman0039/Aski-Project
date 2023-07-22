package it.am.gpsmodule.map.navbar.payment.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import it.am.gpsmodule.R
import it.am.gpsmodule.auth.Registration
import it.am.gpsmodule.databinding.FragmentReloadCardBinding
import it.am.gpsmodule.databinding.FragmentSuccessReloadBinding
import it.am.gpsmodule.map.AskiMap
import it.am.gpsmodule.map.navbar.payment.PaymentActivity
import it.am.gpsmodule.utils.ClientNetwork
import it.am.gpsmodule.utils.DBMSManager


class SuccessReload : Fragment() {

    private var _binding: FragmentSuccessReloadBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSuccessReloadBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var taglio: Double? = arguments?.getDouble("taglio")
        if(taglio !=null){
            if (ClientNetwork.isOnline(this.requireContext())) {
                DBMSManager.queryUpdate("UPDATE Utenti SET Credito=Credito+$taglio WHERE ID_Utente=${DBMSManager.utente?.id}",{
            Handler().postDelayed({ //faccio aspettare 5secondi
                var somma = DBMSManager.utente!!.credito!!.toDouble()
                somma += taglio
                DBMSManager.utente!!.credito= somma.toString()
                PaymentActivity.saldo!!.text = DBMSManager.utente!!.credito+"€"
                AskiMap.credito!!.text = DBMSManager.utente!!.credito+"€"
                PaymentActivity.layoutb!!.visibility = View.INVISIBLE



            }, 3000)},{
                    val dialog = onCreateDialogFailedConn()
                    dialog.show()
                    dialog.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                })
            }else{
                val dialog = onCreateDialogFailed()
                dialog.show()
                dialog.findViewById<TextView>(R.id.errore).text = "Non sei Connesso ad Internet!"
            }
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
                    dialog.cancel()
                    this.requireActivity().finishAffinity()
                    System.exit(0)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla"))
    }
}