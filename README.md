# Plugin to integrate Marathon with any HTTPS proxy

This plugin allows to delegate Marathon authentication to an external
frontend proxy which sets two (user configurable) headers with the login
name of the user and the groups he / she belongs to. In order to avoid
spoofing you must make sure that only the frontend can reach Marathon.

The following environment variables can be defined when running Marathon
with this plugin, to configure its behavior:

- **MARATHON_SSO_LOGIN_HEADER**: the name of the header to use to determine
  the user id.
- **MARATHON_SSO_GROUP_HEADER**: the name of the header to determine the `;`
  separated list of groups the user belongs to.
- **MARATHON_SSO_ADMIN_GROUP**: The name of the admin group, whose users
  have super powers.
- **MARATHON_SSO_VALID_GROUP_PREFIX**: A prefix to match by the groups
  which the user belongs to, so that non-marathon related groups are ignored.

Based on the code from:

https://github.com/mesosphere/marathon-example-plugins
