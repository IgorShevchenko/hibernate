
1) SETUP LOGGING

Run with VM argument:
-Djava.util.logging.config.file=src/main/resources/logging.properties

In Eclipse setup 'Arguments' for 'Run Configurations' and 'Debug Configurations'.

2) Every interaction with your database should occur within explicit transaction boundaries, even if you are only reading data.
