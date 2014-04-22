-- Karina and Vivienne
-- Design and Plan DDL statements
-- 4/5/14

-- remove existing tables

drop table if exists sessions;
drop table if exists tutors;
drop table if exists taking;
drop table if exists students;
drop table if exists classes;


-- creating tables
use trace_db;
create table students(
bid int primary key not null, -- the student's id number for primary
studname varchar(50) not null -- the student's name
)
ENGINE= InnoDB;

create table tutors(
bid int primary key not null,
foreign key (bid) references students(bid) on delete restrict)
ENGINE=InnoDB;

create table classes(
crn integer primary key, -- crn of class
vtype enum('helproom/peer tutoring','SI','writing tutor','none'))
ENGINE = InnoDB;

create table sessions(
vid integer auto_increment primary key not null,
tid integer not null,
crn integer not null,
bid integer not null,
roomnum integer not null,
dayof char(10) not null, -- missing the time for now, put int as placeholder
timeof integer not null,
foreign key (tid) references tutors(bid) on delete restrict,
foreign key (crn) references classes(crn) on delete restrict,
foreign key (bid) references students(bid) on delete restrict)
ENGINE = InnoDB;



create table taking(
bid integer not null,
crn integer not null,
foreign key (bid) references students(bid) on delete restrict,
foreign key (crn) references classes(crn) on delete restrict)
ENGINE= InnoDB;
