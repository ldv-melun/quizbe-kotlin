REPLACE INTO `role` VALUES (1,'ADMIN');
REPLACE INTO `role` VALUES (2,'TEACHER');
REPLACE INTO `role` VALUES (3,'USER');

-- admin pw:adminadmin
REPLACE INTO user VALUES(1,'2022-03-23 17:41:34','2022-08-30 19:17:52.585926', 'adminadmin', 'admi@admin.org',1,'$2a$10$o1KXSJnVERFgC7zrpA65TOLWC9JOuhzH5LbKP2veoNDUbguCwqvJm','admin' );

REPLACE INTO `user_roles` VALUES(1,1);
REPLACE INTO `user_roles` VALUES(1,2);
REPLACE INTO `user_roles` VALUES(1,3);


