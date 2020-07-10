

create table users (
    id int NOT NULL AUTO_INCREMENT,
	mobile varchar(32),
	user_name varchar(40),
	passwd varchar(64),
	property1 varchar(64),
	property2 varchar(64),
	property3 varchar(64),
	property4 varchar(64),
	property5 varchar(64),
	property6 varchar(64),
	property7 varchar(64),
	property8 varchar(64),
	property9 varchar(64),
	state char(1),
	remark varchar(256),
	create_time timestamp default current_timestamp,
	update_time timestamp default current_timestamp on update current_timestamp,
    primary key (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;