-- MySQL dump 10.13  Distrib 5.5.55, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: master
-- ------------------------------------------------------
-- Server version	5.5.55-0ubuntu0.14.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account` (
  `account_id` int(11) NOT NULL,
  `username` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  `nickname` varchar(45) DEFAULT NULL,
  `first_name` varchar(45) DEFAULT NULL,
  `last_name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`account_id`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_event`
--

DROP TABLE IF EXISTS `cart_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cart_event` (
  `cartEventId` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` varchar(20) NOT NULL,
  `customerId` bigint(20) DEFAULT NULL,
  `amount` int(10) DEFAULT NULL,
  `make_time` bigint(20) DEFAULT NULL,
  `bookId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`cartEventId`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_event`
--

LOCK TABLES `cart_event` WRITE;
/*!40000 ALTER TABLE `cart_event` DISABLE KEYS */;
INSERT INTO `cart_event` VALUES (2,'ADD_ITEM',1,6,1494627848258,2),(3,'ADD_ITEM',1,4,1494627856918,3),(4,'ADD_ITEM',1,1,1494628369871,3),(5,'ADD_ITEM',1,7,1494708340655,3),(6,'ADD_ITEM',1,1,1494708356729,1),(7,'REMOVE_ITEM',1,2,1494778100709,2),(8,'REMOVE_ITEM',1,1,1494778104096,1),(9,'ADD_ITEM',1,1,1494778279181,3),(10,'ADD_ITEM',1,2,1494778289638,3),(11,'ADD_ITEM',1,1,1494778299636,1),(12,'REMOVE_ITEM',1,1,1494778318992,1),(13,'ADD_ITEM',1,4,1494778402498,3),(14,'REMOVE_ITEM',1,9,1494780810133,3),(18,'ADD_ITEM',1,4,1494796467588,5),(19,'CHECKOUT',1,0,1494796532081,0),(20,'ADD_ITEM',1,2,1494876715463,2),(21,'ADD_ITEM',1,9,1494876722149,1),(22,'ADD_ITEM',1,2,1494878512044,5),(23,'ADD_ITEM',1,2,1494878538328,5),(24,'REMOVE_ITEM',1,6,1494878767727,1),(25,'ADD_ITEM',1,1,1494878774696,2),(26,'ADD_ITEM',1,2,1494878781326,4);
/*!40000 ALTER TABLE `cart_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review`
--

DROP TABLE IF EXISTS `review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `review` (
  `reviewId` bigint(20) NOT NULL AUTO_INCREMENT,
  `bookId` bigint(20) NOT NULL,
  `userId` bigint(20) DEFAULT NULL,
  `rate` int(11) DEFAULT NULL,
  `comment` text,
  PRIMARY KEY (`reviewId`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review`
--

LOCK TABLES `review` WRITE;
/*!40000 ALTER TABLE `review` DISABLE KEYS */;
INSERT INTO `review` VALUES (1,1,1,5,'Comment for rate 5'),(2,1,2,8,'Comment for rate 8'),(3,1,3,9,'Comment for rate 9');
/*!40000 ALTER TABLE `review` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-08-06 10:49:24
