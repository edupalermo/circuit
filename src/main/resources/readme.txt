mvn clean install exec:java -Dexec.mainClass="org.circuit.Application"


mvn clean install exec:java -Dexec.mainClass="org.circuit.Client"

set JAVA_OPTS="-Xmx1500m -Xms256m"


java -Xmx2G -cp target/classes/. org.circuit.AnotherTest
java -Xmx1G -cp target/classes/. org.circuit.Application


java -cp target/circuito-0.0.1-SNAPSHOT.jar -Dloader.main=org.circuit.Application org.springframework.boot.loader.PropertiesLauncher
java -cp target/circuito-0.0.1-SNAPSHOT.jar -Dloader.main=org.circuit.Client org.springframework.boot.loader.PropertiesLauncher