# LycheeUpload

A java tool that can be used to upload pictures to an instance of the picture sharing server [Lychee](https://github.com/LycheeOrg/Lychee), create new albums, edit access rights to albums and a bit more.

## Usage in own projects

Ways to import the code would for example be to

* Import it using [JitPack](https://jitpack.io/) together with a build/dependency management system (currently supported by JitPack: [Gradle](https://gradle.org/), [Maven](https://maven.apache.org/), [Leiningen](https://leiningen.org/), [SBT](https://www.scala-sbt.org/))
* load it as a [git-submodule like described here](https://git-scm.com/book/en/v2/Git-Tools-Submodules).

You can then use it in your project by creating a LycheeUploaderHttp-object. Check the JavaDoc in the source files for details.

If you just want to copy the code without making it a submodule or JitPack make sure that all dependencies specified in the pom.xml are available.

## CLI Usage

The CLI apllication does not support all operations, that are available in the LycheeUploaderHttp-Class.

```
java -jar lycheeUpload.jar [-u <filepath> <albumId> | -l ]
  --user <username> --password <password> --server <serverAddress>

Options:
  -u <filepath> <albumId> to upload an image\n
  -l to list albums available on the server
```

### Thanks
Thanks to user [finghine](https://github.com/finghine) for pointing me in the right direction with his code from his [upload tool for Lychee](https://github.com/finghine/lychee-upload-tool)!
