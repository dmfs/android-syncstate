# android-syncstate

__An XML layer for the SyncState Contract in Android__


Android provides a simple mechanism to sync applications to store additional meta data for each account and authority, called SyncStateContract. Unfortunately a sync adapter can only store a single blob, so the sync app has to undertake its own steps to store structured data.

This library provides a simple way to read and store structured sync state data.

## Requirements

* Android SDK Level 8 or higher
* [xmlobjects](https://github.com/dmfs/xmlobjects)

## License

Copyright (c) Marten Gajda 2015


Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

