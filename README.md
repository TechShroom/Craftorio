Craftorio
==========
Craftorio hopes to emulate the entirety of Factorio in Minecraft.

Perhaps loading Lua mods will even be supported at some point.
- [![Build Status](https://travis-ci.org/TechShroom/Craftorio.svg?branch=master)](https://travis-ci.org/TechShroom/Craftorio)

Developing:
----------
Setup is slightly different depending on what system and IDE you use.
This assumes you know how to run gradle commands on your system.
The base command, `./gradlew` being used below is for POSIX-based systems. For windows, this would simply change to `gradlew`.
Of course, if you don't need to use the wrapper (as in, you have Gradle installed on your system), you can simply go right to `gradle`.


1. Clone repository to an empty folder.
2. `cd` to the repository (folder where `src` and `resources` are located).
3. Run `./gradlew setupDecompWorkspace` to set up an environment.
4. Run `./gradlew eclipse` or `./gradlew idea` appropriately.
5. Open your IDE using the generated files
6. Edit, run, and debug your new code.
7. Once it's bug free and working, you may submit it as a PR to the main repo.
