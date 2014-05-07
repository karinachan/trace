
-- goal of this file is to create a csv so that we can easily add information to manipulate
use trace_db;


-- load the .csv files from /tmp/ area...shouldn't be in the /tmp folder anymore because we clean up after ourselves...
load data infile 'classestable.csv' into table classes fields terminated by ',' lines terminated by '\r';
-- SET FOREIGN_KEY_CHECKS=0;
-- previously set this since we were having foreign key constraint issues, fixed by manually creating the first connections
load data infile 'studentsData.csv' into table students fields terminated by ',' lines terminated by '\r';

load data infile 'takingData.csv' into table taking fields terminated by ',' lines terminated by '\r';
-- SET FOREIGN_KEY_CHECKS=1;

load data infile 'sessionsData.csv' into table taking fields terminated by ',' lines termianted by '\r';