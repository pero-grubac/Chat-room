INSERT INTO forumroom (Name) VALUES ('Science'), ('Culture'), ('Sports'), ('Music');

INSERT INTO user (IsApproved, Username, Email, Password, Role, Source) 
/* admin admin123 :) */
/* Add email where the token will come*/
VALUES (1, 'admin', 'admin@gmail.com', '$2b$10$pgP4aVmyR1rH0itGJ5AnAO1QsLlFCUyxSh1kBqNBxC/UK0yZyUaBu', 'ROLE_ADMIN', 'LOCAL');

INSERT INTO permission (IdUser, Permission) 
VALUES 
  ((SELECT IdUser FROM user WHERE Username = 'admin'), 'ADD'),
  ((SELECT IdUser FROM user WHERE Username = 'admin'), 'UPDATE'),
  ((SELECT IdUser FROM user WHERE Username = 'admin'), 'DELETE');