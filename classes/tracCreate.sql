-- Karina and Vivienne
-- Design and Plan DDL statements
-- 4/5/14

-- remove existing tables

SET FOREIGN_KEY_CHECKS=0;
drop table if exists visiting;
drop table if exists sessions;
drop table if exists taking;
drop table if exists tutors;
drop table if exists person;
drop table if exists classes;
drop table if exists login_user;
SET FOREIGN_KEY_CHECKS=1;


-- creating tables
use trace_db;
create table person(
bid int primary key not null, -- the student's id number for primary
studname varchar(50) not null, -- the student's name
ptype enum('student','admin','tutor'))
ENGINE= InnoDB;


create table classes(
crn integer primary key, -- crn of class
className varchar(5) not null,
vtype enum('helproom/peer tutoring','SI','writing tutor','none'))
ENGINE = InnoDB;

create table tutors(
bid int not null,
crn int not null,
foreign key (bid) references person(bid) on delete restrict,
foreign key (crn) references classes(crn) on delete restrict)
ENGINE=InnoDB;


create table login_user(
    user varchar(30) primary key,
    pass varchar(30),
    cryp char(41),
    bid int not null,
    foreign key (bid) references person(bid)
);

insert into login_user(user,pass,cryp,bid) values
 ('Tutor Example A','tutor',password('tutor'),22222222),
 ('Tutor Example B','tutor',password('tutor'),22222221),
 ('Tutor Example C','tutor',password('tutor'),22222223),
 ('Admin','admin',password('admin'), 99999999);

create table sessions( -- I think we'll have to have this insert the rest of info first and then update with time
vid integer auto_increment primary key not null,
tid integer not null,
crn integer not null,
entertime timestamp default current_timestamp,
howlong integer, 
status enum('in progress','closed'),
foreign key (tid) references tutors(bid) on delete restrict,
foreign key (crn) references classes(crn) on delete restrict)
ENGINE = InnoDB;


create table visiting(
 bid integer not null,
 vid integer not null,
 exittime timestamp not null, -- keep in mind that exit is not allowed for a var name
 foreign key (vid) references sessions(vid) on delete restrict,
 foreign key (bid) references person(bid) on delete restrict)
ENGINE= InnoDB;

create table taking(
bid integer not null,
crn integer not null,
foreign key (bid) references person(bid) on delete restrict,
foreign key (crn) references classes(crn) on delete restrict)
ENGINE= InnoDB;



-- previously set this since we were having foreign key constraint issues, fixed by manually creating the first connections
load data infile '/tmp/studentsData.csv' into table person fields terminated by ',' lines terminated by '\r';

-- load the .csv files from /tmp/ area...shouldn't be in the /tmp folder anymore because we clean up after ourselves...
load data infile '/tmp/classestable.csv' into table classes fields terminated by ',' lines terminated by '\r';
-- SET FOREIGN_KEY_CHECKS=0;

load data infile '/tmp/takingData.csv' into table taking fields terminated by ',' lines terminated by '\r';
-- SET FOREIGN_KEY_CHECKS=1;

load data infile '/tmp/tutorData.csv' into table tutors fields terminated by ',' lines terminated by '\r';

load data infile '/tmp/sessionsData.csv' into table sessions fields terminated by ',' lines terminated by '\n';
