# create a instance
# Note: please Before executing the command, please clear the datadir.
docker run -p 3306:3306 --name=mysql1 \
--env MYSQL_ROOT_HOST=% \
--env MYSQL_ROOT_PASSWORD=pass1234 \
--mount type=bind,src=/home/jaybill/software/mysql/my.cnf,dst=/etc/my.cnf \
--mount type=bind,src=/home/jaybill/software/mysql/datadir/1,dst=/var/lib/mysql \
-d \
container-registry.oracle.com/mysql/community-server:8.3

