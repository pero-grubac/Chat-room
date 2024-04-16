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
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
INSERT INTO `comment` VALUES (2,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-03-22 12:30:00',1,1,1),(3,'nauka 2','2024-03-22 12:30:00',1,3,1),(4,'nauka 3','2024-03-22 12:30:00',1,3,0),(5,'nauka 4 izmjena 2','2024-03-24 21:54:30',1,3,1),(6,'nauka 5','2024-03-22 12:30:00',1,3,NULL),(10,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 16:15:32',1,3,1),(12,'ispravljen test','2024-04-14 21:23:33',1,1,NULL),(14,'sta ce se sada desiti','2024-04-14 17:13:38',1,1,NULL),(15,'hoces li sada raditi ','2024-04-14 18:36:38',1,1,NULL),(18,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,NULL),(24,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(25,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(26,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(27,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(28,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(29,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(30,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(31,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(32,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(33,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(34,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(35,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(36,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(37,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(38,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(39,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(40,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(41,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(42,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(43,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(44,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(45,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus euismod, velit ut cursus scelerisq','2024-04-14 20:12:33',1,1,1),(46,'a','2024-04-15 19:47:51',1,3,NULL),(48,'a','2024-04-15 20:05:36',1,1,NULL),(49,'a','2024-04-15 20:05:38',1,1,NULL),(50,'a','2024-04-16 00:09:38',1,1,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
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
-- Dumping data for table `logger`
--

LOCK TABLES `logger` WRITE;
/*!40000 ALTER TABLE `logger` DISABLE KEYS */;
INSERT INTO `logger` VALUES (274,'ACTION','Method invoked anonymousUser: class org.unibl.etfbl.ChatRoom.controllers.AuthenticationController.login()'),(275,'EXCEPTION','Exception occurred for user admin: Cannot invoke \"java.lang.Integer.intValue()\" because the return value of \"java.util.Map.get(Object)\" is null'),(276,'EXCEPTION','Exception occurred for user admin: Cannot invoke \"java.lang.Integer.intValue()\" because the return value of \"java.util.Map.get(Object)\" is null'),(278,'EXCEPTION','Exception occurred for user admin: Comment with ID 11 not found'),(280,'EXCEPTION','Exception occurred for user admin: Invalid data');
/*!40000 ALTER TABLE `logger` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permission`
--

LOCK TABLES `permission` WRITE;
/*!40000 ALTER TABLE `permission` DISABLE KEYS */;
INSERT INTO `permission` VALUES (32,1,'ADD'),(33,1,'DELETE'),(34,1,'UPDATE'),(42,10,'ADD'),(45,28,'DELETE'),(46,4,'UPDATE'),(50,26,'ADD'),(51,26,'UPDATE'),(52,26,'DELETE');
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
INSERT INTO `user` VALUES (1,1,NULL,'admin','admin@gmail.com','$2a$10$SHBpBtj88odoDNTy3SISieMP12WJwErrsCcM5dpZCbNxF777BxSIu','ROLE_ADMIN',NULL,'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsInBlcm1pc3Npb25zIjpbIkFERCIsIkRFTEVURSIsIlVQREFURSJdLCJzdWIiOiJhZG1pbiIsImlhdCI6MTcxMzMwMjg0MiwiZXhwIjoxNzEzMzA0MjgyfQ.v40ccYICa6W6B_4Tj8g6fQNKMyxblMXKkTFr9SaBL94'),(3,1,NULL,'aaa','admin@gmail.com','$2a$10$GA.9NeeG6XqxLJjU9T3UOOlH4EdcJLTn1Ig6M65q70dHxPa75t95K','ROLE_KORISNIK',NULL,'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9LT1JJU05JSyIsInBlcm1pc3Npb25zIjpbXSwic3ViIjoiYWFhIiwiaWF0IjoxNzEzMjAzMjYwLCJleHAiOjE3MTMyMDQ3MDB9.0kZltmlkSD14aiQHFU_DqsgFh_3FBwNBa_4s11smEIA'),(4,1,NULL,'aaaa','admin@gmail.com','$2a$10$uKH3rMR/xHb3P15kqnJ06OwhkzrSQjFagUsns.vXqRksDYjYvoSsq','ROLE_MODERATOR',NULL,NULL),(6,1,NULL,'aaaaaa','admin@gmail.com','$2a$10$lRvu7b/n4ulkwcOfiuJ0n.9bXVt/tWVPRbR.jFOwyHj3oweAMK1wa','ROLE_KORISNIK',NULL,NULL),(8,1,NULL,'aa','admin@gmail.com','$2a$10$dT/nvwA0PeeWPUzCARCtWeHCIlTOMjs7AYn.GEitz6PvBkCl6qjS.','ROLE_KORISNIK',NULL,NULL),(9,1,NULL,'ab','admin@gmail.com','$2a$10$qW4QNZ1z9tkqXN7GDWb4r.HURlLCoBc27Q2sRhrgI3g48YqHes18q','ROLE_KORISNIK',NULL,NULL),(10,1,'0a7b9d8c-8f89-4497-8440-2c8a8bd3d10e','abc','admin@gmail.com','$2a$10$AwXCOeJhT4ehmU4NiyXT6ujXnjdLSGkdx66IZmi920PSSodOBrdQW','ROLE_MODERATOR',NULL,NULL),(26,1,NULL,'Pero Grubac','pero.grubac@student.etf.unibl.org','$2a$10$CfIZeFjOTHAhq.MaTsMLc.oA/w3pkMmLTlgNMFlQsV8jjvhpzoIO2','ROLE_ADMIN','GOOGLE',NULL),(27,0,NULL,'a','admin@gmail.com','$2a$10$UtYPRFxNnU7Xu/z5Dk2/FeRUlbqZXIG3J9OEUd2xQ4XP728ibZLCu',NULL,NULL,NULL),(28,NULL,NULL,'Fake Joker','fakejoker2122@gmail.com','$2a$10$UX3FtBfE2d9BOVtl6lCxcuuYOTkjR6aoAFtjFiYsA7IxuM/uKRVty','ROLE_MODERATOR','GOOGLE',NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

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

--
-- Dumping data for table `user_has_room`
--

LOCK TABLES `user_has_room` WRITE;
/*!40000 ALTER TABLE `user_has_room` DISABLE KEYS */;
INSERT INTO `user_has_room` VALUES (1,1),(3,1),(4,1),(6,1),(8,1),(10,1),(1,2),(28,2),(1,3),(26,3),(1,4),(1,8);
/*!40000 ALTER TABLE `user_has_room` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-04-16 23:48:33
