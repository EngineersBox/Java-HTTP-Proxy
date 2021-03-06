# Java-HTTP-Proxy

HTTP proxy written in Java with link and ref reformatting

## Maven Installation

This project is managed via Maven, and built with the surefire plugin. In order to build the jar file
you'll need to have maven installed.

If you haven't already got it installed, then you can run one of the makefile targets depending on your environment:

* Mac OS X: `$> brew install maven` or `$> make install_maven_darwin`
* Linux (binary): `$> make install_maven_linux_binary`
* Linux (apt): `$> sudo apt install maven` or `$> make install_maven_linux_apt`

*Note:* If you are using windows, then it's not as simple to install maven. I would suggest that you run this on a unix system,
but if you Do want to install it on Windows, a tutorial can be found here <https://www.javatpoint.com/how-to-install-maven>

## Building

Once you have maven installed, run the following to build the project to a jar file:

```shell
$> make build_jar
```

Alternatively, if you want to build it manually then you can run the following:

```shell
$> rm -rf target
$> rm -rf http-proxy-0.1.0.jar
$> mvn -am install package
$> mv target/http-proxy-0.1.0-shaded.jar http-proxy-0.1.0.jar
```

## Usage

The jar can be run with the Makefile provided it was build with maven or the `build_jar` makefile target.
By default, using the makefile target `run_jar` will use the config from `resources/config.json`. If you want
to use a custom config, make sure to specify it when running the jar, see the _**Jar Arguments**_ section below for more information.

Running the jar with the makefile target:

```shell
$> make run_jar
```

Running the jar via the java command directly (make sure the jar is in the top level directory and not in `target`):

```shell
$> java -jar -Dconfig.path=resources/config.json -Dlog4j.configurationFile=logback.xml http-proxy-0.1.0.jar
```

### Jar Arguments

* `-Dconfig.path=<PATH>`: Specify the path to the `config.json` file. Defaults to `./config.json`
* `-Dlog4j.configurationFile=<PATH>`: Specify the path to the `logback.xml` file to configure log4j

## Javadocs

All the methods and classes within this project have Javadoc's in order to provide information on both usage and implementation.
Javadocs can be built out into a displayable web format using the inbuilt `javadoc` command.

There is a makefile target created to generate these docs and can be used as such:

```shell
$> make generate_documentation
```

or if you want to build it yourself, that can be done with:

```shell
$> mkdir -p docs
$> rm -rf docs/*
$> find src/main -type f -name "*.java" | xargs javadoc -d docs
```

Once you have built the docs, simply open the `index.html` within the `docs` directory:

```shell
$> open docs/index.html
```

## Maven Dependencies

This project relies on maven as the package manager and build system. There are several dependencies used for various
functionality, ranging from compression to dependency injection. Below is a list of all of the dependencies, however more
detail can be found in the `pom.xml` at the root of the project.

| **Dependency**         	| **Summary**                                                	|
|------------------------	|------------------------------------------------------------	|
| JUnit Jupiter Engine   	| Testing framework                                          	|
| Apache Commons Lang 3  	| Common utilities and QoL methods                           	|
| Maven Shade            	| Single JAR packaging and naming resolution                 	|
| Maven Commons Compress 	| Compression utilities                                      	|
| Google GSON            	| JSON handler                                               	|
| Google Guice           	| Dependency injection                                       	|
| Google Reflections     	| Reflection utilities                                       	|
| Google Brotli Dec      	| Brotli compression (Maven Commons Compress requirement)    	|
| ZStd JNI               	| ZStandard compression (Maven Commons Compress requirement) 	|
| Log4j                  	| Logging                                                    	|
| JSoup                  	| HTML node serialiser and handler                           	|

## Logging

Logging is done via the `log4j` library which allow for asynchronous queue based log resolution. Loggers can are defined
on a per class basis and present themselves alongside the thread they are bound to. Each of the log entries is timestamped
according to UTC time.

The logger format is defined in the `logback.xml` file as:

```xml
<PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
```

Resulting in a logging entry as such:

```log
23:51:36.646 [main] INFO  com.engineersbox.httpproxy.Proxy - An example logging entry
```

Here we attach this to a root logger, preset to the `INFO` level. There are a multitude of logging levels utilised in this
project depending on the situation and visibility requirement. Each of the levels, going down, provides more detail into
the functionality of the processes executing. In order to least to most detail the available logging levels used in this
project are:

* INFO
* WARN
* DEBUG
* TRACE

If you want to view a different logging level, this will need to be changed in the `logback.xml` under the root logger
declaration. After doing so, the application wll need to be restarted.

## Configuration

The proxy utilises JSON configuration files to specify the required behaviour it should enact.
There are two main configuration sections involed:

* policies
* servlet

An example configuration file:

```json
{
	"servlet": {
		"threading": {
			"acceptorPoolSize": 10,
			"handlerPoolSize": 10,
			"schedulingPolicy": "ABORT"
		},
		"connections": {
			"acceptorQueueSize": 10,
			"handlerQueueSize": 10,
			"dropAfter": 1000,
			"readerBufferSize": 1024,
			"writeBufferSize": 1024
		},
		"messages": {
			"maxBodySize": 600000
		},
		"binding": {
			"host": "localhost",
			"port": 3000
		}
	},
	"policies": {
		"enforcement": {
			"whitelistBehaviour": {
				"ip": "BLACKLIST",
				"url": "WHITELIST"
			},
			"allowRedirects": true
		},
		"rulesets": [
			{
				"type": "URL",
				"isWildcard": true,
				"pattern": "(http(s)?://)?some\\.[(prod)|(dev)]\\.site\\.com:[3001-4200]/"
			}
		],
		"textReplacements": [
			{
				"from": "(?i)Sydney",
				"to": "New York"
			},
			{
				"from": "(?i)Perth",
				"to": "Hokkaido"
			}
		],
		"linkReplacements": [
			{
				"from": "www\\.bom\\.gov\\.au",
				"to": "localhost:3000"
			}
		]
	},
	"target": {
		"host": "www.bom.gov.au",
		"port": 80
	}
}
```

A JSON schema has been provided in `resources/config.schema.json` that follows the [JSON Schema draft 7](http://json-schema.org/draft-07/schema#) format.

### Policies

Configuring the proxy to service certain egress/ingress policies is done via a JSON configuration. This config
details how the proxy should act to IP addresses and URLs when accepting, forwarding, connecting and reformatting.

The schema is broken down into 3 main sections:

* enforcement
* whitelist
* blacklist

#### Enforcement

Within the enforcement section, we detail how the proxy honours the rule sets. The configuration properties
define whether to use the whitelist for IPs and/or URLs, and whether to block everything if it doesn't match.

#### Whitelist and Blacklist

The whitelist and blacklist behave the same in the way they act. Both have subfields `ip` and `url` allow a list of
values to acknowledge when scrutinising given resources. These can be wildcarded with valid Java regexes.

##### IP Addresses

Specifying an IP address should be in standard IPv4 or IPv6 format. These will be validated according and ignored if invalid.
If a given rule is invalid then a log entry will be created at the DEBUG level.

##### URLs

Creating a URL entry is done in standard format including schema. These rule sets will be applied to both the resource
URLs and also in place links such as `<a href="...">`, `<img src="...">` or `<link src="...">`.

##### Wildcard regex

When specifying IPs or URLs, you can also use limited regex to encompass a range of values to avoid needless repetition.
You can use any standard Java regex operator syntax.

You can use this at any point within an IP or URL, for example:

* `(http(s)?://)?some.[(prod)|(dev)].site.com:[3001-4200]/`
* `52.3[0-12].102.*/*`

## Configuration Breakdown

Below is a list of each of the sections of the config file with a short description of each of them.

* `servlet`: Allows configuration of the behaviour of the servlet and the connections it establishes
  * `threading`: Options regarding thread pooling and scheduling
    * `acceptorPoolSize`: How many threads to allocate to the acceptor pool
    * `handlerPoolSize`: How many threads to allocate to the handler pool
    * `classMatcherPoolSize`: How many threads to allocate to the matching classes in the HandlerResolver
    * `schedulingPolicy`: What type of scheduling policy to use for the thread pools. Can be one of `ABORT`, `CALLER_RUNS`, `DISCARD_OLDEST` or `DISCARD`
  * `connections`: How connections are handled and what properties they can have in terms of liveness and buffering
    * `acceptorQueueSize`: Size of the acceptor queue
    * `handlerQueueSize`: Size of the handler queue
    * `dropAfter`: How long in milliseconds a socket connection should be dropped after if idle and has not received an `EOT` (-1)
    * `readerBufferSize`: Size of the read buffer for a socket
    * `writeBufferSize`: Size of the write/send buffer for a socket
  * `messages`: Properties of how HTTP(S) messages are handled
    * `maxBodySize`: How large accepted HTTP(S) body sizes can be
  * `binding`: Host configurations for th proxy
    * `host`: Hostname to use on the local machine
    * `port`: Port to use on the local machine
* `policies`: Rules about how the proxy should behave with regards to data and URLs
  * `enforcement`: How the proxy should go about enforcing behaviour
    * `whitelistBehaviour`: What action set should be taken for whitelists
      * `ip`: Whether to act as a whitelist or blacklist for IP rule set entries
      * `url`: Whether to act as a whitelist or blacklist for URL rule set entries
    * `allowRedirects`: Whether URL redirects should be allowed to happen away from the bound domain
  * `rulesets`: A set of rules that describe how URL and IP entries are treated
    * `rule set`: A rule for an UP or URL
      * `type`: Whether this rule applies to an IP or URL
      * `isWildcard`: Whether this rule should support regex patterns
      * `pattern`: IP or URL pattern to action against
  * `textReplacements`: A set of replacement schemas to enact on HTML text nodes
    * `replacement`: The pattern that should be replaced with a given string
      * `from`: Pattern to match against
      * `to`: String to replace a match against the 'from' pattern
  * `linkReplacements`: A set of replacement schemas to enact on HTML link nodes (`<a>`, `<img>`, `<link>`, etc.)
    * `replacement`: The pattern that should be replaced with a given string
      * `from`: Pattern to match against
      * `to`: String to replace a match against the 'from' pattern
* `target`: The target of the proxy to forward requests/responses to and from
  * `host`: Hostname of the target
  * `port`: Port of the target
  
### Scheduling Policy

To expand on the enum values that the `servlet.threading.schedulingPolicy` config field can take... these are Java's
built-in scheduling methodologies that can be used out of the box with executors. Below is an explanation of what each
of them mean in terms of use in a thread pool executor.

| **Policy**         	| **Summary**                                                                                                                                                                                                                                                                                                                                                                 |
|--------------------	|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------	|
| `ABORT`            	| A handler for rejected tasks that throws a RejectedExecutionException.                                                                                                                                                                                                                                                                                                      |
| `CALLER_RUNS`      	| A handler for rejected tasks that runs the rejected task directly in the calling thread of the execute method, unless the executor has been shut down, in which case the task is discarded.<br>You may be familiar with this use of this policy with models such as eat-what-you-kill in libraries such as Jetty (See more here <https://webtide.com/eat-what-you-kill/>). 	|
| `DISCARDED_OLDEST` 	| A handler for rejected tasks that discards the oldest unhandled request and then retries execute, unless the executor is shut down, in which case the task is discarded.                                                                                                                                                                                                    |
| `DISCARD`          	| A handler for rejected tasks that silently discards the rejected task.                                                                                                                                                                                                                                                                                                      |

## Resource Matching

This proxy (and by reasonable extension, HTTP servlet) handles requests/responses based on how a given set of resources 
and endpoints within, match against the HTTP message. There is however, the sequence of matching requests is important in
that it determines how handlers are prioritised and invoked.

The ordering is based on the structure of an HTTP message, from top down. This results in matching by the following
ordering:

1. Message type: Whether the message is a request or response
1. Path: The first line of a message that is read, called the start line, contains the path. THis is the same regardless of it being a request or response
2. Media type: In order of unique information present, the `Content-Type` header determines what type of data is being sent, and as such is second as a matcher as it is a header

Note that for each of these matching stages, partial matches are included in valid matcher sequences. By partial matches,
it means that regex based patterns or portions of a pattern (i.e. the start of a path) will be matched and included as
a valid match if they come before a full match.