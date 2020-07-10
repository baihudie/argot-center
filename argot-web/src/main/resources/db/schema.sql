

create table argot_users (
    id INT IDENTITY(100000,1) PRIMARY KEY ,
	mobile VARCHAR(32),
	good_name VARCHAR(64),
	bandit_code VARCHAR(64),
	passwd VARCHAR(64),
	property1 VARCHAR(64),
	property2 VARCHAR(64),
	property3 VARCHAR(64),
	property4 VARCHAR(64),
	property5 VARCHAR(64),
	property6 VARCHAR(64),
	property7 VARCHAR(64),
	property8 VARCHAR(64),
	property9 VARCHAR(64),
	state CHAR(1),
	remark VARCHAR(256),
	create_time DATETIME,
	update_time DATETIME
);