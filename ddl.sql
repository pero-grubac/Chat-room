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
  `Text` varchar(45) NOT NULL,
  `CreatedAt` datetime NOT NULL,
  `IdForumRoom` int NOT NULL,
  `IdUser` int NOT NULL,
  `IsAllowed` tinyint DEFAULT NULL,
  PRIMARY KEY (`IdComment`),
  KEY `fk_comment_has_user_idx` (`IdUser`),
  KEY `fk_comment_has_room_idx` (`IdForumRoom`),
  CONSTRAINT `fk_comment_has_room` FOREIGN KEY (`IdForumRoom`) REFERENCES `forumroom` (`IdForumRoom`),
  CONSTRAINT `fk_comment_has_user` FOREIGN KEY (`IdUser`) REFERENCES `user` (`IdUser`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
INSERT INTO `comment` VALUES (2,'nauka','2024-03-22 12:30:00',1,1,1),(3,'nauka 2','2024-03-22 12:30:00',1,3,1),(4,'nauka 3','2024-03-22 12:30:00',1,3,0),(5,'nauka 4 izmjena 2','2024-03-24 21:54:30',1,3,1),(6,'nauka 5','2024-03-22 12:30:00',1,3,NULL),(9,'nauka 5ab','2024-03-24 21:53:50',1,3,NULL);
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forumroom`
--

LOCK TABLES `forumroom` WRITE;
/*!40000 ALTER TABLE `forumroom` DISABLE KEYS */;
INSERT INTO `forumroom` VALUES (8,'IT'),(2,'Kultura'),(3,'Muzika'),(1,'Nauka'),(4,'Umjetnost');
/*!40000 ALTER TABLE `forumroom` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permission`
--

LOCK TABLES `permission` WRITE;
/*!40000 ALTER TABLE `permission` DISABLE KEYS */;
INSERT INTO `permission` VALUES (32,1,'ADD'),(33,1,'DELETE'),(34,1,'UPDATE'),(37,4,'ADD'),(38,4,'DELETE'),(39,8,'ADD');
/*!40000 ALTER TABLE `permission` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,1,NULL,'admin','admin@gmail.com','$2a$10$SHBpBtj88odoDNTy3SISieMP12WJwErrsCcM5dpZCbNxF777BxSIu','ROLE_ADMIN',NULL,'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInBlcm1pc3Npb25zIjpbIkFERCIsIkRFTEVURSIsIlVQREFURSJdLCJzdWIiOiJhZG1pbiIsImlhdCI6MTcxMjU2MTQzNCwiZXhwIjoxNzEyNTYyODc0fQ.b3CrQRvqGsnNRnIXHt9boUf8mqAE77P_ghrqcBtW93Q'),(3,1,NULL,'aaa','admin@gmail.com','$2a$10$GA.9NeeG6XqxLJjU9T3UOOlH4EdcJLTn1Ig6M65q70dHxPa75t95K','ROLE_KORISNIK',NULL,NULL),(4,1,NULL,'aaaa','admin@gmail.com','$2a$10$uKH3rMR/xHb3P15kqnJ06OwhkzrSQjFagUsns.vXqRksDYjYvoSsq','ROLE_MODERATOR',NULL,NULL),(6,1,NULL,'aaaaaa','admin@gmail.com','$2a$10$lRvu7b/n4ulkwcOfiuJ0n.9bXVt/tWVPRbR.jFOwyHj3oweAMK1wa','ROLE_KORISNIK',NULL,NULL),(8,1,NULL,'aa','admin@gmail.com','$2a$10$dT/nvwA0PeeWPUzCARCtWeHCIlTOMjs7AYn.GEitz6PvBkCl6qjS.','ROLE_MODERATOR',NULL,NULL),(9,NULL,NULL,'ab','admin@gmail.com','$2a$10$qW4QNZ1z9tkqXN7GDWb4r.HURlLCoBc27Q2sRhrgI3g48YqHes18q',NULL,NULL,NULL),(10,1,'0a7b9d8c-8f89-4497-8440-2c8a8bd3d10e','abc','admin@gmail.com','$2a$10$AwXCOeJhT4ehmU4NiyXT6ujXnjdLSGkdx66IZmi920PSSodOBrdQW','ROLE_KORISNIK',NULL,NULL),(26,NULL,NULL,'Pero Grubac','pero.grubac@student.etf.unibl.org','$2a$10$CfIZeFjOTHAhq.MaTsMLc.oA/w3pkMmLTlgNMFlQsV8jjvhpzoIO2',NULL,'GOOGLE',NULL),(27,NULL,NULL,'a','admin@gmail.com','$2a$10$UtYPRFxNnU7Xu/z5Dk2/FeRUlbqZXIG3J9OEUd2xQ4XP728ibZLCu',NULL,NULL,NULL),(28,NULL,NULL,'Fake Joker','fakejoker2122@gmail.com','$2a$10$UX3FtBfE2d9BOVtl6lCxcuuYOTkjR6aoAFtjFiYsA7IxuM/uKRVty',NULL,'GOOGLE',NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-04-08  9:35:24
