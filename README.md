# LycheeUpload

A java tool that can be used to upload pictures to an instance of the picture sharing server [Lychee](https://github.com/LycheeOrg/Lychee), create new albums, edit access rights to albums and a bit more.

## Usage

Best way to import the code would probably be to load it as a [submodule like decribed here](https://git-scm.com/book/en/v2/Git-Tools-Submodules).
You can then use it in your project by calling the LycheeUploaderHttp-class.
If you just want to copy the code without making it a submodule make sure that all dependencies specified in the pom.xml are available.

The CLI application does not yet work, so you will need to write your own if you need one (pull requests will be gladly accepted) or wait until I get to implementing it.

### Thanks
Thanks to user [finghine](https://github.com/finghine) for pointing me in the right direction with his code from his [upload tool for Lychee](https://github.com/finghine/lychee-upload-tool)!