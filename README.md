# Quilt Mappings

Quilt Mappings is a set of open, unencumbered Minecraft mappings, free for everyone to use under the Creative Commons Zero license. The intention is to let 
everyone mod Minecraft freely and openly, while also being able to innovate and process the mappings as they see fit.

To see the current version being targeted, check the branch name!

## Usage
To use Quilt Mappings-deobfuscated Minecraft for Minecraft modding or as a dependency in a Java project, you can use [quilt-gradle](https://github.com/quiltmc/quilt-gradle) Gradle plugin. See [fabric wiki tutorial](https://fabricmc.net/wiki/tutorial:setup) for more information.

To obtain a deobfuscated Minecraft jar, [`./gradlew mapNamedJar`](#mapNamedJar) will generate a jar named like `<minecraft version>-named.jar`, which can be sent to a decompiler for deobfuscated code.

Please note to run the Quilt Mappings build script **Java 16** or higher is required!

## Contributing

Our goal is to provide high-quality names that make intuitive sense. In our experience, this goal is best achieved through contribution of original names, rather than copying names from other mappings projects. While we believe that discussions relating to the names used by other mappings projects can be useful, those names must stand up to scrutiny on their own - we won't accept names on the grounds that they're present in other mappings projects. 

We recommend discussing your contribution with other members of the community - either directly in your pull request, or in our other community spaces. We're always happy to help if you need us!

Please have a look at the [naming conventions](/CONVENTIONS.md) before submitting mappings.

### Getting Started

1. Fork and clone the repo
2. Run `./gradlew mappings` (Linux, macOS) or `gradlew mappings` (Windows) to open [Enigma](https://github.com/QuiltMC/Enigma), a user interface to easily edit the mappings
3. Commit and push your work to your fork
4. Open a pull request with your changes

## Gradle
Quilt Mappings uses Gradle to provide a number of utility tasks for working with the mappings.

### `mappings`
Setup and download and launch the latest version of [Enigma](https://github.com/QuiltMC/Enigma) automatically configured to use the merged jar and the mappings.

Compared to launching Enigma externally, the gradle task adds a name guesser plugin that automatically maps enums and a few constant field names.

### `mappingsUnpicked`
Same as above, but unpicks the constants and launches Enigma with them. Can be a little bit slower to get going.


### `build`
Build a GZip'd archive containing a tiny mapping between official (obfuscated), [hashed-mojmap](https://github.com/QuiltMC/mappings-hasher), and Quilt Mappings names ("named") and packages enigma mappings into a zip archive.

### `mapNamedJar`
Builds a deobfuscated jar with Quilt mappings and automapped fields (enums, etc.). Unmapped names will be filled with [hashed-mojmap](https://github.com/QuiltMC/mappings-hasher) names.

### `decompileCFR`
Decompile the mapped source code. **Note:** This is not designed to be recompiled.

### `download`
Downloads the client and server Minecraft jars for the current Minecraft version to `.gradle/minecraft`

### `mergeJars`
Merges the client and server jars into one merged jar, located at `VERSION-merged.jar` in the mappings directory where `VERSION` is the current Minecraft version.
