# DerpBot

DerpBot is a multi-purpose IRC bot.

Dependencies:

* Maven (For building from sources)
* JDK8 (For building from sources, JRE8 enough for just running the built JAR)

### Building

Execute 

```sh
mvn clean package
```

### "Installation"

* Create directory /opt/derpbot/ and place the built JAR file as /opt/derpbot/DerpBot.jar
* Create a configuration file as /opt/derpbot/derpbot.properties (see derpbot.properties.example for reference)
* Create a (system)user for DerpBot:

```sh
useradd -s /bin/false -r -M -d /opt/derpbot derpbot
```

* The initscript contains a sysvinit initscript, copy it as /etc/init.d/derpbot and execute 

```sh
update-rc.d derpbot defaults
```

Note: DerpBot will look for derpbot.properties configuration file in the directory where the JAR file itself is, and one directory above that. This means, during development, you can have your local derpbot.properties file in the "root" of the project and execute the JAR directly from the Maven target directory.

### Running

For "installed" version:

```sh
service derpbot start
```

For built version (while in the "root" of the project):

```sh
java -jar target/DerpBot.jar
```

Easily compile and run while developing:

```
mvn compile exec:java
```

### Developing additional features

The basic concept of creating a new feature is to create a new class under fi.derpnet.derpbot.handler.impl and implement one of the following interfaces (which ever suits your needs best), and add the class to HandlerRegistry. (This could be replaced with some sort of dynamic scanning of the classpath for compatible interfaces...)

* **RawMessageHandler** - handles any RAW message that comes from the IRC server
* **SimpleMessageHandler** - handles regular PRIVMSG messages (both on channels and in queries and sends a one-message response
* **SimpleMultiLineMessageHandler** - same as above but is capable of sending multiple lines of response
* **LoudMessageHandler** - handles regular PRIVMSG messages (both on channels and in queries and sends a one-message response to the sender if the original message was sent to a quieter channel
* **LoudMultiLineMessageHandler** - same as above but is capable of sending multiple lines of response

Package conventions:
* **fi.derpnet.derpbot.adapter** - contains adapter classes (mainly for handler adapters)
* **fi.derpnet.derpbot.bean** - contains bean-like classes
* **fi.derpnet.derpbot.util** - contains utility classes
* **fi.derpnet.derpbot.handler** - contains the handler-specific interfaces
* **fi.derpnet.derpbot.handler.impl** - contains the actual handler implementations