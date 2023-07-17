# Quilt Mappings

Quilt Mappings is a set of open, accurate, and easy-to-understand Minecraft mappings
under the Creative Commons Zero license. Quilt Mappings seeks to provide the best of
both worlds in terms of mapping quality: the accuracy and completeness of Mojang's
official mappings, and the simplicity and set-in-stone conventions of its basis, the
[Yarn](https://github.com/fabricmc/yarn) project.

Quilt Mappings exists to provide a stable set of mappings for everyone to contribute
and improve upon. We provide mappings for all versions of Minecraft from 1.17 onwards.
To see what version is currently being targeted for updates, check the current Git branch's name.

## Using QM

To use Quilt Mappings in your mod, you can use either [quilt-loom](https://github.com/quiltmc/quilt-loom)
for Quilt mods or use our Intermediary publication on [Fabric's loom](https://github.com/fabricmc/fabric-loom)
for Fabric mods. We recommend cloning the [Quilt Template Mod](https://github.com/quiltmc/quilt-template-mod)
to start a new mod with QM, and you can also add this block to your `build.gradle` inside the `dependencies` block:

```groovy
mappings("org.quiltmc:quilt-mappings:[version]:intermediary-v2")
```

Note that on `fabric-loom`, you'll have to add the following to your `repositories` block as well, to tell gradle
where to find the QM publication:

```groovy
maven { url = "https://maven.quiltmc.org/repository/release/" }
```

QM versions are named after the Minecraft version they target, followed by `+build.x`,
with `x` being a number that increments with each new build of QM for that Minecraft version.
For example, `1.18.2+build.1` is the first build of QM for Minecraft `1.18.2`. You can see additional
information and see what the latest QM build for each Minecraft version is with [LambdAurora's import tool](https://lambdaurora.dev/tools/import_quilt.html).

## Contributing to QM

When contributing names, the primary goal you should keep in mind is their intuitiveness:
if you read this name with no context other than its declaration, would you know what it does?
If not, the name either needs to be rethought or given javadocs if a good name can't be found.

We recommend coming up with original names as often as possible. In the case that something is too
difficult to understand or name, check Mojang and Yarn's mappings for inspiration! Always spend some
time thinking of original names before you make the decision to match theirs. For original names, it's
best to check Mojang and Yarn anyway to make sure your name is correct - it's easy to make mistakes!

In order to get started with making your first pull request, 
please see the guide in [CONTRIBUTING.md](CONTRIBUTING.md#guide-pull-requests)!

## Gradle Tasks

QM has a set of helpful Gradle tasks to make mapping easier, including:

### `mappings`
Setup, download, and launch the latest version of [Enigma](https://github.com/QuiltMC/Enigma) automatically configured to use the merged jar and the mappings.

Compared to launching Enigma externally, the gradle task adds a name guesser plugin that automatically maps enums and a few constant field names.

### `mappingsUnpicked`
Same as above, but unpicks the constants and launches Enigma with them. Can be a little bit slower to get going.


### `build`
Build a GZip'd archive containing a tiny mapping between official (obfuscated), [hashed-mojmap](https://github.com/QuiltMC/mappings-hasher), and Quilt Mappings names ("named") and packages enigma mappings into a zip archive.

### `mapNamedJar`
Builds a deobfuscated jar with Quilt mappings and automapped fields (enums, etc.). Unmapped names will be filled with [hashed-mojmap](https://github.com/QuiltMC/mappings-hasher) names.

### `decompileQuiltflower`
Decompile the mapped source code. **Note:** This is not designed to be recompiled.

### `downloadMinecraftJars`
Downloads the client and server Minecraft jars for the current Minecraft version to `.gradle/minecraft`

### `mergeJars`

Merges the client and server jars into one merged jar, located at `VERSION-merged.jar` in the project directory
where `VERSION` is the current Minecraft version.