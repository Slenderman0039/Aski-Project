package it.am.gpsmodule.utils

import android.util.Log
import androidx.core.text.isDigitsOnly
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.Year
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit

class FormValidator {
companion object {
    fun validateFormAndDigitAndSpecialChar(form: TextInputEditText): Boolean {
        if (form.text.toString().isBlank() || form.text.toString()
                .isNullOrEmpty() || form.text.toString().isDigitsOnly() || form.text.toString().contains(" ".toRegex())
        ) {
            form.setError("Errore: Campo non valido!")
            return false //RITORNA FALSE SE IL CAMPO HA DEI PROBLEMI
        }
        return true //RITORNA TRUE SE IL CAMPO VA BENE
    }

    fun validateFormNameandSurname(form: TextInputEditText): Boolean {
        if (form.text.toString().isBlank() || form.text.toString()
                .isNullOrEmpty() || form.text.toString().isDigitsOnly() || form.text.toString().contains(" ".toRegex()) || !form.text.toString().contains("^(?=.{1,40}\$)[a-zA-Z]+(?:[-'\\s][a-zA-Z]+)*\$".toRegex())
        ) {
            form.setError("Errore: Campo non valido!")
            return false //RITORNA FALSE SE IL CAMPO HA DEI PROBLEMI
        }
        return true //RITORNA TRUE SE IL CAMPO VA BENE
    }
    fun validateFormCity(form: TextInputEditText): Boolean {
        if (form.text.toString().isBlank() || form.text.toString()
                .isNullOrEmpty() || form.text.toString().isDigitsOnly() || form.text.toString().contains(" ".toRegex()) || !form.text.toString().contains("^(?=.{1,58}\$)[a-zA-Z]+(?:[-'\\s][a-zA-Z]+)*\$".toRegex())
        ) {
            form.setError("Errore: Campo non valido!")
            return false //RITORNA FALSE SE IL CAMPO HA DEI PROBLEMI
        }
        return true //RITORNA TRUE SE IL CAMPO VA BENE
    }



    fun validateFormAndSpecialChar(form: TextInputEditText): Boolean {
        if (form.text.toString().isBlank() || form.text.toString()
                .isNullOrEmpty() || form.text.toString().isDigitsOnly() || !form.text.toString().contains("([Vv]ia|[Vv]ia|[Cc]orso|[Vv]iale|iazza) ([a-zA-Z ]+?),[0-9]{1,5}([a-z]?)\$".toRegex())
        ) {
            form.setError("Errore: Campo non valido!")
            return false //RITORNA FALSE SE IL CAMPO HA DEI PROBLEMI
        }
        return true //RITORNA TRUE SE IL CAMPO VA BENE
    }

    fun validateFormDate(form: TextInputEditText): Boolean {
        if (!form.text.toString().contains("(^(((0[1-9]|1[0-9]|2[0-8])[\\/](0[1-9]|1[012]))|((29|30|31)[\\/](0[13578]|1[02]))|((29|30)[\\/](0[4,6,9]|11)))[\\/](19|[2-9][0-9])\\d\\d\$)|(^29[\\/]02[\\/](19|[2-9][0-9])(00|04|08|12|16|20|24|28|32|36|40|44|48|52|56|60|64|68|72|76|80|84|88|92|96)\$)\n".toRegex()) || form.text.isNullOrEmpty() || form.text!!.isBlank()){
            form.setError("Errore: Campo non valido!\nGG/MM/AAAA")
            return false //RITORNA FALSE SE IL CAMPO HA DEI PROBLEMI
        }else if(calcolaDistanzaAnni(form.text.toString()) < 18){
            form.setError("Errore: Non hai compiuto 18 anni!")
            return false
        }
        return true //RITORNA TRUE SE IL CAMPO VA BENE
    }
    fun validateFormCap(form: TextInputEditText): Boolean {
        if (form.text.toString().isBlank() || form.text.toString().isNullOrEmpty() || !form.text.toString().contains("^[0-9]{5}\$".toRegex()) || form.text.toString().length!=5) {
            form.setError("Errore: Campo non valido!")
            return false //RITORNA FALSE SE IL CAMPO HA DEI PROBLEMI
        }
        return true //RITORNA TRUE SE IL CAMPO VA BENE
    }
    fun validateFormPhone(form: TextInputEditText): Boolean {
        if (form.text.toString().isBlank() || form.text.toString().isNullOrEmpty() || form.text.toString().contains("[!\"#$%&'()*+,-./:;\\\\<=>?@\\[\\]^_`{|}~]".toRegex()) || form.text.toString().length!=10) {
            form.setError("Errore: Campo non valido!")
            return false //RITORNA FALSE SE IL CAMPO HA DEI PROBLEMI
        }
        return true //RITORNA TRUE SE IL CAMPO VA BENE
    }
    fun validateFormEmail(form: TextInputEditText): Boolean {
        Log.e("DEBUG",form.text.toString())
        if (form.text.toString().isBlank() || form.text.toString().isNullOrEmpty() || !form.text.toString().contains("@") || !form.text.toString().contains("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$".toRegex())  || (!form.text.toString().contains(".it") && !form.text.toString().contains(".com")&& !form.text.toString().contains(".org"))) {
            form.setError("Errore: Campo non valido!")
            return false //RITORNA FALSE SE IL CAMPO HA DEI PROBLEMI
        }
        return true //RITORNA TRUE SE IL CAMPO VA BENE
    }
    fun validateFormPassword(form: TextInputEditText): Boolean {
        if (form.text.toString().isBlank() || form.text.toString().isNullOrEmpty() || form.text.toString().length<=7) {
            form.setError("Errore: Campo non valido!\nMin. 8 caratteri")
            return false //RITORNA FALSE SE IL CAMPO HA DEI PROBLEMI
        }
        return true //RITORNA TRUE SE IL CAMPO VA BENE
    }
    fun validateForm(form: TextInputEditText): Boolean {
        if (form.text.toString().isBlank() || form.text.toString().isNullOrEmpty()) {
            form.setError("Errore: Campo non valido!")
            return false //RITORNA FALSE SE IL CAMPO HA DEI PROBLEMI
        }
        return true //RITORNA TRUE SE IL CAMPO VA BENE
    }

    fun validateFormTarga(form: TextInputEditText): Boolean {
        if (form.text.toString().isBlank() || form.text.toString().isNullOrEmpty() || form.text.toString().isDigitsOnly()|| !form.text?.subSequence(2,4)?.contains("[0-9]".toRegex())!! || !form.text?.subSequence(0,1)?.contains("[A-Za-z]".toRegex())!! || !form.text?.subSequence(5,6)?.contains("[A-Za-z]".toRegex())!!  ) {
            form.setError("Errore: Campo non valido!")
            return false //RITORNA FALSE SE IL CAMPO HA DEI PROBLEMI
        }
        return true //RITORNA TRUE SE IL CAMPO VA BENE
    }

    fun validateFormAnno(form: TextInputEditText): Boolean {
        if (form.text.toString().isBlank() || form.text.toString().isNullOrEmpty() || !form.text.toString().isDigitsOnly()|| form.text.toString().length>4|| form.text.toString().length<=0|| form.text.toString().toInt() < 1980 || form.text.toString().toInt() > Calendar.getInstance().get(Calendar.YEAR).toInt()) {
            form.setError("Errore: Campo non valido!")
            return false //RITORNA FALSE SE IL CAMPO HA DEI PROBLEMI
        }
        return true //RITORNA TRUE SE IL CAMPO VA BENE
    }
    fun validateFormCard(form: TextInputEditText): Boolean {
        if (form.text.toString().isBlank() || form.text.toString().isNullOrEmpty() || !form.text.toString().isDigitsOnly()|| form.text.toString().length<16) {
            form.setError("Errore: CARTA NON VALIDA!")
            return false //RITORNA FALSE SE IL CAMPO HA DEI PROBLEMI
        }
        return true //RITORNA TRUE SE IL CAMPO VA BENE
    }
    fun validateFormCVV(form: TextInputEditText): Boolean {
        if (form.text.toString().isBlank() || form.text.toString().isNullOrEmpty() || !form.text.toString().isDigitsOnly()|| form.text.toString().length<3) {
            form.setError("Errore: CVV NON VALIDO!")
            return false //RITORNA FALSE SE IL CAMPO HA DEI PROBLEMI
        }
        return true //RITORNA TRUE SE IL CAMPO VA BENE
    }
    fun validateFormScadenzaCard(form: TextInputEditText): Boolean {
        try{
        val str = form.text?.split("/")
        val date = str?.get(0) + "/" + "20"+ str?.get(1)
        Log.e("TAG",date.toString())
        if(form.text.toString().contains("/") && form.text.toString().count{ it == '/' } == 1){
        if (!form.text.toString().isBlank() && !form.text.toString().isNullOrEmpty() && !form.text.toString().isDigitsOnly() && form.text.toString().length==5 && date.takeLast(4).toInt() >= Calendar.getInstance().get(Calendar.YEAR).toInt()) {
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            Log.e("Eccezione", "SONO QUI")
            var month = Calendar.getInstance().get(Calendar.MONTH) + 1
            var current: String = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                .toString() + "/" + month.toString() + "/" + Calendar.getInstance()
                .get(Calendar.YEAR).toString()
            val firstDate: Date = sdf.parse(current)
            val dati: ArrayList<String> = date.split("/") as ArrayList<String>
            if (dati.get(0).toString().toInt() > 0 && dati.get(0).toString().toInt() <= 12) {
                Log.e("Eccezione", "SONO QUI1")

                var secondDate: Date? = null
                try {

                    secondDate = sdf.parse("01/" + date)
                } catch (e: Exception) {
                    Log.e("Eccezione", "ERRORE")
                    Log.e("Eccezione", "SONO QUI2")
                    form.setError("Errore: CARTA NON VALIDA!")
                    return false
                }
                Log.e("DATA FORM", secondDate.toString())
                Log.e("DATA CORRENTE", firstDate.toString())
                Log.e("COMPARE TO", secondDate?.compareTo(firstDate)!!.toString())
                if (secondDate != null) {
                    if (secondDate?.compareTo(firstDate)!! < 0) {
                        Log.e("Eccezione", "SONO QUI3")

                        form.setError("Errore: CARTA SCADUTA O VUOTA!")
                        return false //RITORNA FALSE SE IL CAMPO HA DEI PROBLEMI
                    } else {
                        return true //RITORNA TRUE SE IL CAMPO VA BENE
                    }
                }
                return false
            } else {
                Log.e("Eccezione", "SONO QUI4")


                form.setError("Errore: CARTA NON VALIDA!")
                return false
            }
        }
        }
        }catch (e:Exception){
            form.setError("Errore: Scadenza non valida!")
            return false
        }
        Log.e("Eccezione", "SONO QUI5")

        form.setError("Errore: CARTA NON VALIDA!")
        return false
    }
    fun validateDataPrenIn(form:TextInputEditText,form2: TextInputEditText):Boolean{
        try {
            if (!form.text.toString()
                    .contains("(^(((0[1-9]|1[0-9]|2[0-8])[\\/](0[1-9]|1[012]))|((29|30|31)[\\/](0[13578]|1[02]))|((29|30)[\\/](0[4,6,9]|11)))[\\/](19|[2-9][0-9])\\d\\d\$)|(^29[\\/]02[\\/](19|[2-9][0-9])(00|04|08|12|16|20|24|28|32|36|40|44|48|52|56|60|64|68|72|76|80|84|88|92|96)\$)\n".toRegex()) || form.text.isNullOrEmpty() || form.text!!.isBlank()
            ) {
                form.setError("Errore: Campo non valido!\nGG/MM/AAAA")
                return false //RITORNA FALSE SE IL CAMPO HA DEI PROBLEMI
            }  else if (SimpleDateFormat("dd/MM/yyyy").parse(form2.text.toString())
                    .before(SimpleDateFormat("dd/MM/yyyy").parse(form.text.toString()))
            ) {
                form.setError("Errore: non puoi scambiare le date!")
                return false
            }else if(calcolaNumeroGiorni(Date(),SimpleDateFormat("dd/MM/yyyy").parse(form.text.toString())) <= 2){
                form.setError("Errore: Minimo 2 giorni di anticipo!")
                return false
            }
            else if (Date().after(SimpleDateFormat("dd/MM/yyyy").parse(form.text.toString()))){
                form.setError("Errore: Hai scelto una data prima di quella di oggi!")
                return false
            }
        }catch (e:Exception){
            return false
        }
        return true
    }
    fun validateDataPrenEND(form:TextInputEditText,form2: TextInputEditText):Boolean{
        try {
            if (!form.text.toString()
                    .contains("(^(((0[1-9]|1[0-9]|2[0-8])[\\/](0[1-9]|1[012]))|((29|30|31)[\\/](0[13578]|1[02]))|((29|30)[\\/](0[4,6,9]|11)))[\\/](19|[2-9][0-9])\\d\\d\$)|(^29[\\/]02[\\/](19|[2-9][0-9])(00|04|08|12|16|20|24|28|32|36|40|44|48|52|56|60|64|68|72|76|80|84|88|92|96)\$)\n".toRegex()) || form.text.isNullOrEmpty() || form.text!!.isBlank()
            ) {
                form.setError("Errore: Campo non valido!\nGG/MM/AAAA")
                return false //RITORNA FALSE SE IL CAMPO HA DEI PROBLEMI
            } else if (SimpleDateFormat("dd/MM/yyyy").parse(form2.text.toString())
                    .after(SimpleDateFormat("dd/MM/yyyy").parse(form.text.toString()))
            ) {
                form.setError("Errore: non puoi scambiare le date!")
                return false
            } else if (calcolaNumeroGiorni(
                    Date(),
                    SimpleDateFormat("dd/MM/yyyy").parse(form.text.toString())
                ) <= 2
            ) {
                form.setError("Errore: Minimo 2 giorni di anticipo!")
                return false
            } else if (Date().after(SimpleDateFormat("dd/MM/yyyy").parse(form.text.toString()))) {
                form.setError("Errore: Hai scelto una data prima di quella di oggi!")
                return false
            }
        } catch(e: Exception){
            return false
        }
        return true
    }
    fun checkOra(form:TextInputEditText):Boolean{
        if(form.text.isNullOrEmpty()){
             form.setError("Errore: Campo vuoto o non valido!")
            return false
        }
        return true
    }

    fun checkSameDateOra(inizio:TextInputEditText, fine:TextInputEditText ,orainizio:TextInputEditText, orafine:TextInputEditText):Boolean{
        Log.e("COMPARE",fine.text.toString()+"|"+inizio.text.toString()+"|"+fine.text.toString()+"|"+orainizio.text.toString()+"|"+orafine.text.toString())

        if(inizio.text.toString() == fine.text.toString() && inizio.text.toString().isNotBlank() && fine.text.toString().isNotBlank() && !orainizio.text.toString().equals("Ore") && !orafine.text.toString().equals("Ore")){
            var time1 = SimpleDateFormat("HH:mm").parse(orainizio.text.toString())
            var time2 = SimpleDateFormat("HH:mm").parse(orafine.text.toString())
            Log.e("COMPARE",time1.compareTo(time2).toString())
            if(time1.compareTo(time2) == -1){
                return true
            }
            orainizio.setError("Tempo Inizio e Fine non valido!")
            return false
        }
        return false
    }
    fun checkCard(card: String): String {
        val visa = """^4[0-9]{6,}${'$'}""".toRegex()
        val mastercard = """^5[1-5][0-9]{5,}|222[1-9][0-9]{3,}|22[3-9][0-9]{4,}|2[3-6][0-9]{5,}|27[01][0-9]{4,}|2720[0-9]{3,}${'$'}""".toRegex()
        if (!card.isNullOrEmpty() && card.contains(visa)) {
            return "Visa"
        }else if(!card.isNullOrEmpty() && card.contains(mastercard)){
            return "Mastercard"
        }
        return "card"
    }
    fun calcolaNumeroGiorni(data1: Date, data2: Date): Long {
        val differenzaMillisecondi = data2.time - data1.time
        val giorni = differenzaMillisecondi / 86_400_000

        return Math.round(giorni.toDouble())
    }
    fun calcolaDistanzaAnni(data: String): Long {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dataFornita = dateFormat.parse(data)

        val calDataFornita = Calendar.getInstance()
        calDataFornita.time = dataFornita

        val calDataCorrente = Calendar.getInstance()

        val differenzaMillisecondi = calDataCorrente.timeInMillis - calDataFornita.timeInMillis
        val giorni = TimeUnit.MILLISECONDS.toDays(differenzaMillisecondi)
        val anni = giorni / 365

        return anni
    }
}
}