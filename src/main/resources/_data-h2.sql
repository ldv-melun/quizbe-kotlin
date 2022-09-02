insert INTO ROLE VALUES (1,'ADMIN');
insert INTO ROLE VALUES (2,'TEACHER');
insert INTO ROLE VALUES (3,'USER');

-- admin pw:adminadmin
insert INTO USER VALUES(1,'2022-03-23 17:41:34','2022-08-30 19:17:52.585926', 'adminadmin', 'admi@admin.org',1,'$2a$10$o1KXSJnVERFgC7zrpA65TOLWC9JOuhzH5LbKP2veoNDUbguCwqvJm','admin' );

insert INTO USER_ROLES VALUES(1,1);
insert INTO USER_ROLES VALUES(1,2);
insert INTO USER_ROLES VALUES(1,3);




