# Java-HTTP-Proxy
A HTTP proxy written in C with link and ref reformatting

## Overview

## Building

## Usage

### VM Arguments

* `-Dconfig.path=<PATH>`: Specify the path to the `config.json` file. Defaults to `./config.json`

## Tests

## File structure

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
			"schedulingPolicy": "FIFO"
		},
		"connections": {
			"acceptorQueueSize": 10,
			"handlerQueueSize": 10,
			"dropAfter": 30000,
			"dropOnFailedDNSLookup": false,
			"readerBufferSize": 1024
		},
		"messages": {
			"maxBodySize": 65535,
			"dropOnMalformed": true
		},
		"binding": {
			"host": "localhost",
			"port": 3000
		},
		"cacheSize": 25
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
				"pattern": "http://console.[dev,prod].test.com/"
			}
		]
	},
	"target": {
		"host": "www.bom.gov.au",
		"port": 80
	}
}
```

JSON Schema describing the configuration file

```json
{
	"$schema": "http://json-schema.org/draft-04/schema#",
	"type": "object",
	"properties": {
		"servlet": {
			"type": "object",
			"properties": {
				"threading": {
					"type": "object",
					"properties": {
						"acceptorPoolSize": {
							"type": "integer"
						},
						"handlerPoolSize": {
							"type": "integer"
						},
						"schedulingPolicy": {
							"type": "string"
						}
					},
					"required": [
						"acceptorPoolSize",
						"handlerPoolSize",
						"schedulingPolicy"
					]
				},
				"connections": {
					"type": "object",
					"properties": {
						"acceptorQueueSize": {
							"type": "integer"
						},
						"handlerQueueSize": {
							"type": "integer"
						},
						"dropAfter": {
							"type": "integer"
						},
						"dropOnFailedDNSLookup": {
							"type": "boolean"
						},
						"readerBufferSize": {
							"type": "integer"
						}
					},
					"required": [
						"acceptorQueueSize",
						"handlerQueueSize",
						"dropAfter",
						"dropOnFailedDNSLookup",
						"readerBufferSize"
					]
				},
				"messages": {
					"type": "object",
					"properties": {
						"maxBodySize": {
							"type": "integer"
						},
						"dropOnMalformed": {
							"type": "boolean"
						}
					},
					"required": [
						"maxBodySize",
						"dropOnMalformed"
					]
				},
				"binding": {
					"type": "object",
					"properties": {
						"host": {
							"type": "string"
						},
						"port": {
							"type": "integer"
						}
					},
					"required": [
						"host",
						"port"
					]
				},
				"cacheSize": {
					"type": "integer"
				}
			},
			"required": [
				"threading",
				"connections",
				"messages",
				"binding",
				"cacheSize"
			]
		},
		"policies": {
			"type": "object",
			"properties": {
				"enforcement": {
					"type": "object",
					"properties": {
						"whitelistBehaviour": {
							"type": "object",
							"properties": {
								"ip": {
									"type": "string"
								},
								"url": {
									"type": "string"
								}
							},
							"required": [
								"ip",
								"url"
							]
						},
						"allowRedirects": {
							"type": "boolean"
						}
					},
					"required": [
						"whitelistBehaviour",
						"allowRedirects"
					]
				},
				"rulesets": {
					"type": "array",
					"items": [
						{
							"type": "object",
							"properties": {
								"type": {
									"type": "string"
								},
								"isWildcard": {
									"type": "boolean"
								},
								"pattern": {
									"type": "string"
								}
							},
							"required": [
								"type",
								"isWildcard",
								"pattern"
							]
						}
					]
				}
			},
			"required": [
				"enforcement",
				"rulesets"
			]
		},
		"target": {
			"type": "object",
			"properties": {
				"host": {
					"type": "string"
				},
				"port": {
					"type": "integer"
				}
			},
			"required": [
				"host",
				"port"
			]
		}
	},
	"required": [
		"servlet",
		"policies",
		"target"
	]
}
```

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
The available operators are:

* Integer range: `[3-45]`
* Enumerated set: `[over,z35,mailto]` (`~` is a special character indicating none or empty)
* Any values: `*`

You can use this at any point within an IP or URL, for example:

* `http[~,s]://some.[prod,dev].site.com:[3001-4200]/`
* `52.3[0-12].102.*/*`

### Servlet Config

#### Threading

Todo

#### Connections

Todo

#### Packets

Todo

#### Cache

Todo