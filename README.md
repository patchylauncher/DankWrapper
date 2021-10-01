# DankWrapper
Similar to Mojang's [LaunchWrapper](https://github.com/Mojang/LegacyLauncher) project, which is used to launch Minecraft 1.5.2 and below in the new launcher. Applies mostly the same patches to said versions
## Differences
Unlike the official Mojang solution, this works on Java 9 and above. Currently only tested on Release 1.5.2, but anything that doesnt use the Alpha or Beta Tweakers should work fine.
## Note
This was designed for danklauncher, so the way its used is incompatible with the official launcher as of now. You must have all the game libraries in a single folder, and pass that folder and minecraft.jar in the classpath for it to work.

Example:
```
-cp /stuff/libs/*;/stuff/minecraft.jar
```