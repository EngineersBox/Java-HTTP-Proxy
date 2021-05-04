MAVEN_VERSION:=3.8.1
MAVEN_FULL_NAME:=apache-maven-$(MAVEN_VERSION)
MAVEN_TAR:=$(MAVEN_FULL_NAME)-bin.tar.gz
MAVEN_TAR_URL:=https://apache.mirror.digitalpacific.com.au/maven/maven-3/$(MAVEN_VERSION)/binaries/$(MAVEN_TAR)
M2_HOME:=/usr/local/apache-maven/$(MAVEN_FULL_NAME)
MAVEN_CONFIG_OPTS:="export M2_HOME=$(M2_HOME);export M2=$M2_HOME/bin;export MAVEN_OPTS=-Xms256m -Xmx512m;export PATH=$M2:$PATH"

.PHONY: build_jar run_jar install_maven_darwin install_maven_linux_binary install_maven_linux_apt

install_maven_darwin:
	@brew install maven

install_maven_linux_binary:
	@curl -v $(MAVEN_TAR_URL) -o $(MAVEN_TAR)
	@tar -xvf $(MAVEN_TAR) -C $(M2_HOME)
	@echo $(MAVEN_CONFIG_OPTS) >> ~/.bashrc
	@source ~/.bashrc

install_maven_linux_apt:
	@sudo apt-get install maven

build_jar:
	@mvn install
	@mvn package
	@mv target/HTTP-Proxy-0.1.0-shaded.jar HTTP-Proxy-0.1.0.jar

run_jar:
	@java -jar -Dconfig.path=resources/config.json -Dlog4j.configurationFile=logback.xml HTTP-Proxy-0.1.0-shaded.jar