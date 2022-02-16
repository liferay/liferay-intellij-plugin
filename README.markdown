# Liferay IntelliJ Plugin

## Install from jetbrains offical link
https://plugins.jetbrains.com/plugin/10739-liferay-intellij-plugin

## Confiugre plugin develop environment
This project need Java JDK version 11.

## Building from source
If you would like to build from source, use this following command:

```
$ ./gradlew clean build
```

Once it finishes the locally built the IntelliJ Idea Plugin will be located here:

```
build/distributions/liferay-intellij-plugin-<version>.zip
```

You can install this using _Preferences > Plugins > Install plugin from disk... > Point to newly built zip file_

## Key features

### Liferay Workspace

- Create a Liferay Workspace Project from Start menu > click *Create New Project* or click *File* > *New* > *Project*

- Choose *Liferay Workspace* from left list

- Click *Next* button

- Type your Liferay Workspace *Project Name* and choose a *Project Location* or leave it default.

- Click *Finish* button and there will be one popup show up.

- Click *OK* to finish

### Liferay Server

You must have one Liferay Workspace Project to do the following steps:

- Right click on the root of your Liferay Workspace Project

- Click Liferay > InitBundle

- Then the gradle will run *initBundle* task

- It will take several minutes to download the latest Liferay Server from remote if you don't have the local cache

- Click *Edit configruration...* on the right top corner

- Click the plus icon

- Choose *Liferay Server*

- You can leave the config values by default or change to what you want

- Click *OK* Bundle

- After you add a new *Liferay Server*, you can *start* or *debug* it

### Liferay Module

You must have one Liferay Workspace Project to do the following steps:

- Right click on existing Liferay Workspace Project and choose *New* > *Liferay Module*

- Choose *Liferay Modules*

- Depends on what template you choose, you can type *Package Name*, *Class Name* and *Service Name*

- Click *Next* Button

- Type *Project Name* and the *location* is not able to customize.

- Click *Finish* Button

### Deployment

After you get the Liferay Modules you can deploy them to running or debugging Liferay Server:

- Right Click on your *Liferay Module*

- Choose Liferay > Deploy

- When you see the log showing in the console view, you get your module successful deploying

### Watch

After you get the Liferay Modules you can *watch* them on running or debugging Liferay Server:

- Right Click on your *Liferay Module* or the folders like modules or the root of Liferay Workspace.

- Choose Liferay > Watch

- When you see the gradle console output and the deplog log showing in the console view, you get your module successful deploying.

- You could keep the watch task running and it will listen on the changes of your coding.

- You could click red button to cancel the running watch task in Gradle Task view.

### Better Editors for Liferay Files

- service.xml

- custom-sql/default.xml

- portlet-model-hints.xml

- Liferay Taglib Support for Jsp files(Ultimate Only)

- etc.

## License

This library, *Liferay IDE*, is free software ("Licensed
Software"); you can redistribute it and/or modify it under the terms of the [GNU
Lesser General Public License](http://www.gnu.org/licenses/lgpl-2.1.html) as
published by the Free Software Foundation; either version 2.1 of the License, or
(at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; including but not limited to, the implied warranty of MERCHANTABILITY,
NONINFRINGEMENT, or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
Public License for more details.

You should have received a copy of the [GNU Lesser General Public
License](http://www.gnu.org/licenses/lgpl-2.1.html) along with this library; if
not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
Floor, Boston, MA 02110-1301 USA