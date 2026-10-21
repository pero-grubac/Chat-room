-- 2FA codes now expire
ALTER TABLE `user` ADD COLUMN `TwoFactorTokenExpiresAt` datetime DEFAULT NULL AFTER `TwoFactorToken`;
