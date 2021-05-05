# Java-HTTP-Proxy

HTTP proxy written in Java with link and ref reformatting

## Maven Installation

This project is managed via Maven, and built with the surefire plugin. In order to build the jar file
you'll need to have maven installed.

If you haven't already got it installed, then you can run one of the makefile targets depending on your environment:

* Mac OS X: `$> make install_maven_darwin`
* Linux (binary): `$> make install_maven_linux_binary`
* Linux (apt): `$> make install_maven_linux_apt`

*Note:* If you are using windows, then it's not as simple to install maven. I would suggest that you run this on a unix system,
but if you want to install it on windows, a tutorial can be found here <https://www.javatpoint.com/how-to-install-maven>


## Building

Once you have maven installed, run the following to build the project to a jar file:

```shell
$> make build_jar
```

Alternatively, if you want to build it manually then you can run the following:

```shell
$> mvn install
$> mvn package
$> mv target/HTTP-Proxy-0.1.0-shaded.jar HTTP-Proxy-0.1.0.jar
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
$> java -jar -Dconfig.path=resources/config.json -Dlog4j.configurationFile=logback.xml HTTP-Proxy-0.0.1-shaded.jar
```

### Jar Arguments

* `-Dconfig.path=<PATH>`: Specify the path to the `config.json` file. Defaults to `./config.json`
* `-Dlog4j.configurationFile=<PATH>`: Specify the path to the `logback.xml` file to configure log4j

## Tests

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
define whether to use the whitelist for IPs and/or URLs, and whether to block everything if it doesnt match.

Note that in the case of making `block_otherwise` a `true` value, this causes the proxy to ignore the blacklists
for IPs or URLs if they are `true`.

The `allow_redirects` property specifies whether inplace redirects within URLs should be honoured or not. In the case
that this is falsy, the proxy will not follow redirects from any connections.

#### Whitelist and Blacklist

The whitelist and blacklist behave the same in the way they act. Both have subfields `ip` and `url` allow a list of
values to acknowledge when scrutinising given resources. These can be wildcarded to support limit regex statements.

##### IP Addresses

Specifying an IP address should be in standard IPv4 or IPv6 format. These will be validated according and ignored if invalid.
If a given rule is invalid then a log entry will be created at the DEBUG level.

##### URLs

Creating a URL entry is done in standard format including schema. These rule sets will be applied to both the resource
URLs and also inplace links.

##### Wildcard regex

When specifying IPs or URLs, you can also use limited regex to encompass a range of values to avoid needless repetition.
You can use any standard Java regex operator syntax.

You can use this at any point within an IP or URL, for example:

* `(http(s)?://)?some.[(prod)|(dev)].site.com:[3001-4200]/`
* `52.3[0-12].102.*/*`

### Servlet Config

#### Threading

Todo

#### Connections

Todo

#### Messages

Todo

#### Binding

#### Cache

Todo

### Policies Config

#### Enforcement

Todo

#### Rule sets

Todo

#### Text Replacements

Todo

#### Link Replacements

### Target

Todo