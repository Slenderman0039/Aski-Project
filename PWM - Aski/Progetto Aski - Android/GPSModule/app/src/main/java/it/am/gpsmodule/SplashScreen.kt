package it.am.gpsmodule

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.JsonObject
import it.am.gpsmodule.auth.Login
import it.am.gpsmodule.auth.LoginRegistration
import it.am.gpsmodule.databinding.ActivitySplashScreenBinding
import it.am.gpsmodule.entity.Utente
import it.am.gpsmodule.map.AskiMap
import it.am.gpsmodule.utils.ClientNetwork
import it.am.gpsmodule.utils.DBMSManager
import it.am.gpsmodule.utils.OnSwipeTouchListener


class SplashScreen : AppCompatActivity() {

    lateinit var binding:ActivitySplashScreenBinding
    companion object {
        private const val MSG_CONTINUE = 1234
        private const val DELAY: Long = 2000
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //DISATTIVA LA MODALITA' DARK
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.setOnTouchListener(object : OnSwipeTouchListener(this@SplashScreen) {
            override fun onSwipeUp() {
                super.onSwipeUp()
                _continue()
            }
        })


       // mHandler.sendEmptyMessageDelayed(MSG_CONTINUE, DELAY)


    }

    override fun onDestroy() {
        mHandler.removeMessages(MSG_CONTINUE)
        super.onDestroy()
    }

    private fun _continue() {
        val sharedPreferences =  getSharedPreferences("AskiSharedPreferences",Context.MODE_PRIVATE)
        if(!sharedPreferences.contains("FirstTime")){ //SE NON LO CONTIENE E' LA PRIMA VOLTA CHE ENTRA NELL'APP
            var editor = sharedPreferences.edit()
            editor.putBoolean("FirstTime",false) //DATO CHE E' ENTRATO NELL'APP IMPOSTIAMO IL PARAMETRO A FALSE
            editor.commit()
            startActivity(Intent(this, LoginRegistration::class.java))
            overridePendingTransition(R.anim.bottom_to_top, R.anim.nothing_anim)
            finish()
        }else{
            if(sharedPreferences.contains("id_utente") && sharedPreferences.contains("password_utente")){
                var id:Int = sharedPreferences.getInt("id_utente",0)
                var password: String? = sharedPreferences.getString("password_utente","")
                if(ClientNetwork.isOnline(this)) {
                    DBMSManager.querySelect("SELECT u.*, COUNT(p.ID_Utente) AS Noleggiate, n.Token AS Token,\n" +
                            "       COALESCE(SUM(DATEDIFF(Data_Prenotazione_Fine, Data_Prenotazione_Inizio)), 0) AS Giorni,\n" +
                            "       COALESCE(FORMAT(SUM(HOUR(TIMEDIFF(Data_Prenotazione_Inizio, Data_Prenotazione_Fine))) % 24, 1), 0) AS Ore\n" +
                            "FROM Utenti u\n" +
                            "LEFT JOIN Prenotazioni p ON p.ID_Utente = u.ID_Utente\n" +
                            "LEFT JOIN Notifiche n ON n.ID = u.ID_Utente\n" +
                            "WHERE u.ID_Utente = $id AND u.Password = '$password'\n" +
                            "GROUP BY u.ID_Utente",{
                            result ->
                        runOnUiThread {
                            if (result.size() > 0) {
                                val obj = (result.get(0) as JsonObject)
                                if(obj.get("Nome") != null){
                                Log.e("TAG",obj.toString())
                                DBMSManager.utente = Utente(obj.get("ID_Utente").asInt,obj.get("Nome").asString,obj.get("Cognome").asString,obj.get("Data_Nascita").asString,obj.get("Indirizzo").asString,obj.get("CAP").asInt,obj.get("Città").asString,obj.get("Num_Telefono").asString,obj.get("Email").asString,obj.get("Password").asString,obj.get("Credito").asString,obj["Noleggiate"].asString,obj["Giorni"].asString,obj["Ore"].asString,if (obj["Token"].isJsonNull) "" else obj["Token"].asString)
                                startActivity(Intent(this, AskiMap::class.java))
                                overridePendingTransition(R.anim.bottom_to_top, R.anim.nothing_anim)
                                finish()
                                }else{
                                    throw java.lang.Exception()
                                }
                            }else{
                                startActivity(Intent(this, Login::class.java))
                                overridePendingTransition(R.anim.bottom_to_top, R.anim.nothing_anim)
                                finish()
                            }
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
                startActivity(Intent(this, Login::class.java))
                overridePendingTransition(R.anim.bottom_to_top, R.anim.nothing_anim)
                finish()
            }
        }

    }
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_CONTINUE -> _continue()
            }
        }
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
                    this.finish()
                    System.exit(0)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla"))
    }
}