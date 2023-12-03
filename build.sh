 mvn install:install-file -Dfile=pom.xml -DgroupId=org.jaybill -DartifactId=fast-im -Dversion=1.0-SNAPSHOT -Dpackaging=pom
 cd common
 mvn clean install
 cd ../common-cache
 mvn clean install