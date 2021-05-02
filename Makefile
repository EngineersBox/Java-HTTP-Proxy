MAVEN_TAR_URL:=https://apache.mirror.digitalpacific.com.au/maven/maven-3/3.8.1/binaries/apache-maven-3.8.1-bin.tar.gz

.PHONY: build_jar run_jar install_maven_darwin install_maven_linux_binary install_maven_linux_apt

install_maven_darwin:
	@brew install maven

install_maven_linux_binary:
	@curl -v $(MAVEN_TAR_URL) -o apache-maven-3.8.1-bin.tar.gz
	@tar -xvf apache-maven-3.8.1-bin.tar.gz -C /usr/local/apache-maven/apache-maven-3.8.1
	@echo "export M2_HOME=/usr/local/apache-maven/apache-maven-3.8.1;export M2=$M2_HOME/bin;export MAVEN_OPTS=-Xms256m -Xmx512m;export PATH=$M2:$PATH" >> ~/.bashrc
	@source ~/.bashrc

install_maven_linux_apt:
	@sudo apt-get install maven

build_jar:
	@mvn install
	@mvn package
	@mv target/HTTP-Proxy-0.1.0-shaded.jar HTTP-Proxy-0.1.0.jar

run_jar:
	@java -jar -Dconfig.path=resources/config.json -Dlog4j.configurationFile=logback.xml HTTP-Proxy-0.1.0-shaded.jar