-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
--
-- Host: localhost    Database: chatroom
-- ------------------------------------------------------
-- Server version	8.0.34

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
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `IdComment` int NOT NULL AUTO_INCREMENT,
  `Text` varchar(100) NOT NULL,
  `CreatedAt` datetime NOT NULL,
  `IdForumRoom` int NOT NULL,
  `IdUser` int NOT NULL,
  `IsAllowed` tinyint DEFAULT NULL,
  PRIMARY KEY (`IdComment`),
  KEY `fk_comment_has_user_idx` (`IdUser`),
  KEY `fk_comment_has_room_idx` (`IdForumRoom`),
  CONSTRAINT `fk_comment_has_room` FOREIGN KEY (`IdForumRoom`) REFERENCES `forumroom` (`IdForumRoom`),
  CONSTRAINT `fk_comment_has_user` FOREIGN KEY (`IdUser`) REFERENCES `user` (`IdUser`)
) ENGINE=InnoDB AUTO_INCREMENT=74 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `forumroom`
--

DROP TABLE IF EXISTS `forumroom`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forumroom` (
  `IdForumRoom` int NOT NULL AUTO_INCREMENT,
  `Name` varchar(45) NOT NULL,
  PRIMARY KEY (`IdForumRoom`),
  UNIQUE KEY `Name_UNIQUE` (`Name`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `logger`
--

DROP TABLE IF EXISTS `logger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `logger` (
  `IdLogger` int NOT NULL AUTO_INCREMENT,
  `Type` varchar(45) NOT NULL,
  `Message` varchar(255) NOT NULL,
  PRIMARY KEY (`IdLogger`)
) ENGINE=InnoDB AUTO_INCREMENT=281 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `permission`
--

DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permission` (
  `IdPermission` int NOT NULL AUTO_INCREMENT,
  `IdUser` int NOT NULL,
  `Permission` varchar(45) NOT NULL,
  PRIMARY KEY (`IdPermission`),
  KEY `fk_User_has_Permission_idx` (`IdUser`),
  CONSTRAINT `fk_User_has_Permission` FOREIGN KEY (`IdUser`) REFERENCES `user` (`IdUser`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `IdUser` int NOT NULL AUTO_INCREMENT,
  `IsApproved` tinyint DEFAULT NULL,
  `TwoFactorToken` varchar(255) DEFAULT NULL,
  `Username` varchar(45) NOT NULL,
  `Email` varchar(45) NOT NULL,
  `Password` varchar(255) NOT NULL,
  `Role` varchar(45) DEFAULT NULL,
  `Source` varchar(45) DEFAULT NULL,
  `JWT` text,
  PRIMARY KEY (`IdUser`),
  UNIQUE KEY `Username_UNIQUE` (`Username`),
  UNIQUE KEY `TwoFactorToken_UNIQUE` (`TwoFactorToken`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_has_room`
--

DROP TABLE IF EXISTS `user_has_room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_has_room` (
  `IdUser` int NOT NULL,
  `IdForumRoom` int NOT NULL,
  PRIMARY KEY (`IdUser`,`IdForumRoom`),
  KEY `Room_IdForumRoom_idx` (`IdForumRoom`),
  CONSTRAINT `Room_IdForumRoom` FOREIGN KEY (`IdForumRoom`) REFERENCES `forumroom` (`IdForumRoom`),
  CONSTRAINT `User_IdUser` FOREIGN KEY (`IdUser`) REFERENCES `user` (`IdUser`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-05-26 10:25:48
