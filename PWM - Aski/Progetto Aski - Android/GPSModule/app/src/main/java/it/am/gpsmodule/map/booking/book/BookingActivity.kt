package it.am.gpsmodule.map.booking.book

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.core.text.isDigitsOnly
import androidx.core.widget.addTextChangedListener
import com.google.gson.JsonObject
import it.am.gpsmodule.R
import it.am.gpsmodule.databinding.ActivityBookingBinding
import it.am.gpsmodule.databinding.ActivityRegistrationBinding
import it.am.gpsmodule.entity.MapCar
import it.am.gpsmodule.entity.Prenotazione
import it.am.gpsmodule.entity.ProprietarioAuto
import it.am.gpsmodule.map.AskiMap
import it.am.gpsmodule.map.navbar.owncar.MapOwnCarActivityList
import it.am.gpsmodule.map.navbar.payment.PaymentActivity
import it.am.gpsmodule.utils.*
import it.am.gpsmodule.utils.fcm.FCMNotification
import it.am.gpsmodule.utils.fcm.FCMNotificationData
import it.am.gpsmodule.utils.fcm.FCMService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.Normalizer.Form
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

class BookingActivity : AppCompatActivity() {
    lateinit var binding: ActivityBookingBinding
    var flag = false
    companion object{

        fun calcoloPrezzo2(brand:String,modello:String,anno:String):Float{
            var prezzo_h:Float = 0f
            var db: SQLiteHelper? = SQLiteHelper.getInstance(AskiMap.instances()!!)
            val dbopen: SQLiteDatabase? = db?.writableDatabase
            var c: Cursor? = null
            c = dbopen?.rawQuery("SELECT Marca,Modello, CAST(AVG(Prezzo) AS INTEGER) AS Prezzo, CAST(AVG(Km) AS INTEGER) AS Km FROM ModelliAuto WHERE Marca = '$brand' AND Modello = '$modello'",null)
            if (c!!.moveToNext()){
                val year = Calendar.getInstance().get(Calendar.YEAR)
                val dist = year - anno.toInt()
                val prezzo = c.getInt(2)
                val km = c.getInt(3)
                Log.e("TAG", "km:" + km.toString() +  " prezzo: " + prezzo.toString())
                when{
                    (prezzo in 0 until  10000) -> {
                        Log.e("TAG",(prezzo.toFloat()*0.0035f).toString())
                        Log.e("TAG",(km.toFloat()*0.000025f).toString())
                        Log.e("TAG",(dist*0.1f).toString())

                        prezzo_h = ((prezzo.toFloat()*0.0035f)-(km.toFloat()*0.000025f)-(dist*0.1f)).absoluteValue
                    };
                    (prezzo in 10000 until 20000) ->{prezzo_h = ((prezzo.toFloat()*0.0035f)-(km.toFloat()*0.0003f)-(dist*0.2f)).absoluteValue}
                    (prezzo in 20000 until 40000) ->{prezzo_h = ((prezzo.toFloat()*0.0023f)-(km.toFloat()*0.0003f)-(dist*0.25f)).absoluteValue}
                    (prezzo in 40000 until 80000) ->{prezzo_h = ((prezzo.toFloat()*0.0014f)-(km.toFloat()*0.00004f)-(dist*0.3f)).absoluteValue}
                    (prezzo in 80000 until 130000) ->{prezzo_h = ((prezzo.toFloat()*0.0035f)-(km.toFloat()*0.00005f)-(dist*0.35f)).absoluteValue}
                    (prezzo >=130000) ->{prezzo_h = ((prezzo*0.0012f)-(km*0.00007f)-(dist*0.4f))}
                }
            }
            return roundOffDecimal(prezzo_h.toDouble())!!.toFloat()
        }
        fun roundOffDecimal(number: Double): Double? {
            val truncatedNumber = BigDecimal(number).setScale(2, RoundingMode.DOWN).toDouble()
            return truncatedNumber
        }
         fun calcoloprezzo_tot(prezzoh:Float,data_init:String,data_fine:String):Float{
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm")
            val data_i = sdf.parse(data_init)
            val data_f = sdf.parse(data_fine)
            var prezzo_tot:Float = 0f
            val days: Long = (data_f.time - data_i.time)/86400000
            val hour = (data_f.time - data_i.time)%86400000/3600000
            val min = (data_f.time -data_i.time)%86400000%3600000/60000
            Log.e("TAG" ," giorni: "+  days.toString() + " ore $hour")
                if(days > 0){prezzo_tot += prezzoh*(days*24)- ((prezzoh*(days*24))/100)*50};
                if(hour >= 12 && hour < 24){prezzo_tot += (prezzoh*hour)- (prezzoh*hour/100)*25}
                if(hour >= 6 && hour < 12){prezzo_tot += (prezzoh*hour)- (prezzoh*hour/100)*15}
                if(hour < 6){prezzo_tot += prezzoh*hour}
                if(min > 0){prezzo_tot+=(prezzoh/60)*min}
            return  roundOffDecimal(prezzo_tot.toDouble())!!.toFloat()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val sharedpreferences: SharedPreferences = getSharedPreferences("AskiSharedPreferences", Context.MODE_PRIVATE)
        if(sharedpreferences.contains("pref_card")){
            var pref:String = sharedpreferences.getString("pref_card","default").toString()
            if(pref.equals("default")){
                binding.card.setText("Conto Aski")
                 var uri:String = "logo_vq"
                    binding.cvv.isEnabled = false
                    binding.cvv.isFocusable = false
                    binding.cvv.isFocusableInTouchMode = false
                val resourceId: Int = resources.getIdentifier(uri, "drawable", this.applicationContext.packageName)
                binding.outlinedTextFieldcard.startIconDrawable = resources.getDrawable(resourceId)
            }else{
                val resourceId: Int = resources.getIdentifier( FormValidator.checkCard(pref).toLowerCase()+"_icon", "drawable", this.applicationContext.packageName)
                binding.outlinedTextFieldcard.startIconDrawable = resources.getDrawable(resourceId)
                binding.card.setText("**** "+pref.takeLast(4))
            }
        }else{
            binding.card.setText("Conto Aski")
            var uri:String = "logo_vq"
            binding.cvv.isEnabled = false
            binding.cvv.isFocusable = false
            binding.cvv.isFocusableInTouchMode = false
            val resourceId: Int = resources.getIdentifier(uri, "drawable", this.applicationContext.packageName)
            binding.outlinedTextFieldcard.startIconDrawable = resources.getDrawable(resourceId)
        }
        var car: MapCar? = intent.getParcelableExtra<MapCar>("car")
        if(car != null){
            binding.brandmodello.setText(car.brand+" - " + car.modello)
            binding.carburante.setText(car.carburante)
            binding.prezzoh.setText(intent.getStringExtra("prezzoh") + "€/h")
        }
        if(ClientNetwork.isOnline(this)) {
            DBMSManager.querySelect("SELECT Nome,Cognome,Num_Telefono FROM Utenti WHERE ID_Utente = ${car?.id} ",{
                    result ->
                runOnUiThread {
                        val obj = (result.get(0) as JsonObject)
                        Log.e("TAG",obj.toString())
                        val proprietario = ProprietarioAuto(car?.id.toString(),obj["Nome"].asString,obj["Cognome"].asString,obj["Num_Telefono"].asString)
                    binding.proprietario.text = proprietario.nome + " " + proprietario.cognome
                    DBMSManager.querySelect("SELECT Targa,AVG(Valutazione) AS Media, COUNT(Targa) AS Num_recensioni FROM Valutazioni WHERE Targa = '${car?.targa}'",
                        {
                            val obj2 = (it.get(0) as JsonObject)
                            if(obj2["Num_recensioni"].asInt == 0 ){
                                binding.recensioni.text = "Nessuna"
                        }else{
                            binding.recensioni.text = "${DecimalFormat("#.#").format(obj2["Media"].asDouble)}/5 (${obj2["Num_recensioni"].asString} recensioni)"
                        }
                    },{
                        val dialog = onCreateDialogFailedConn()
                        dialog.show()
                        dialog.findViewById<TextView>(R.id.errore).text =
                            "Errore connessione al server!"
                    })
                }
            },{
                val dialog = onCreateDialogFailedConn()
                dialog.show()
                dialog.findViewById<TextView>(R.id.errore).text =
                    "Errore connessione al server!"
            })
        }else{
            var dialog = onCreateDialogFailed()
            dialog.show()
            dialog.findViewById<TextView>(R.id.errore).text = "Non sei Connesso ad Internet!"
        }


        binding.inizio.addTextChangedListener {
            if(!binding.time.text.toString().equals("Ore") && !binding.slot.text.toString().equals("Ore") && !binding.fine.text.isNullOrBlank() && !binding.inizio.text.isNullOrBlank()){
                if (FormValidator.checkOra(binding.time) && FormValidator.checkOra(binding.slot) && FormValidator.validateDataPrenIn(binding.inizio,binding.fine) && FormValidator.validateDataPrenEND(binding.fine,binding.inizio)){
                    if(binding.inizio.text.toString().equals(binding.fine.text.toString())){
                        var time1 = SimpleDateFormat("HH:mm").parse(binding.time.text.toString())
                        var time2 = SimpleDateFormat("HH:mm").parse(binding.slot.text.toString())
                        if(time1.compareTo(time2) == -1){
                            Log.e("TAG", calcoloprezzo_tot(intent.getStringExtra("prezzoh").toString().toFloat(), binding.inizio.text.toString()+ " 00:00:00",binding.fine.text.toString()+ " 00:00:00").toString())
                            binding.prezzoeuro.text = calcoloprezzo_tot(intent.getStringExtra("prezzoh").toString().toFloat(), binding.inizio.text.toString() + " ${binding.time.text.toString()}:00",binding.fine.text.toString() + " ${binding.slot.text.toString()}:00") .toString()+" €"
                        }else{
                            binding.prezzoeuro.text = "0.0€"
                            binding.time.error="Orari non validi"
                        }
                    }else{
                        Log.e("TAG", calcoloprezzo_tot(intent.getStringExtra("prezzoh").toString().toFloat(), binding.inizio.text.toString()+ " 00:00:00",binding.fine.text.toString()+ " 00:00:00").toString())
                        binding.prezzoeuro.text = calcoloprezzo_tot(intent.getStringExtra("prezzoh").toString().toFloat(), binding.inizio.text.toString() + " ${binding.time.text.toString()}:00",binding.fine.text.toString() + " ${binding.slot.text.toString()}:00") .toString()+" €"
                    }
                   }
            }
        }

        binding.slot.setOnClickListener {
            var popup = showMenu(
                it,
                R.menu.cards_menu,
                arrayListOf(
                    "00:00",
                    "00:30",
                    "01:00",
                    "01:30",
                    "02:00",
                    "02:30",
                    "03:00",
                    "03:30",
                    "04:00",
                    "04:30",
                    "05:00",
                    "05:30",
                    "06:00",
                    "06:30",
                    "07:00",
                    "07:30",
                    "08:00",
                    "08:30",
                    "09:00",
                    "09:30",
                    "10:00",
                    "10:30",
                    "11:00",
                    "11:30",
                    "12:00",
                    "12:30",
                    "13:00",
                    "13:30",
                    "14:00",
                    "14:30",
                    "15:00",
                    "15:30",
                    "16:00",
                    "16:30",
                    "17:00",
                    "17:30",
                    "18:00",
                    "18:30",
                    "19:00",
                    "19:30",
                    "20:00",
                    "20:30",
                    "21:00",
                    "21:30",
                    "22:00",
                    "22:30",
                    "23:00",
                    "23:30"
                )
            )
            popup.setOnMenuItemClickListener {
                binding.slot.setText(it.title.toString())

                if (!binding.time.text.toString().equals("Ore") && !binding.slot.text.toString()
                        .equals("Ore") && !binding.fine.text.isNullOrBlank() && !binding.inizio.text.isNullOrBlank()
                ) {
                    if (FormValidator.checkOra(binding.time) && FormValidator.checkOra(binding.slot) && FormValidator.validateDataPrenIn(
                            binding.inizio,
                            binding.fine
                        ) && FormValidator.validateDataPrenEND(binding.fine, binding.inizio)
                    ) {
                        if (binding.inizio.text.toString().equals(binding.fine.text.toString())) {
                            var time1 =
                                SimpleDateFormat("HH:mm").parse(binding.time.text.toString())
                            var time2 =
                                SimpleDateFormat("HH:mm").parse(binding.slot.text.toString())
                            if (time1.compareTo(time2) == -1) {
                                Log.e(
                                    "TAG",
                                    calcoloprezzo_tot(
                                        intent.getStringExtra("prezzoh").toString().toFloat(),
                                        binding.inizio.text.toString() + " 00:00:00",
                                        binding.fine.text.toString() + " 00:00:00"
                                    ).toString()
                                )
                                binding.prezzoeuro.text = calcoloprezzo_tot(
                                    intent.getStringExtra("prezzoh").toString().toFloat(),
                                    binding.inizio.text.toString() + " ${binding.time.text.toString()}:00",
                                    binding.fine.text.toString() + " ${binding.slot.text.toString()}:00"
                                ).toString() + " €"
                            } else {
                                binding.prezzoeuro.text = "0.0€"
                                binding.time.error = "Orari non validi"

                            }
                        } else {
                            Log.e(
                                "TAG",
                                calcoloprezzo_tot(
                                    intent.getStringExtra("prezzoh").toString().toFloat(),
                                    binding.inizio.text.toString() + " 00:00:00",
                                    binding.fine.text.toString() + " 00:00:00"
                                ).toString()
                            )
                            binding.prezzoeuro.text = calcoloprezzo_tot(
                                intent.getStringExtra("prezzoh").toString().toFloat(),
                                binding.inizio.text.toString() + " ${binding.time.text.toString()}:00",
                                binding.fine.text.toString() + " ${binding.slot.text.toString()}:00"
                            ).toString() + " €"
                        }
                    }
                }

                true
            }
            popup.show()
            /* Log.e("TAG", calcoloprezzo_tot(
                intent.getStringExtra("prezzoh").toString().toFloat(),
                binding.inizio.text.toString()+ " 00:00:00",binding.fine.text.toString()+ " 00:00:00").toString())
            binding.prezzoeuro.text = calcoloprezzo_tot(intent.getStringExtra("prezzoh").toString().toFloat(), binding.inizio.text.toString() + " 00:00:00",binding.fine.text.toString() + " 00:00:00") .toString()*/
        }
        binding.time.setOnClickListener {
            var popup = showMenu(it,R.menu.cards_menu, arrayListOf("00:00","00:30","01:00","01:30","02:00","02:30","03:00","03:30","04:00","04:30","05:00","05:30","06:00","06:30","07:00","07:30","08:00","08:30","09:00","09:30","10:00","10:30","11:00","11:30","12:00","12:30","13:00","13:30","14:00","14:30","15:00","15:30","16:00","16:30","17:00","17:30","18:00","18:30","19:00","19:30","20:00","20:30","21:00","21:30","22:00","22:30","23:00","23:30"))
            popup.setOnMenuItemClickListener {
                binding.time.setText(it.title.toString())
                if(!binding.time.text.toString().equals("Ore") && !binding.slot.text.toString().equals("Ore") && !binding.fine.text.isNullOrBlank() && !binding.inizio.text.isNullOrBlank() ){
                    if (FormValidator.checkOra(binding.time) && FormValidator.checkOra(binding.slot) && FormValidator.validateDataPrenIn(binding.inizio,binding.fine) && FormValidator.validateDataPrenEND(binding.fine,binding.inizio)){
                        if(binding.inizio.text.toString().equals(binding.fine.text.toString())){
                            var time1 = SimpleDateFormat("HH:mm").parse(binding.time.text.toString())
                            var time2 = SimpleDateFormat("HH:mm").parse(binding.slot.text.toString())
                            if(time1.compareTo(time2) == -1){
                                Log.e("TAG", calcoloprezzo_tot(intent.getStringExtra("prezzoh").toString().toFloat(), binding.inizio.text.toString()+ " 00:00:00",binding.fine.text.toString()+ " 00:00:00").toString())
                                binding.prezzoeuro.text = calcoloprezzo_tot(intent.getStringExtra("prezzoh").toString().toFloat(), binding.inizio.text.toString() + " ${binding.time.text.toString()}:00",binding.fine.text.toString() + " ${binding.slot.text.toString()}:00") .toString()+" €"
                            }else{
                                binding.prezzoeuro.text = "0.0€"
                                binding.time.error="Orari non validi"
                            }
                        }else{
                            Log.e("TAG", calcoloprezzo_tot(intent.getStringExtra("prezzoh").toString().toFloat(), binding.inizio.text.toString()+ " 00:00:00",binding.fine.text.toString()+ " 00:00:00").toString())
                            binding.prezzoeuro.text = calcoloprezzo_tot(intent.getStringExtra("prezzoh").toString().toFloat(), binding.inizio.text.toString() + " ${binding.time.text.toString()}:00",binding.fine.text.toString() + " ${binding.slot.text.toString()}:00") .toString()+" €"
                        }
                    }
                }
                true
            }
            popup.show()
        }
        binding.fine.addTextChangedListener {
            if(!binding.time.text.toString().equals("Ore") && !binding.slot.text.toString().equals("Ore") && !binding.fine.text.isNullOrBlank() && !binding.inizio.text.isNullOrBlank()){
                if (FormValidator.checkOra(binding.time) && FormValidator.checkOra(binding.slot) && FormValidator.validateDataPrenIn(binding.inizio,binding.fine) && FormValidator.validateDataPrenEND(binding.fine,binding.inizio)){
                    if(binding.inizio.text.toString().equals(binding.fine.text.toString())){
                        var time1 = SimpleDateFormat("HH:mm").parse(binding.time.text.toString())
                        var time2 = SimpleDateFormat("HH:mm").parse(binding.slot.text.toString())
                        if(time1.compareTo(time2) == -1){
                            Log.e("TAG", calcoloprezzo_tot(intent.getStringExtra("prezzoh").toString().toFloat(), binding.inizio.text.toString()+ " 00:00:00",binding.fine.text.toString()+ " 00:00:00").toString())
                            binding.prezzoeuro.text = calcoloprezzo_tot(intent.getStringExtra("prezzoh").toString().toFloat(), binding.inizio.text.toString() + " ${binding.time.text.toString()}:00",binding.fine.text.toString() + " ${binding.slot.text.toString()}:00") .toString()+" €"
                        }else{
                            binding.prezzoeuro.text = "0.0€"
                            binding.time.error="Orari non validi"
                        }
                    }else{
                        Log.e("TAG", calcoloprezzo_tot(intent.getStringExtra("prezzoh").toString().toFloat(), binding.inizio.text.toString()+ " 00:00:00",binding.fine.text.toString()+ " 00:00:00").toString())
                        binding.prezzoeuro.text = calcoloprezzo_tot(intent.getStringExtra("prezzoh").toString().toFloat(), binding.inizio.text.toString() + " ${binding.time.text.toString()}:00",binding.fine.text.toString() + " ${binding.slot.text.toString()}:00") .toString()+" €"
                    }
                }
            }
        }
        binding.confermaButton.setOnClickListener {
            if(FormValidator.checkOra(binding.time) && FormValidator.checkOra(binding.slot) && FormValidator.validateDataPrenIn(binding.inizio,binding.fine) && FormValidator.validateDataPrenEND(binding.fine,binding.inizio) ){
                if(binding.inizio.text.toString().equals(binding.fine.text.toString())){
                    var time1 = SimpleDateFormat("HH:mm").parse(binding.time.text.toString())
                    var time2 = SimpleDateFormat("HH:mm").parse(binding.slot.text.toString())
                    if(time1.compareTo(time2) == -1){
                        val dati:ArrayList<String> =   binding.inizio.text.toString().split("/") as ArrayList<String>
                        Log.e("TAG",binding.time.toString() + " " + binding.inizio.toString() + " " + binding.slot.toString() + " " + binding.fine.toString())
                        val data = dati.get(2) + "-" + dati.get(1) + "-" + dati.get(0)
                        val dati2:ArrayList<String> =   binding.fine.text.toString().toString().split("/") as ArrayList<String>
                        val data2 = dati2.get(2) + "-" + dati2.get(1) + "-" + dati2.get(0)
                        val data_in_check = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(data + " " + binding.time.text.toString() + ":00")
                        val data_end_check = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(data2 + " " + binding.slot.text.toString() + ":00")
                        var flag = true
                        for(i in 0 until AskiMap.pren_future.size){
                            val data_pren_1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(AskiMap.pren_future[i].data_prenotazione_inizio!!.replace("T"," "))
                            val data_pren_2 =  SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(AskiMap.pren_future[i].data_prenotazione_fine!!.replace("T"," "))
                            if((data_pren_1.after(data_in_check) && data_pren_1.before(data_end_check)) || (data_pren_2.after(data_in_check) && data_pren_2.after(data_end_check))){
                                flag = false
                                break
                            }
                        }
                        if(flag){
                        if(ClientNetwork.isOnline(this)){
                            if(sharedpreferences.getString("pref_card","default").equals("default") && (DBMSManager.utente!!.credito!!.toFloat() <= binding.prezzoeuro.text.toString().split(" ").get(0).toFloat())){
                                var dialog = onCreateDialogFailed()
                                dialog.show()
                                dialog.findViewById<TextView>(R.id.errore).text = "Non hai abbastanza Credito nel Conto Aski!"
                            }
                            else if(!(sharedpreferences.getString("pref_card", "default") == "default") &&
                                !(binding.cvv.text.toString().contains("^[0-9]{3,4}$".toRegex()))) {
                                var dialog = onCreateDialogFailed()
                                dialog.show()
                                dialog.findViewById<TextView>(R.id.errore).text = "CVV non conforme!"
                            }
                            else{
                                DBMSManager.querySelect("SELECT * FROM Prenotazioni WHERE ((Data_Prenotazione_Inizio BETWEEN '$data ${binding.time.text.toString()}:00' AND '$data2 ${binding.slot.text.toString()}:00') OR (Data_Prenotazione_Fine BETWEEN '$data ${binding.time.text.toString()}:00' AND '$data2 ${binding.slot.text.toString()}:00')) AND Targa_Veicolo = '${car!!.targa}'",
                                    { result ->
                                        runOnUiThread {
                                            if (result.size() <= 0) {
                                                if(!(sharedpreferences.getString("pref_card","default").equals("default"))){
                                                    DBMSManager.querySelect("SELECT * FROM Carte WHERE ID_Utente = ${DBMSManager.utente?.id} AND Codice_Carta ='${sharedpreferences.getString("pref_card","default").toString()}' AND CVC = ${binding.cvv.text.toString()}",{
                                                        runOnUiThread {
                                                            if(it.size() > 0){
                                                                DBMSManager.queryInsert("INSERT INTO Prenotazioni VALUES (${DBMSManager.utente?.id},'${car!!.targa}','$data ${binding.time.text.toString()}:00','$data2 ${binding.slot.text.toString()}:00')",
                                                                    { it ->
                                                                        runOnUiThread {
                                                                            DBMSManager.querySelect("SELECT Token FROM Notifiche WHERE ID = ${car.id}",{
                                                                                val obj = (it[0] as JsonObject)["Token"].asString //token
                                                                                val toast = Toast.makeText(
                                                                                    this,
                                                                                    "Prenotazione del veicolo effettuata!",
                                                                                    Toast.LENGTH_LONG
                                                                                )
                                                                                val calendar: Calendar =
                                                                                    Calendar.getInstance()
                                                                                Log.e(
                                                                                    "PROVA",
                                                                                    binding.inizio.text.toString()
                                                                                )
                                                                                val fcmService = FCMService.create()
                                                                                val token = obj
                                                                                val title = "Aski"
                                                                                val message = "La tua ${car.brand}-${car.modello} è stata prenotata da ${data + " " + binding.time.text.toString()} a ${data2 + " " + binding.slot.text.toString()}"
                                                                                val notification =
                                                                                    FCMNotification(
                                                                                        to = token,
                                                                                        notification = FCMNotificationData(
                                                                                            title = title,
                                                                                            body = message
                                                                                        ),
                                                                                        data = emptyMap() // Dati aggiuntivi opzionali
                                                                                    )
                                                                                NotificationReceiver.setNotification("AskiMap","Prenotazione",R.drawable.car_icon)

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

                                                                                AskiMap.pren_future.add(
                                                                                    Prenotazione(
                                                                                        DBMSManager.utente!!.id,
                                                                                        car!!.targa,
                                                                                        data + " " + binding.time.text.toString() + ":00",
                                                                                        data2 + " " + binding.slot.text.toString() + ":00"
                                                                                    )
                                                                                )
                                                                                toast.show()
                                                                                finish()
                                                                            },{
                                                                                val dialog = onCreateDialogFailedConn()
                                                                                dialog.show()
                                                                                dialog.findViewById<TextView>(R.id.errore).text =
                                                                                    "Errore connessione al server!"
                                                                            })
                                                                        }
                                                                    },
                                                                    {
                                                                        val dialog = onCreateDialogFailedConn()
                                                                        dialog.show()
                                                                        dialog.findViewById<TextView>(R.id.errore).text =
                                                                            "Errore connessione al server!"
                                                                    })
                                                            }else{
                                                                var dialog = onCreateDialogFailed()
                                                                dialog.show()
                                                                dialog.findViewById<TextView>(R.id.errore).text =
                                                                    "CVV Errato!"
                                                            }
                                                        }
                                                    },{
                                                        val dialog = onCreateDialogFailedConn()
                                                        dialog.show()
                                                        dialog.findViewById<TextView>(R.id.errore).text =
                                                            "Errore connessione al server!"
                                                    })
                                                }else{
                                                    DBMSManager.queryInsert("INSERT INTO Prenotazioni VALUES (${DBMSManager.utente?.id},'${car!!.targa}','$data ${binding.time.text.toString()}:00','$data2 ${binding.slot.text.toString()}:00')",
                                                        { it ->
                                                            runOnUiThread {
                                                                DBMSManager.querySelect("SELECT Token FROM Notifiche WHERE ID = ${car.id}",{
                                                                    val obj = (it[0] as JsonObject)["Token"].asString //token
                                                                    val toast = Toast.makeText(
                                                                        this,
                                                                        "Prenotazione del veicolo effettuata!",
                                                                        Toast.LENGTH_LONG
                                                                    )

                                                                    val calendar: Calendar =
                                                                        Calendar.getInstance()
                                                                    Log.e(
                                                                        "PROVA",
                                                                        binding.inizio.text.toString()
                                                                    )
                                                                    val fcmService = FCMService.create()
                                                                    val token = obj
                                                                    val title = "Aski"
                                                                    val message = "La tua ${car.brand}-${car.modello} è stata prenotata da ${data + " " + binding.time.text.toString()} a ${data2 + " " + binding.slot.text.toString()}"
                                                                    val notification =
                                                                        FCMNotification(
                                                                            to = token,
                                                                            notification = FCMNotificationData(
                                                                                title = title,
                                                                                body = message
                                                                            ),
                                                                            data = emptyMap() // Dati aggiuntivi opzionali
                                                                        )
                                                                    NotificationReceiver.setNotification("AskiMap","Prenotazione",R.drawable.car_icon)

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

                                                                    AskiMap.pren_future.add(
                                                                        Prenotazione(
                                                                            DBMSManager.utente!!.id,
                                                                            car!!.targa,
                                                                            data + " " + binding.time.text.toString() + ":00",
                                                                            data2 + " " + binding.slot.text.toString() + ":00"
                                                                        )
                                                                    )
                                                                    toast.show()
                                                                    finish()
                                                                },{
                                                                    val dialog = onCreateDialogFailedConn()
                                                                    dialog.show()
                                                                    dialog.findViewById<TextView>(R.id.errore).text =
                                                                        "Errore connessione al server!"
                                                                })
                                                            }
                                                        },
                                                        {
                                                            val dialog = onCreateDialogFailedConn()
                                                            dialog.show()
                                                            dialog.findViewById<TextView>(R.id.errore).text =
                                                                "Errore connessione al server!"
                                                        })}

                                            } else {
                                                var dialog = onCreateDialogFailed()
                                                dialog.show()
                                                dialog.findViewById<TextView>(R.id.errore).text =
                                                    "La macchina è già stata prenotata per questo Periodo!"
                                                binding.fine.text!!.clear()
                                                binding.inizio.text!!.clear()
                                                binding.slot.setText("Ore")
                                                binding.time.setText("Ore")
                                            }
                                        }
                                    },
                                    {
                                        val dialog = onCreateDialogFailedConn()
                                        dialog.show()
                                        dialog.findViewById<TextView>(R.id.errore).text =
                                            "Errore connessione al server!"
                                    })
                            }
                        }
                        else{
                            var dialog = onCreateDialogFailed()
                            dialog.show()
                            dialog.findViewById<TextView>(R.id.errore).text = "Non sei Connesso ad Internet!"
                        }}else{
                            var dialog = onCreateDialogFailed()
                            dialog.show()
                            dialog.findViewById<TextView>(R.id.errore).text = "Hai già prenotato per questo periodo!"
                        }
                    }else{
                        binding.prezzoeuro.text = "0.0€"
                        binding.time.error="Orari non validi"
                    }
                }else{
                    val dati:ArrayList<String> =   binding.inizio.text.toString().split("/") as ArrayList<String>
                    Log.e("TAG",binding.time.toString() + " " + binding.inizio.toString() + " " + binding.slot.toString() + " " + binding.fine.toString())
                    val data = dati.get(2) + "-" + dati.get(1) + "-" + dati.get(0)
                    val dati2:ArrayList<String> =   binding.fine.text.toString().toString().split("/") as ArrayList<String>
                    val data2 = dati2.get(2) + "-" + dati2.get(1) + "-" + dati2.get(0)
                    val data_in_check = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(data + " " + binding.time.text.toString() + ":00")
                    val data_end_check = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(data2 + " " + binding.slot.text.toString() + ":00")
                    var flag = true
                    for(i in 0 until AskiMap.pren_future.size){
                        val data_pren_1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(AskiMap.pren_future[i].data_prenotazione_inizio!!.replace("T"," "))
                        val data_pren_2 =  SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(AskiMap.pren_future[i].data_prenotazione_fine!!.replace("T"," "))
                        if((data_pren_1.after(data_in_check) && data_pren_1.before(data_end_check)) || (data_pren_2.after(data_in_check) && data_pren_2.after(data_end_check))){
                            flag = false
                            break
                        }
                    }
                    if(flag){
                    if(ClientNetwork.isOnline(this)){
                        if(sharedpreferences.getString("pref_card","default").equals("default") && (DBMSManager.utente!!.credito!!.toFloat() <= binding.prezzoeuro.text.toString().split(" ").get(0).toFloat())){
                            var dialog = onCreateDialogFailed()
                            dialog.show()
                            dialog.findViewById<TextView>(R.id.errore).text = "Non hai abbastanza Credito nel Conto Aski!"
                        }
                        else if(!(sharedpreferences.getString("pref_card", "default") == "default") && !(binding.cvv.text.toString().contains("^[0-9]{3,4}$".toRegex()))) {
                            var dialog = onCreateDialogFailed()
                            dialog.show()
                            dialog.findViewById<TextView>(R.id.errore).text = "CVV non conforme!"
                        }
                        else{
                            DBMSManager.querySelect("SELECT * FROM Prenotazioni WHERE ((Data_Prenotazione_Inizio BETWEEN '$data ${binding.time.text.toString()}:00' AND '$data2 ${binding.slot.text.toString()}:00') OR (Data_Prenotazione_Fine BETWEEN '$data ${binding.time.text.toString()}:00' AND '$data2 ${binding.slot.text.toString()}:00')) AND Targa_Veicolo = '${car!!.targa}'",
                                { result ->
                                    runOnUiThread {
                                        if (result.size() <= 0) {
                                            if(!(sharedpreferences.getString("pref_card","default").equals("default"))){
                                                DBMSManager.querySelect("SELECT * FROM Carte WHERE ID_Utente = ${DBMSManager.utente?.id} AND Codice_Carta ='${sharedpreferences.getString("pref_card","default").toString()}' AND CVC = ${binding.cvv.text.toString()}",{
                                                    runOnUiThread {
                                                        if(it.size() > 0){
                                                            DBMSManager.queryInsert("INSERT INTO Prenotazioni VALUES (${DBMSManager.utente?.id},'${car!!.targa}','$data ${binding.time.text.toString()}:00','$data2 ${binding.slot.text.toString()}:00')",
                                                                { it ->
                                                                    runOnUiThread {
                                                                        DBMSManager.querySelect("SELECT Token FROM Notifiche WHERE ID = ${car.id}",{
                                                                            val obj = (it[0] as JsonObject)["Token"].asString //token
                                                                            val toast = Toast.makeText(
                                                                                this,
                                                                                "Prenotazione del veicolo effettuata!",
                                                                                Toast.LENGTH_LONG
                                                                            )

                                                                            val calendar: Calendar =
                                                                                Calendar.getInstance()

                                                                            val fcmService = FCMService.create()
                                                                            val token = obj
                                                                            val title = "Aski"
                                                                            val message = "La tua ${car.brand}-${car.modello} è stata prenotata da ${data + " " + binding.time.text.toString()} a ${data2 + " " + binding.slot.text.toString()}"
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
                                                                            AskiMap.pren_future.add(
                                                                                Prenotazione(
                                                                                    DBMSManager.utente!!.id,
                                                                                    car!!.targa,
                                                                                    data + " " + binding.time.text.toString() + ":00",
                                                                                    data2 + " " + binding.slot.text.toString() + ":00"
                                                                                )
                                                                            )
                                                                            toast.show()
                                                                            finish()
                                                                        },{
                                                                            val dialog = onCreateDialogFailedConn()
                                                                            dialog.show()
                                                                            dialog.findViewById<TextView>(R.id.errore).text =
                                                                                "Errore connessione al server!"
                                                                        })
                                                                    }
                                                                },
                                                                {
                                                                    val dialog = onCreateDialogFailedConn()
                                                                    dialog.show()
                                                                    dialog.findViewById<TextView>(R.id.errore).text =
                                                                        "Errore connessione al server!"
                                                                })
                                                        }else{
                                                            var dialog = onCreateDialogFailed()
                                                            dialog.show()
                                                            dialog.findViewById<TextView>(R.id.errore).text =
                                                                "CVV Errato!"
                                                        }
                                                    }
                                                },{
                                                    val dialog = onCreateDialogFailedConn()
                                                    dialog.show()
                                                    dialog.findViewById<TextView>(R.id.errore).text =
                                                        "Errore connessione al server!"
                                                })
                                            }else{
                                                DBMSManager.queryInsert("INSERT INTO Prenotazioni VALUES (${DBMSManager.utente?.id},'${car!!.targa}','$data ${binding.time.text.toString()}:00','$data2 ${binding.slot.text.toString()}:00')",
                                                    { it ->
                                                        runOnUiThread {
                                                            DBMSManager.querySelect("SELECT Token FROM Notifiche WHERE ID = ${car.id}",{
                                                                val obj = (it[0] as JsonObject)["Token"].asString //token
                                                                val toast = Toast.makeText(
                                                                    this,
                                                                    "Prenotazione del veicolo effettuata!",
                                                                    Toast.LENGTH_LONG
                                                                )

                                                                val calendar: Calendar =
                                                                    Calendar.getInstance()

                                                                val fcmService = FCMService.create()
                                                                val token = obj
                                                                val title = "Aski"
                                                                val message = "La tua ${car.brand}-${car.modello} è stata prenotata da ${data + " " + binding.time.text.toString()} a ${data2 + " " + binding.slot.text.toString()}"
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

                                                                AskiMap.pren_future.add(
                                                                    Prenotazione(
                                                                        DBMSManager.utente!!.id,
                                                                        car!!.targa,
                                                                        data + " " + binding.time.text.toString() + ":00",
                                                                        data2 + " " + binding.slot.text.toString() + ":00"
                                                                    )
                                                                )
                                                                toast.show()
                                                                finish()
                                                            },{
                                                                val dialog = onCreateDialogFailedConn()
                                                                dialog.show()
                                                                dialog.findViewById<TextView>(R.id.errore).text =
                                                                    "Errore connessione al server!"
                                                            })
                                                        }
                                                    },
                                                    {
                                                        val dialog = onCreateDialogFailedConn()
                                                        dialog.show()
                                                        dialog.findViewById<TextView>(R.id.errore).text =
                                                            "Errore connessione al server!"
                                                    })}

                                        } else {
                                            var dialog = onCreateDialogFailed()
                                            dialog.show()
                                            dialog.findViewById<TextView>(R.id.errore).text =
                                                "La macchina è già stata prenotata per questo Periodo!"
                                            binding.fine.text!!.clear()
                                            binding.inizio.text!!.clear()
                                            binding.slot.setText("Ore")
                                            binding.time.setText("Ore")
                                        }
                                    }
                                },
                                {
                                    val dialog = onCreateDialogFailedConn()
                                    dialog.show()
                                    dialog.findViewById<TextView>(R.id.errore).text =
                                        "Errore connessione al server!"
                                })
                        }
                    }
                    else{
                        var dialog = onCreateDialogFailed()
                        dialog.show()
                        dialog.findViewById<TextView>(R.id.errore).text = "Non sei Connesso ad Internet!"
                    }
                    }else{
                        var dialog = onCreateDialogFailed()
                        dialog.show()
                        dialog.findViewById<TextView>(R.id.errore).text = "Hai già prenotato per questo periodo!"
                    }
                }

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

    @SuppressLint("RestrictedApi")
    private fun showMenu(v: View, @MenuRes menuRes: Int, orari: ArrayList<String>): PopupMenu {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        if (popup.menu is MenuBuilder) {
            val menuBuilder = popup.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
            var i = 0
            if(orari.size > 0){
                for(orario in orari){
                    menuBuilder.add(i,i,i,orario)
                    i++
                }
            }
        }
        return popup
    }

}