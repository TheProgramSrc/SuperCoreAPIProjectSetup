# Why this project setup?
One day I was tired of creating the new pom.xml file and looking for the shade plugin and confiure it, 
and search for the latest version of every dependency, so I created this, a tool to create a Plugin Project with SuperCoreAPI.

# How to use?
> YOU MUST HAVE Java INSTALLED IN YOUR SYSTEM.
1. Download the latest version of this project
2. Execute the following command: <br>
`java -jar ProjectSetup.jar --projN PROJECT_NAME --projV PROJECT_VERSION --projP PROJECT_PACKAGE --path PROJECT_PATH`
3. Replace the variables:
    * `PROJECT_NAME`: Is the name of your project that will be used as ArtifactID, In the plugin.yml and bungee.yml name, the project package and the main classes.
    * `PROJECT_VERSION`: The version of your project. If you don't know which version to use we recommend you the [semantic versioning](https://semver.org/)
    * `PROJECT_PACKAGE`: The group id of your project. You don't need to create the full package, just the start, like "xyz.theprogramsrc" because then the tool will 
    create the project package using the given package + the project name, if we use as example the project name "MyPlugin" and the package "xyz.theprogramsrc", the result
    would be `xyz.theprogramsrc.myplugin`.
    * `PROJECT_PATH`: Must be a folder which will contain the Project Folder, it can be your desktop because the tool will create another folder inside the given path with 
    the name of the project
4. Wait until the tool generate your project.

# Please help me!!
If you need any help join [our discord](https://go.theprogramsrc.xyz/discord) or execute the command --discord like this: `java -jar ProjectSetup.jar --discord`
