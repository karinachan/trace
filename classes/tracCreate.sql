-- Karina and Vivienne
-- Design and Plan DDL statements
-- 4/5/14

-- remove existing tables


drop table if exists visting;
drop table if exists sessions;
drop table if exists taking;
drop table if exists tutors;
drop table if exists students;
drop table if exists classes;



-- creating tables
use trace_db;
create table students(
bid int primary key not null, -- the student's id number for primary
studname varchar(50) not null -- the student's name
)
ENGINE= InnoDB;

create table classes(
crn integer primary key, -- crn of class
className varchar(5) not null,
vtype enum('helproom/peer tutoring','SI','writing tutor','none'))
ENGINE = InnoDB;

create table tutors(
bid int primary key not null,
crn int not null,
foreign key (bid) references students(bid) on delete restrict,
foreign key (crn) references classes(crn) on delete restrict)
ENGINE=InnoDB;



create table sessions( -- I think we'll have to have this insert the rest of info first and then update with time
vid integer auto_increment primary key not null,
tid integer not null,
crn integer not null,
bid integer not null,
roomnum varchar(10) not null,
length integer, -- right now just a length, maybe use cookies to capture the actual time!
foreign key (tid) references tutors(bid) on delete restrict,
foreign key (crn) references classes(crn) on delete restrict,
foreign key (bid) references students(bid) on delete restrict)
ENGINE = InnoDB;


create table visiting(
  bid integer not null,
  vid integer not null,
  foreign key (vid) references sessions(vid) on delete restrict,
  foreign key (bid) references students(bid) on delete restrict
)
ENGINE= InnoDB;

create table taking(
bid integer not null,
crn integer not null,
foreign key (bid) references students(bid) on delete restrict,
foreign key (crn) references classes(crn) on delete restrict)
ENGINE= InnoDB;



-- previously set this since we were having foreign key constraint issues, fixed by manually creating the first connections
load data local infile 'studentsData.csv' into table students fields terminated by ',' lines terminated by '\r';

-- load the .csv files from /tmp/ area...shouldn't be in the /tmp folder anymore because we clean up after ourselves...
load data local infile 'classestable.csv' into table classes fields terminated by ',' lines terminated by '\r';
-- SET FOREIGN_KEY_CHECKS=0;

load data local infile 'takingData.csv' into table taking fields terminated by ',' lines terminated by '\r';
-- SET FOREIGN_KEY_CHECKS=1;

load data local infile 'tutorData.csv' into table tutors fields terminated by ',' lines terminated by '\r';

load data local infile 'sessionsData.csv' into table sessions fields terminated by ',' lines terminated by '\r';
