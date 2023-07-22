package it.am.gpsmodule.map.navbar.profile

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import it.am.gpsmodule.R
import it.am.gpsmodule.databinding.ActivityMapProfiloBinding
import it.am.gpsmodule.utils.ClientNetwork
import it.am.gpsmodule.utils.DBMSManager
import it.am.gpsmodule.utils.FormValidator


class MapProfiloActivity : AppCompatActivity() {

    lateinit var binding: ActivityMapProfiloBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapProfiloBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nome.text = DBMSManager.utente?.nome
        binding.cognome.text = DBMSManager.utente?.cognome
        binding.dNascita.text = DBMSManager.utente?.data_nascita
        binding.residenza.text = DBMSManager.utente?.indirizzo+", "+DBMSManager.utente?.citta+", " + DBMSManager.utente?.cap
        binding.email.text = DBMSManager.utente?.email
        var size = DBMSManager.utente?.password?.length as Int
        val str:String = "*".repeat(size)
        binding.password.text = str
        binding.telefono.text = DBMSManager.utente?.telefono


        binding.emailLayout.setOnClickListener {
            onCreateDialog(R.layout.dialog_editemail_profile,"email").show()
        }
        binding.passwordLayout.setOnClickListener {
            onCreateDialog(R.layout.dialog_editpassword_profile,"password").show()
        }
        binding.telefonoLayout.setOnClickListener {
            onCreateDialog(R.layout.dialog_edittelefono_profile,"telefono").show()
        }



    }

     fun onCreateDialog(layout:Int, scelta:String): Dialog {
        return this?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = this.layoutInflater;
            var dialogview = inflater.inflate(layout, null)

            lateinit var emailEditText:TextInputEditText
            lateinit var passwordEditText:TextInputEditText
            lateinit var telefonoEditText:TextInputEditText

            if(scelta.equals("email"))
                 emailEditText = dialogview.findViewById<TextInputEditText>(R.id.email_change)
            if(scelta.equals("password"))
                passwordEditText = dialogview.findViewById<TextInputEditText>(R.id.password_change)
            if(scelta.equals("telefono"))
                telefonoEditText = dialogview.findViewById<TextInputEditText>(R.id.phone_change)

            builder.setView(dialogview)
                // Add action buttons
                .setPositiveButton("Conferma",
                    DialogInterface.OnClickListener { dialog, id ->
                        if(scelta.equals("email")){
                            if(FormValidator.validateFormEmail(emailEditText)){
                                if(ClientNetwork.isOnline(this.applicationContext)) {
                                    DBMSManager.queryUpdate(
                                        "UPDATE Utenti SET Email = '${emailEditText.text.toString()}' WHERE ID_Utente = ${DBMSManager.utente?.id};"
                                    ,{ result ->
                                        runOnUiThread {
                                            DBMSManager.utente?.email = emailEditText.text.toString()
                                            binding.email.text = DBMSManager.utente?.email
                                            Toast.makeText(applicationContext,"Email aggiornata!",Toast.LENGTH_SHORT).show()
                                        }
                                    },{
                                            val dialog = onCreateDialogFailedConn()
                                            dialog.show()
                                            dialog.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                                        })
                                }else{
                                    var dialog = onCreateDialogFailed()
                                    dialog.show()
                                    dialog.findViewById<TextView>(R.id.errore).text = "Non sei Connesso ad Internet!"
                                }
                            }else{
                                val mySnackbar = Snackbar.make(binding.root, "Email non valida!", Snackbar.LENGTH_SHORT)
                                mySnackbar.setAction("Correggi", View.OnClickListener {  onCreateDialog(layout,scelta).show()})
                                mySnackbar.show()
                            }
                        }else if(scelta.equals("password")){
                            if(FormValidator.validateFormPassword(passwordEditText)){
                                if(ClientNetwork.isOnline(this.applicationContext)) {
                                    DBMSManager.queryUpdate(
                                        "UPDATE Utenti SET Password = '${passwordEditText.text.toString()}' WHERE ID_Utente = ${DBMSManager.utente?.id};"
                                    ,{ result ->
                                        runOnUiThread {
                                            DBMSManager.utente?.password = passwordEditText.text.toString()
                                            var size = DBMSManager.utente?.password?.length as Int
                                            val str:String = "*".repeat(size)
                                            binding.password.text = str
                                            val sharedPreferences =  getSharedPreferences("AskiSharedPreferences",
                                                Context.MODE_PRIVATE)
                                            var editor = sharedPreferences.edit()
                                            editor.putString("password_utente",passwordEditText.text.toString())
                                            editor.commit()
                                            Toast.makeText(applicationContext,"Password aggiornata!",Toast.LENGTH_SHORT).show()
                                        }
                                    },{
                                            val dialog = onCreateDialogFailedConn()
                                            dialog.show()
                                            dialog.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                                        })
                                }else{
                                    var dialog = onCreateDialogFailed()
                                    dialog.show()
                                    dialog.findViewById<TextView>(R.id.errore).text = "Non sei Connesso ad Internet!"
                                }
                            }else{
                                val mySnackbar = Snackbar.make(binding.root, "Password non valida!", Snackbar.LENGTH_SHORT)
                                mySnackbar.setAction("Correggi", View.OnClickListener {  onCreateDialog(layout,scelta).show()})
                                mySnackbar.show()
                            }
                        }else{
                            if(FormValidator.validateFormPhone(telefonoEditText)){
                                if(ClientNetwork.isOnline(this.applicationContext)) {
                                    DBMSManager.queryUpdate(
                                        "UPDATE Utenti SET Num_Telefono = '${telefonoEditText.text.toString()}' WHERE ID_Utente = ${DBMSManager.utente?.id};"
                                    ,{ result ->
                                        runOnUiThread {
                                            DBMSManager.utente?.telefono = telefonoEditText.text.toString()
                                            binding.telefono.text = DBMSManager.utente?.telefono
                                            Toast.makeText(applicationContext,"Numero di Telefono aggiornato!",Toast.LENGTH_SHORT).show()
                                        }
                                    },{
                                            val dialog = onCreateDialogFailedConn()
                                            dialog.show()
                                            dialog.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                                        })
                                }else{
                                    var dialog = onCreateDialogFailed()
                                    dialog.show()
                                    dialog.findViewById<TextView>(R.id.errore).text = "Non sei Connesso ad Internet!"
                                }
                            }else{
                                val mySnackbar = Snackbar.make(binding.root, "Numero di Telefono non valido!", Snackbar.LENGTH_SHORT)
                                mySnackbar.setAction("Correggi", View.OnClickListener {  onCreateDialog(layout,scelta).show()})
                                mySnackbar.show()
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