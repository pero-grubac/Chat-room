-- Initial schema (previously ddl.sql)

CREATE TABLE IF NOT EXISTS `forumroom` (
  `IdForumRoom` int NOT NULL AUTO_INCREMENT,
  `Name` varchar(45) NOT NULL,
  PRIMARY KEY (`IdForumRoom`),
  UNIQUE KEY `Name_UNIQUE` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `comment` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `logger` (
  `IdLogger` int NOT NULL AUTO_INCREMENT,
  `Type` varchar(45) NOT NULL,
  `Message` varchar(255) NOT NULL,
  PRIMARY KEY (`IdLogger`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `permission` (
  `IdPermission` int NOT NULL AUTO_INCREMENT,
  `IdUser` int NOT NULL,
  `Permission` varchar(45) NOT NULL,
  PRIMARY KEY (`IdPermission`),
  KEY `fk_User_has_Permission_idx` (`IdUser`),
  CONSTRAINT `fk_User_has_Permission` FOREIGN KEY (`IdUser`) REFERENCES `user` (`IdUser`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_has_room` (
  `IdUser` int NOT NULL,
  `IdForumRoom` int NOT NULL,
  PRIMARY KEY (`IdUser`, `IdForumRoom`),
  KEY `Room_IdForumRoom_idx` (`IdForumRoom`),
  CONSTRAINT `Room_IdForumRoom` FOREIGN KEY (`IdForumRoom`) REFERENCES `forumroom` (`IdForumRoom`),
  CONSTRAINT `User_IdUser` FOREIGN KEY (`IdUser`) REFERENCES `user` (`IdUser`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
