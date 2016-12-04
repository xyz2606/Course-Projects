
drop table HasKeywords;
drop table Keywords;
drop table Favorites;
drop table Rates;
drop table Feedback;
drop table Trust;
drop table Visit;
drop table VisEvent;
drop table Users;
drop table POI;


create table POI(
	pid int NOT NULL AUTO_INCREMENT,
	name varchar(50),
	state varchar(50),
	city varchar(50),
	price float,
	category varchar(50),
	telephone varchar(50),
	address varchar(50),
	HOO varchar(50),
	YOE varchar(50),
	URL varchar(50),
	primary key (pid)
);

create table Users(
	login char(30),
	name varchar(40),
	userType varchar(20),
	password varchar(50),
	phone varchar(50),
	address varchar(50),
	primary key (login)
);

create table VisEvent(
	vid int NOT NULL AUTO_INCREMENT,
	cost float,
	numberofheads int,
	primary key (vid)
);

create table Visit(
	login char(30),
	pid int,
	vid int,
	visitdate date,
	primary key (login, pid, vid),
	foreign key (login) references Users(login),
	foreign key (pid) references POI(pid),
	foreign key (vid) references VisEvent(vid)
);

create table Trust(
	login1 char(30),
	login2 char(30),
	isTrusted int,
	primary key (login1, login2),
	foreign key (login1) references Users(login),
	foreign key (login2) references Users(login)
);

CREATE TABLE Feedback (
	fid int NOT NULL AUTO_INCREMENT,
	pid int NOT NULL,
	login char(30),
	comment_text char(100),
	fbdate date,
	score int,
	PRIMARY KEY(fid),
	FOREIGN KEY(pid) REFERENCES POI(pid),
	FOREIGN KEY(login) REFERENCES Users(login)
);

CREATE TABLE Rates (
	login char(30),
	fid int,
	rating int,
	PRIMARY KEY(login, fid),
	FOREIGN KEY(login) REFERENCES Users(login),
	FOrEIGN KEY(fid) REFERENCES Feedback(fid)
);

CREATE TABLE Favorites (
	pid int,
	login char(30),
	fvdate timestamp,
	PRIMARY KEY(pid, login),
	FOREIGN KEY(pid) REFERENCES POI(pid),
	FOREIGN KEY(login) REFERENCES Users(login)
);

CREATE TABLE Keywords (
	wid int NOT NULL AUTO_INCREMENT,
	language char(20),
	word char(50),
	PRIMARY KEY(wid)
);

CREATE TABLE HasKeywords (
	pid int,
	wid int,
	PRIMARY KEY(pid, wid),
	FOREIGN KEY(pid) REFERENCES POI(pid),
	FOREIGN KEY(wid) REFERENCES Keywords(wid)
);


insert into Users(login,name,userType,password) values('admin','admin','admin','dgplq');
