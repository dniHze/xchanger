[![CircleCI](https://circleci.com/gh/dniHze/revolut-test-assignment.svg?style=svg&circle-token=05cd8fe4e3269f9094d78c0ae567a231d0330f3f)](https://circleci.com)

# XChanger
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
* No proper input filters for sum entered by user. Currently you allowed to type `00000123.4567`
* No support for entered sum on process death 
(could be added with SavedStateHandle to ViewModel and StateMachine init logic update)
* RecyclerView layout time takes long on adapter update (due to `wrap_content` EditText, probably). 
Could be solved with custom view but it will complex the support
## Links
**Test task**: [Google Docs](https://docs.google.com/document/d/13Ecs3hhgZJJLsugNUwZPUn_9gsqzwH80Bb-1CRbauTQ/edit)<br/>
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
