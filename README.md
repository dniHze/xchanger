# XChanger
[![CircleCI](https://circleci.com/gh/dniHze/xchanger.svg?style=svg)](https://app.circleci.com/github/dniHze/xchanger/pipelines) [![APK](https://img.shields.io/badge/-Download%20APK-brightgreen)](https://github.com/dniHze/xchanger/releases)

<img src="/art/icon.png" height="200" width="200">

Test assignment project for Revolut
## About
**Architecture**: MVI / MVVM<br/>
**DB**: Room<br/>
**Network Stack**: Retrofit2 / OkHttp4<br/>
**Min API**: 21<br/>
**Tests**: Unit Tests, Instrumented Test (No UI tests)
## Features
* Edge-to-edge drawing with proper insets support
* Currency digits after zero ([ISO 4217](https://en.wikipedia.org/wiki/ISO_4217))
* Offline Mode
* Screen rotation (but not process death)
## Issues to solve
* ~~No proper input filters for sum entered by user. Currently you allowed to type `00000123.4567`~~.
Added "proper" input support to master branch. The implementation locking your cursor,
but give you the full advantage over numeric input representation.
* ~~No support for entered sum on process death ~~. Fixed in `0.1.6`.
* RecyclerView layout time takes long on adapter update (due to `wrap_content` EditText, probably). 
Could be solved with custom view but it will complex the support
## Links
**Test task**: [Google Docs](https://docs.google.com/document/d/13Ecs3hhgZJJLsugNUwZPUn_9gsqzwH80Bb-1CRbauTQ/edit)<br/>
## Download
You could find the latest release and debug APKs on the "**Releases**" [page](https://github.com/dniHze/xchanger/releases).
## License
```
Copyright 2020 Artyom Dorosh

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
