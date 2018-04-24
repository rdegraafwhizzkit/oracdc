# ORACDC

Read Oracle Archive Log to support Changed Data Capture scenarios and output the statements to a writer of your liking

## Compilation

- Download Oracle JDBC Driver from http://www.oracle.com/technetwork/apps-tech/jdbc-112010-090769.html
- Install the driver in your own MVN repository by executing `mvn install:install-file -Dfile=ojdbc6.jar -DgroupId=com.oracle -DartifactId=ojdbc -Dversion=6 -Dpackaging=jar`
- Build jar by invoking `mvn clean package`

## Prepare database

As a sysdba, put the database into Archive Log mode if it isn't already and enable supplemental log data:

`$ sqlplus / as sysdba`

`SQL> SHUTDOWN IMMEDIATE;`

`SQL> STARTUP MOUNT;`

`SQL> ALTER DATABASE ARCHIVELOG;`

`SQL> ALTER DATABASE OPEN;`

`SQL> ALTER DATABASE ADD SUPPLEMENTAL LOG DATA;`

As a sysdba, create a user and grant just enough roles and privileges. Note that these privileges are already very broad!

`$ sqlplus / as sysdba`

`SQL> create user test identified by test;`

`SQL> grant create session to test;`

`SQL> grant execute_catalog_role to test;`

`SQL> grant select any transaction to test;`

`SQL> grant select on v_$logmnr_contents to test;`

## Running the jar (outside IDE)

- Put cdc-1.0-SNAPSHOT.jar in an empty directory
- Extract property files from the jar:
  * `$ jar xf cdc-1.0-SNAPSHOT.jar cdc.properties db.properties`
- Remove these property files from the jar file:
  * `$ zip -d cdc-1.0-SNAPSHOT.jar cdc.properties db.properties`
- Now update and save those two properties files to your liking
  * `vi db.properties`
  * `chmod 600 db.properties`
  * `vi cdc.properties`
- Run the CDC tool:
  * `$ java -cp "cdc-1.0-SNAPSHOT.jar:." nl.whizzkit.oracdc.CDC`
- Perform DML on the database table that is watched

## Properties

- db.properties
  * `user=scott` : username
  * `password=tiger` : password
- cdc.properties
  * `writer=ConsoleWriter`: where to direct the output
  * `driver=oracle.jdbc.OracleDriver`: database connection driver
  * `url=jdbc:oracle:thin:@localhost:1521:XE` : database connection url
  * `start=01-Jan-2018 00:00:00` : start time of redo log
  * `operations='INSERT','UPDATE','DELETE'` : operations to log (may be more than one but separated by comma and enclosed by single quote)
  * `tables='EMPLOYEE','DEPARTMENT'` : tables to log (may be more than one but separated by comma and enclosed by single quote)
  * `schema='SCOTT'` : schema to query (only one entry allowed)

## Adding new writers

You may add new writers by creating classes that implement the IWritable interface and configure the `writer` property value accordingly.
