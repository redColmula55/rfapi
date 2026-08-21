# Reservoir Fluid API

English | [简体中文](README-zh.md)

## Description
This is a lib mod intends to make creating fluids in Fabric easier.

* Mod ID: reservoir-api
* Old name: Fluid Lib

* Note: This mod is still in beta and maybe unstable and buggy. Use under your own risk.

* If you find any bug, please report it in the issues page. Please describe how the bug was triggered and what it affects so that we can patch it as fast as possible. We accept Chinese / English issues.

## Features
### Registry
* Customized fluid registry
* Less overrides through `ExtendedFluid`
* Customize properties with a single `FluidSettings` object
### Behaviors & Interactions
* Supports flowing upwards
* New data-driven fluid reaction system
* Able to create different types of bucket and make them stackable
* Entity movement no longer requires tags
* Customizable submerging fog
* More fluid related features...

## Dependencies
* Minecraft version: 1.20.1
* Fabric version: 0.15.0 or greater
* Required Dependency Mods: Fabric API, [Cloth Config API](https://github.com/shedaniel/cloth-config)

## Developing Guide
### CurseMaven
1. Add the CurseMaven repo by following the official tutorial
2. Optional: Download the sources jar for javadoc supports
3. Add follow lines under `dependencies` in your `build.gradle` file:
   `modImplementation 'curse.maven:rfapi-1649310:VERSIONID'`
4. Replace the `VERSIONID` with ID of version to use
5. Refresh the project
### Modrinth Maven
1. Add the Modrinth Maven repo by following the official tutorial
2. Optional: Download the sources jar for javadoc supports
3. Add follow lines under `dependencies` in your `build.gradle` file:
   `modImplementation 'maven.modrinth:rfapi:VERSION'`
4. Replace the `VERSION` to a proper version number
5. Refresh the project
### Manually
1. Create a `libs` folder under your project base dir
2. Put the mod jar file into the folder and rename it to `rfapi.jar`
3. Optional: Download the sources jar for javadoc supports
4. Add follow lines under `dependencies` in your `build.gradle` file:
   ` modImplementation(files('libs/rfapi.jar'))`
5. Add Cloth Config API to `modRuntimeOnly` or `modImplmention`(recommended if your mod needs a config api) gradle config
6. Refresh the project

## License
This project is licensed under [LGPL-2.1](LICENSE) or later.  
&copy; 2026 redColmula55  
Not an official _Minecraft_ product. Not approved by or associated with _Mojang_.