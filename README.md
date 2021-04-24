# Java-HTTP-Proxy
A HTTP proxy written in C with link and ref reformatting

## Overview

## Building

## Usage

## Tests

## File structure

## Configuration

The proxy utilises JSON configuration files to specify the required behaviour it should enact.
There are two main configuration files involed:

* `policies.json`
* `servlet.json`

### Policies

If you are familiar with the JSON Schema "standard" then a Draft 4 vesion can be found below. Otherwise, there is an explaination after it deteailing each of the feilds 

```json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "enforcement": {
      "type": "object",
      "properties": {
        "use_whitelist": {
          "type": "object",
          "properties": {
            "ip": {
              "type": "boolean"
            },
            "url": {
              "type": "boolean"
            },
            "block_otherwise": {
              "type": "boolean"
            }
          },
          "required": [
            "ip",
            "url",
            "block_otherwise"
          ]
        },
        "use_blacklist": {
          "type": "object",
          "properties": {
            "ip": {
              "type": "boolean"
            },
            "url": {
              "type": "boolean"
            }
          },
          "required": [
            "ip",
            "url"
          ]
        },
        "allow_redirects": {
          "type": "boolean"
        }
      },
      "required": [
        "use_whitelist",
        "use_blacklist",
        "allow_redirects"
      ]
    },
    "whitelist": {
      "type": "object",
      "properties": {
        "ip": {
          "type": "array",
          "items": [
            {
              "type": "string"
            }
          ]
        },
        "url": {
          "type": "array",
          "items": [
            {
              "type": "string"
            }
          ]
        }
      },
      "required": [
        "ip",
        "url"
      ]
    },
    "blacklist": {
      "type": "object",
      "properties": {
        "ip": {
          "type": "array",
          "items": [
            {
              "type": "string"
            }
          ]
        },
        "url": {
          "type": "array",
          "items": [
            {
              "type": "string"
            }
          ]
        }
      },
      "required": [
        "ip",
        "url"
      ]
    }
  },
  "required": [
    "enforcement",
    "whitelist",
    "blacklist"
  ]
}
```

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

```json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "threading": {
      "type": "object",
      "properties": {
        "pool_size": {
          "type": "integer"
        },
        "scheduling_policy": {
          "type": "string"
        }
      },
      "required": [
        "pool_size",
        "scheduling_policy"
      ]
    },
    "connections": {
      "type": "object",
      "properties": {
        "drop_after": {
          "type": "integer"
        },
        "drop_on_failed_dns_lookup": {
          "type": "boolean"
        }
      },
      "required": [
        "drop_after",
        "drop_on_failed_dns_lookup"
      ]
    },
    "packets": {
      "type": "object",
      "properties": {
        "max_body_size": {
          "type": "integer"
        },
        "drop_on_malformed": {
          "type": "boolean"
        }
      },
      "required": [
        "max_body_size",
        "drop_on_malformed"
      ]
    },
    "cache_size": {
      "type": "integer"
    }
  },
  "required": [
    "threading",
    "connections",
    "packets",
    "cache_size"
  ]
}
```

#### Threading

Todo

#### Connections

Todo

#### Packets

Todo

#### Cache

Todo