To run locally:

1) setup a mysql instance

2) edit 3 files put the right credentials to smtp server and mysql database 
src/main/resources/META-INF/spring/database.properties
src/main/resources/META-INF/spring/email.properties
src/test/resources/META-INF/spring-test/email.properties

3) 
mvn install
mvn jetty:run 

The you should see the app running on localhost:9090/linker-webapp

Done


Just after the start an empty db will be prepopulated with two roles ROLE_ADMIN and ROLE_USER
and an admin user 
username: admin@example.com 
password: 123456

To configure logging and base application url 
have a lookk at: 

~/sindice/linker-webapp/config.xml
~/sindice/linker-webapp/logback.xml

Logs can be found: 
~/sindice/log/linker-webapp/

This folders should be created when you first time run the application

Note: for developers about working with jspx templates

Jspx are cool :-) but at the start they might give you a little headache
Have a look at some toutorials before you start 
http://jspx-bay.sourceforge.net/
 
Remeber that the template file has to be valid xml file:
1) No empty elements like <p></p>  If you need them do  <p><!-- --></p>
2) No html entities, use unicode representation so instead &copy; put &#169;
add here more points if you think something should be added 
 


