create table Account (id int auto_increment,
						username varchar unique,
						firstName varchar not null, 
						lastName varchar not null,
						password varchar default '',
						primary key (id));
