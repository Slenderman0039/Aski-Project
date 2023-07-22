-- MySQL dump 10.13  Distrib 8.0.28, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: webmobile
-- ------------------------------------------------------
-- Server version	8.0.33-0ubuntu0.22.04.2

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `Carte`
--

DROP TABLE IF EXISTS `Carte`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Carte` (
  `ID_Utente` int NOT NULL,
  `Codice_Carta` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `CVC` int NOT NULL,
  `Anno_Scadenza` date NOT NULL,
  PRIMARY KEY (`ID_Utente`,`Codice_Carta`,`Anno_Scadenza`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Carte`
--

LOCK TABLES `Carte` WRITE;
/*!40000 ALTER TABLE `Carte` DISABLE KEYS */;
INSERT INTO `Carte` VALUES (1,'4444444444444444',444,'2029-08-01');
/*!40000 ALTER TABLE `Carte` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Notifiche`
--

DROP TABLE IF EXISTS `Notifiche`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Notifiche` (
  `ID` int NOT NULL,
  `Token` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Notifiche`
--

LOCK TABLES `Notifiche` WRITE;
/*!40000 ALTER TABLE `Notifiche` DISABLE KEYS */;
INSERT INTO `Notifiche` VALUES (1,'dvwwkR9ZTS-9DpIgH8axu-:APA91bGU4aeTbqzgWNBYqSwVyd0cQTf5wBkQVVH2K_23GxOFdXRHHiCqWBm98Th6ULynZPKjhD6YPr4Lz88xiaJzuVL6MPWlu9MOsDROiAlB8AJSaP_5WW5sY5qmxKz3TmntPuWq6N7f'),(2,'djF6RQueTKqIQ8QVlb_ihF:APA91bGLXR-Q1E_FM70MEQLwKVogxcwOupMTdEcfnvW4n34WaDzs1NhGQm7A_LQA0fjFd25m_uVnkAt_7x7YEBCaOdajJRPVSKPDG-pmK68X_ECPXJWO0UUb-lTbPqXw7G2S-9C3MoUu'),(3,'djF6RQueTKqIQ8QVlb_ihF:APA91bGLXR-Q1E_FM70MEQLwKVogxcwOupMTdEcfnvW4n34WaDzs1NhGQm7A_LQA0fjFd25m_uVnkAt_7x7YEBCaOdajJRPVSKPDG-pmK68X_ECPXJWO0UUb-lTbPqXw7G2S-9C3MoUu');
/*!40000 ALTER TABLE `Notifiche` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Pagamenti`
--

DROP TABLE IF EXISTS `Pagamenti`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Pagamenti` (
  `ID_Utente` int NOT NULL,
  `Metodo_Pagamento` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `Importo` float NOT NULL,
  `Data_Pagamento` datetime NOT NULL,
  PRIMARY KEY (`ID_Utente`,`Data_Pagamento`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Pagamenti`
--

LOCK TABLES `Pagamenti` WRITE;
/*!40000 ALTER TABLE `Pagamenti` DISABLE KEYS */;
/*!40000 ALTER TABLE `Pagamenti` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Prenotazioni`
--

DROP TABLE IF EXISTS `Prenotazioni`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Prenotazioni` (
  `ID_Utente` int NOT NULL,
  `Targa_Veicolo` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `Data_Prenotazione_Inizio` datetime NOT NULL,
  `Data_Prenotazione_Fine` datetime NOT NULL,
  PRIMARY KEY (`ID_Utente`,`Data_Prenotazione_Inizio`,`Data_Prenotazione_Fine`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Prenotazioni`
--

LOCK TABLES `Prenotazioni` WRITE;
/*!40000 ALTER TABLE `Prenotazioni` DISABLE KEYS */;
INSERT INTO `Prenotazioni` VALUES (1,'ES708PX','2023-08-09 15:00:00','2023-08-10 18:00:00');
/*!40000 ALTER TABLE `Prenotazioni` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Utenti`
--

DROP TABLE IF EXISTS `Utenti`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Utenti` (
  `ID_Utente` int NOT NULL AUTO_INCREMENT,
  `Nome` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `Cognome` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `Indirizzo` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `CAP` int NOT NULL,
  `Città` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `Email` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `Num_Telefono` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `Password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `Data_Nascita` date NOT NULL,
  `Credito` float NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID_Utente`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Utenti`
--

LOCK TABLES `Utenti` WRITE;
/*!40000 ALTER TABLE `Utenti` DISABLE KEYS */;
INSERT INTO `Utenti` VALUES (1,'Antonio','Spedito','Via Elia Crisafulli,2',90128,'Palermo','antonio.spedito@community.unipa.it','320333567','Antonietto01','2001-03-04',500),(2,'Alessandro','Macaluso','Corso dei Mille,1060',90123,'Palermo','macaluso01@outlook.it','3203556593','AleMaca','2002-03-02',700),(3,'Federico','Concone','Corso delle Scienze, 5',90128,'Palermo','federico.concone@gmail.com','0000000000','federico.concone','1988-07-06',10000);
/*!40000 ALTER TABLE `Utenti` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Valutazioni`
--

DROP TABLE IF EXISTS `Valutazioni`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Valutazioni` (
  `Targa` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `ID_Utente` int NOT NULL,
  `Data_Valutazione` datetime NOT NULL,
  `Valutazione` float NOT NULL DEFAULT '0',
  PRIMARY KEY (`Targa`,`ID_Utente`,`Data_Valutazione`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Valutazioni`
--

LOCK TABLES `Valutazioni` WRITE;
/*!40000 ALTER TABLE `Valutazioni` DISABLE KEYS */;
/*!40000 ALTER TABLE `Valutazioni` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Veicoli`
--

DROP TABLE IF EXISTS `Veicoli`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `Veicoli` (
  `ID` int NOT NULL AUTO_INCREMENT,
  `Targa` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `Modello` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `Marca` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `Anno_Produzione` year NOT NULL,
  `Carburante` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `Latitudine` double NOT NULL,
  `Longitudine` double NOT NULL,
  `Disponibile` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`,`Targa`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Veicoli`
--

LOCK TABLES `Veicoli` WRITE;
/*!40000 ALTER TABLE `Veicoli` DISABLE KEYS */;
INSERT INTO `Veicoli` VALUES (1,'FB904PA','500','Abarth',2022,'Benzina',38.078916666666665,13.512365,1),(1,'GA702JK','City','Aixam',2023,'Diesel',38.075655,13.443406666666666,1),(2,'BQ903PF','Rio','Kia',2004,'Benzina',38.11569,13.361486666666666,1),(2,'EB274LQ','Duster','Dacia',2015,'Benzina',38.13300666666667,13.35187,1),(2,'ES708PX','Panda','Fiat',2014,'GPL',38.134998333333336,13.348955,1),(3,'FA703PL','116','BMW',2016,'Benzina',38.104243333333336,13.349641666666667,1),(3,'FB832GA','500C','Fiat',2020,'Benzina',38.11942666666667,13.369145,1);
/*!40000 ALTER TABLE `Veicoli` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-07-03  0:08:01
