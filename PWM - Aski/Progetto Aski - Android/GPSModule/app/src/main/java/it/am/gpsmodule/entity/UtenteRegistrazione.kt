package it.am.gpsmodule.entity

class UtenteRegistrazione(var nome:String?,var cognome:String?,var data_nascita:String?,var indirizzo:String?,var cap:String?,var citta:String?,var telefono:String?,var email:String?,var password:String?) {


    constructor(nome:String?, cognome:String?, data_nascita:String?, indirizzo: String?, cap: String?, citta: String?) :  this(nome,cognome,data_nascita,indirizzo, cap, citta,null,null,null)


    constructor(nome:String?,cognome:String?,data_nascita:String?) : this(nome,cognome,data_nascita,null, null, null)

}