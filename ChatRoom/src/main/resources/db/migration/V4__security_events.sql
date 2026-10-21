-- The logger table becomes the SIEM event store: who, from where and when
ALTER TABLE `logger`
  ADD COLUMN `Username` varchar(45) DEFAULT NULL,
  ADD COLUMN `IpAddress` varchar(45) DEFAULT NULL,
  ADD COLUMN `CreatedAt` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ADD KEY `logger_CreatedAt_idx` (`CreatedAt`);
