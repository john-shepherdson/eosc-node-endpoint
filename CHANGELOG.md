# Changelog

## [1.1.0](https://github.com/madgeek-arc/eosc-node-endpoint/compare/v1.0.0...v1.1.0) (2026-05-28)


### Features

* add HTTP client for resolving Node Registry entries ([43e6c28](https://github.com/madgeek-arc/eosc-node-endpoint/commit/43e6c28165de0b0afbf6fd74363c93086f7338cb))


### Bug Fixes

* add java buildpack to build image ([1f220e2](https://github.com/madgeek-arc/eosc-node-endpoint/commit/1f220e2fcc1d7f1a8892209cd8dae8b9fc931712))
* **client:** accept more JSON response media types ([bfd5a56](https://github.com/madgeek-arc/eosc-node-endpoint/commit/bfd5a569e41442d94e6f4afebed640007675ef61))

## [1.0.0](https://github.com/madgeek-arc/eosc-node-endpoint/compare/v0.0.1...v1.0.0) (2026-05-06)


### ⚠ BREAKING CHANGES

* split into multi-module Maven project with model and client libraries ([#2](https://github.com/madgeek-arc/eosc-node-endpoint/issues/2))

### Features

* add better exception handling for read / write failures ([712c1d6](https://github.com/madgeek-arc/eosc-node-endpoint/commit/712c1d623f80d2f3e855800363355f9460e84a1f))
* **auth:** implement authentication and authorization ([ac96308](https://github.com/madgeek-arc/eosc-node-endpoint/commit/ac9630870eea1e7849e89774b194407dad04ceb1))
* **endpoint:** add TTL cache for file-backed capabilities ([400d541](https://github.com/madgeek-arc/eosc-node-endpoint/commit/400d5415b31fe07e4debc051a793571b729b8fb9))
* init project ([9c0cc56](https://github.com/madgeek-arc/eosc-node-endpoint/commit/9c0cc56ed3d0f9a35d7a271a773e7aa42ab8cac4))
* read capabilities from file ([257e6be](https://github.com/madgeek-arc/eosc-node-endpoint/commit/257e6be72d2a3c4ccd7d606ccbb4cb3d9103ff19))
* split into multi-module Maven project with model and client libraries ([#2](https://github.com/madgeek-arc/eosc-node-endpoint/issues/2)) ([e434f8a](https://github.com/madgeek-arc/eosc-node-endpoint/commit/e434f8a5a145abb3921c4531ae50f47b17345bc5))
* update capabilities functionality ([ed2528e](https://github.com/madgeek-arc/eosc-node-endpoint/commit/ed2528e0df3858a811f9ee5b2c15cd73bdef966a))


### Bug Fixes

* NPE on protocol and status ([4a67760](https://github.com/madgeek-arc/eosc-node-endpoint/commit/4a6776051060dbf86f83d280eaed4c44337e72a0))
* preserve null protocol / status metadata in Capability ([3afc984](https://github.com/madgeek-arc/eosc-node-endpoint/commit/3afc9846bdd93d51bd5d30e245a8528162623856))


### Documentation

* add changelog ([3678dde](https://github.com/madgeek-arc/eosc-node-endpoint/commit/3678dde709ea683f105558f247dc09291167cc20))
* add documentation ([2e2a17e](https://github.com/madgeek-arc/eosc-node-endpoint/commit/2e2a17ebd2d1a07a7e11c88781f5d5b10acc8f47))
* add LICENSE, CONTRIBUTING, and CODE_OF_CONDUCT files ([4670fd2](https://github.com/madgeek-arc/eosc-node-endpoint/commit/4670fd2ecafd14ca26b07a4c35a8c9552c0cac31))
* **readme:** update property documentation ([afc66d1](https://github.com/madgeek-arc/eosc-node-endpoint/commit/afc66d1a4caa7054b40901979321df4c9f54eb3e))


### Miscellaneous Chores

* release 1.0.0 ([3f2cb6b](https://github.com/madgeek-arc/eosc-node-endpoint/commit/3f2cb6bafb98194bf11a59f710cebe4e6c0f1c62))
