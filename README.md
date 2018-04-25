# ORACDC

Read Oracle Archive Log to support Changed Data Capture scenarios and output the statements to a writer of your liking.

## Compilation

- Download Oracle JDBC Driver from http://www.oracle.com/technetwork/apps-tech/jdbc-112010-090769.html
- Install the driver in your own MVN repository by executing `mvn install:install-file -Dfile=ojdbc6.jar -DgroupId=com.oracle -DartifactId=ojdbc -Dversion=6 -Dpackaging=jar`
- I still have to take a look if one is allowed to package the Oracle JDBC driver with the application, I suspect not!
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
- Create a directory to hold the config files
  * `mkdir conf`
  * `cd conf`
- Extract property files from the jar:
  * `$ jar xf ../cdc-1.0-SNAPSHOT.jar cdc.properties.template db.properties.template AwsCredentials.properties.template kinesis.properties.template`
- Now rename, update and save those properties files to your liking
  * `mv db.properties.template db.properties`
  * `vi db.properties`
  * `chmod 600 db.properties`
  * `mv cdc.properties.template cdc.properties`
  * `vi cdc.properties`
- If you are using the Kinesis writer, do the same for those property files
  * `mv kinesis.properties.template kinesis.properties`
  * `vi kinesis.properties`
  * `mv AwsCredentials.properties.template AwsCredentials.properties`
  * `vi AwsCredentials.properties`
  * `chmod 600 AwsCredentials.properties`
- Run the CDC tool:
  * `cd ..`
  * `$ java -cp "oracdc-1.0-SNAPSHOT.jar:./conf/" nl.whizzkit.oracdc.CDC`
- Perform DML on the database table that is watched
- You can stop the tool by pressing ctrl-c. The db connection will be shutdown by a shutdownhook

## Solving errors 

- If you run into `Exception in thread "main" java.sql.SQLException: ORA-01291: missing logfile` you probably have selected a `start` value in `cdc.properties` that is too far away in history.
- The fetchSize is currently set to '1' in the program. This will lead to a lot of network traffic, but it results in tighter synchronization
- I have created a separate conf directory in the project path that holds the 'real' properties. In IDEA, add `-Xbootclasspath/a:conf/` to the VM Options in a run configuration

## Properties

- db.properties
  * `user=scott` : username
  * `password=tiger` : password
- cdc.properties
  * `writer=nl.whizzkit.oracdc.writer.ConsoleWriter, nl.whizzkit.oracdc.writer.DummyWriter`: where to direct the output. You may add a comma separated list of class names.
  * `driver=oracle.jdbc.OracleDriver`: database connection driver
  * `url=jdbc:oracle:thin:@localhost:1521:XE` : database connection url
  * `start=01-Jan-2018 00:00:00` : start time of redo log
  * `operations='INSERT','UPDATE','DELETE'` : operations to log (may be more than one but separated by comma and enclosed by single quote)
  * `tables='EMPLOYEE','DEPARTMENT'` : tables to log (may be more than one but separated by comma and enclosed by single quote)
  * `schema='SCOTT'` : schema to query (only one entry allowed)

## Adding new writers

You may add new writers by creating classes that implement the IWritable interface and configure the `writer` property value accordingly. Currently there are 3 writers:

- ConsoleWriter. This just writes the statement to the console.
- KafkaWriter. This one writes to a Kafka topic, config is hardcoded for now.
- KinesisWriter. This one writes to a Kinesis Stream, it's configurable by editing kinesis.properties and AwsCredentials.properties.
