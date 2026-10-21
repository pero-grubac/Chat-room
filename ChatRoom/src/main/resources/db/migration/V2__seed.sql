-- Initial data (previously seed.sql). Idempotent, so it is safe on a database created by the old ddl.sql + seed.sql.

INSERT IGNORE INTO `forumroom` (`Name`) VALUES ('Science'), ('Culture'), ('Sports'), ('Music');

-- admin / admin123; the email receives the 2FA codes (ADMIN_EMAIL)
INSERT IGNORE INTO `user` (`IsApproved`, `Username`, `Email`, `Password`, `Role`, `Source`)
VALUES (1, 'admin', '${adminEmail}', '$2b$10$pgP4aVmyR1rH0itGJ5AnAO1QsLlFCUyxSh1kBqNBxC/UK0yZyUaBu', 'ROLE_ADMIN', 'LOCAL');

INSERT INTO `permission` (`IdUser`, `Permission`)
SELECT u.`IdUser`, p.`Permission`
FROM `user` u
JOIN (SELECT 'ADD' AS `Permission` UNION ALL SELECT 'UPDATE' UNION ALL SELECT 'DELETE') p
WHERE u.`Username` = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM `permission` x WHERE x.`IdUser` = u.`IdUser` AND x.`Permission` = p.`Permission`
  );
