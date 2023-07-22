package it.am.gpsmodule.auth

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.google.gson.JsonObject
import it.am.gpsmodule.R
import it.am.gpsmodule.SplashScreen
import it.am.gpsmodule.map.AskiMap
import it.am.gpsmodule.databinding.ActivityLoginBinding
import it.am.gpsmodule.entity.Utente
import it.am.gpsmodule.auth.registration.sone_registration_fragment
import it.am.gpsmodule.utils.ClientNetwork
import it.am.gpsmodule.utils.DBMSManager
import it.am.gpsmodule.utils.FormValidator
import it.am.gpsmodule.utils.OnSwipeTouchListener

class Login : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //DISATTIVA LA MODALITA' DARK
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Aski - Autenticazione"

        binding.login.setOnClickListener {
            if(FormValidator.validateFormEmail(binding.emailLogin) && FormValidator.validateForm(binding.passwordLogin)){
                if(ClientNetwork.isOnline(this)) {
                    DBMSManager.querySelect("SELECT u.*, COUNT(p.ID_Utente) AS Noleggiate, n.Token AS Token,\n" +
                            "       COALESCE(SUM(DATEDIFF(Data_Prenotazione_Fine, Data_Prenotazione_Inizio)), 0) AS Giorni,\n" +
                            "       COALESCE(FORMAT(SUM(HOUR(TIMEDIFF(Data_Prenotazione_Inizio, Data_Prenotazione_Fine))) % 24, 1), 0) AS Ore\n" +
                            "FROM Utenti u\n" +
                            "LEFT JOIN Prenotazioni p ON p.ID_Utente = u.ID_Utente\n" +
                            "LEFT JOIN Notifiche n ON n.ID = u.ID_Utente\n" +
                            "WHERE u.Email = '${binding.emailLogin.text.toString()}' AND u.Password = '${binding.passwordLogin.text.toString()}'\n" +
                            "GROUP BY u.ID_Utente",{
                        result ->
                        runOnUiThread {
                            if (result.size() > 0) {
                                val obj = (result.get(0) as JsonObject)
                                if(obj.get("Nome") != null) {
                                    DBMSManager.utente = Utente(obj.get("ID_Utente").asInt,obj.get("Nome").asString,obj.get("Cognome").asString,obj.get("Data_Nascita").asString,obj.get("Indirizzo").asString,obj.get("CAP").asInt,obj.get("Città").asString,obj.get("Num_Telefono").asString,obj.get("Email").asString,obj.get("Password").asString,obj.get("Credito").asString,obj["Noleggiate"].asString,obj["Giorni"].asString,obj["Ore"].asString, if (obj["Token"].isJsonNull) "" else obj["Token"].asString)
                                    Log.e("TAG", DBMSManager.utente.toString())
                                    val sharedPreferences = getSharedPreferences(
                                        "AskiSharedPreferences",
                                        Context.MODE_PRIVATE
                                    )
                                    if (!sharedPreferences.contains("id_utente") && !sharedPreferences.contains("password_utente")) {
                                        var editor = sharedPreferences.edit()
                                        editor.putInt("id_utente", obj.get("ID_Utente").asInt) //DATO CHE E' ENTRATO NELL'APP IMPOSTIAMO IL PARAMETRO A FALSE
                                        editor.putString("password_utente", obj.get("Password").asString)
                                        editor.commit()
                                    }

                                    startActivity(Intent(this, AskiMap::class.java))
                                    finish()
                                }else{
                                    binding.emailLogin.text?.clear()
                                    binding.passwordLogin.text?.clear()
                                    var dialog = onCreateDialogFailed()
                                    dialog.show()
                                    dialog.findViewById<TextView>(R.id.errore).text =
                                        "Errore Utente inesistente!"
                                }
                            }else{
                                binding.emailLogin.text?.clear()
                                binding.passwordLogin.text?.clear()
                                var dialog = onCreateDialogFailed()
                                dialog.show()
                                dialog.findViewById<TextView>(R.id.errore).text =
                                    "Errore campi non validi!"
                            }
                        }
                    },{
                            val dialog = onCreateDialogFailedConn()
                            dialog.show()
                            dialog.findViewById<TextView>(R.id.errore).text =
                                "Errore connessione al server!"
                    })
                }else{
                    binding.emailLogin.text?.clear()
                    binding.passwordLogin.text?.clear()
                    var dialog = onCreateDialogFailed()
                    dialog.show()
                    dialog.findViewById<TextView>(R.id.errore).text = "Non sei Connesso ad Internet!"
                }
            }
        }

        binding.root.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeRight() {
                super.onSwipeRight()
                _back()
            }
        })
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
    private fun _back() {
        startActivity(Intent(applicationContext, Registration::class.java))
        overridePendingTransition(R.anim.right_to_left, R.anim.nothing_anim)
        finish()
    }

}