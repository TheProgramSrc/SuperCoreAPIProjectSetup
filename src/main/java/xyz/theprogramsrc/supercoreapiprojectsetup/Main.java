package xyz.theprogramsrc.supercoreapiprojectsetup;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.json.JSONObject;
import org.json.XML;
import xyz.theprogramsrc.supercoreapiprojectsetup.http.HttpRequest;

import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

@SuppressWarnings("ResultOfMethodCallIgnored") // Why we want warnings about File#mkdirs or File#createNewFile ?
public class Main{

    private static boolean validateDiscord(String[] args){
        for (String arg : args) {
            if(arg.equalsIgnoreCase("--discord")){
                return true;
            }
        }

        return false;
    }

    private static String
            scapiVersion = "5.0.0-SNAPSHOT", // <-- Update if there is a new SCAPI Update
            spigotVersion = "1.17-R0.1-SNAPSHOT",  // <-- Update if there is a new spigot version
            bungeeVersion = "1.17-R0.1-SNAPSHOT"; // <-- Update if there is a new bungee version

    public static void main(String[] args) {
        if(validateDiscord(args)){
            // Open discord link in browser
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if(desktop != null && desktop.isSupported(Desktop.Action.BROWSE)){
                try {
                    System.out.println("Opening link to discord in your browser...");
                    desktop.browse(new URL("https://go.theprogramsrc.xyz/discord").toURI());
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }else{
                System.out.println("This environment does not support the browser action! Please paste the following link in your browser: https://go.theprogramsrc.xyz/discord");
            }
            return;
        }


        if(!requestVersions()) return;
        ArgumentParser parser = generateArgs();
        if(args.length <= 4){
            parser.printHelp();
            return;
        }

        Namespace ns;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
            return;
        }

        String projectName = ns.getString("projectName");
        String projectVersion = ns.getString("projectVersion");
        String projectPackage = ns.getString("projectPackage");
        String fullPath = ns.getString("path");
        if(is_null(projectName) || is_null(projectVersion) || is_null(projectPackage) || is_null(fullPath)){
            parser.printHelp();
        }else{
            projectName = projectName.replaceAll(" ", "_");
            File finalFolder = new File(fullPath);
            if(!finalFolder.exists()){
                System.out.println("The given path doesn't exists!");
            }else{
                if(!finalFolder.isDirectory()){
                    System.out.println("The given path is not a directory!");
                }else{
                    File projectFolder = new File(finalFolder, projectName.replaceAll(" ", "_"));
                    if(!projectFolder.exists()) projectFolder.mkdirs();
                    File pomXml = new File(projectFolder, "pom.xml");
                    try{
                        if(!pomXml.exists()) pomXml.createNewFile();
                        String data = generateXml();
                        data = data.replace("{PROJECT_NAME}", projectName)
                                .replace("{PROJECT_PACKAGE}", projectPackage)
                                .replace("{PROJECT_VERSION}", projectVersion)
                                .replace("{PROJECT_NAME_LOWER}", projectName.toLowerCase())
                                .replace("{SPIGOT}", spigotVersion)
                                .replace("{BUNGEE}", bungeeVersion)
                                .replace("{SCAPI}", scapiVersion);
                        writeStringToFile(data, pomXml);
                        File srcFolder = new File(projectFolder, "src/main/java/");
                        if(!srcFolder.exists()) srcFolder.mkdirs();
                        File resourcesFolder = new File(projectFolder, "src/main/resources/");
                        if(!resourcesFolder.exists()) resourcesFolder.mkdirs();

                        File pluginYml = new File(resourcesFolder, "plugin.yml"),
                                bungeeYml = new File(resourcesFolder, "bungee.yml");
                        if(!pluginYml.exists()) pluginYml.createNewFile();
                        if(!bungeeYml.exists()) bungeeYml.createNewFile();

                        String plYml = generatePluginYML();
                        plYml = plYml.replace("{PROJECT_NAME}", projectName)
                                .replace("{PROJECT_NAME_LOWER}", projectName.toLowerCase())
                                .replace("{PROJECT_PACKAGE}", projectPackage);
                        String bgYml = generateBungeeYML();
                        bgYml = bgYml.replace("{PROJECT_NAME}", projectName)
                                .replace("{PROJECT_NAME_LOWER}", projectName.toLowerCase())
                                .replace("{PROJECT_PACKAGE}", projectPackage);
                        writeStringToFile(plYml, pluginYml);
                        writeStringToFile(bgYml, bungeeYml);

                        File packageFolder = new File(srcFolder, projectPackage.replaceAll("\\.", File.separator) + (projectPackage.endsWith(".") ? "" : File.separator) + projectName.toLowerCase());
                        if(!packageFolder.exists()) packageFolder.mkdirs();
                        File spigotPackage = new File(packageFolder, "spigot/"),
                                bungeePackage = new File(packageFolder, "bungee/");
                        if(!spigotPackage.exists()) spigotPackage.mkdirs();
                        if(!bungeePackage.exists()) bungeePackage.mkdirs();

                        String mainSpigot = generateMain("Spigot"),
                                mainBungee = generateMain("Bungee");
                        mainSpigot = mainSpigot.replace("{PROJECT_NAME}", projectName)
                                .replace("{PROJECT_NAME_LOWER}", projectName.toLowerCase())
                                .replace("{PROJECT_PACKAGE}", projectPackage);

                        mainBungee = mainBungee.replace("{PROJECT_NAME}", projectName)
                                .replace("{PROJECT_NAME_LOWER}", projectName.toLowerCase())
                                .replace("{PROJECT_PACKAGE}", projectPackage);
                        File mainSpigotFile = new File(spigotPackage, projectName + ".java"),
                                mainBungeeFile = new File(bungeePackage, projectName + ".java");
                        if(!mainSpigotFile.exists()) mainSpigotFile.createNewFile();
                        if(!mainBungeeFile.exists()) mainBungeeFile.createNewFile();
                        writeStringToFile(mainSpigot, mainSpigotFile);
                        writeStringToFile(mainBungee, mainBungeeFile);
                        System.out.println("Your project was successfully generated!");
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }

        System.out.println("If you need any help join our discord: https://go.theprogramsrc.xyz/discord. Or execute the command --discord like this: java -jar ProjectSetup.jar --discord");
    }

    private static void writeStringToFile(String string, File file) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
        writer.write(string);
        writer.close();
    }

    private static boolean is_null(Object o){
        return o == null;
    }

    private static boolean requestVersions(){
        String scapiMetadata = "https://repo.theprogramsrc.xyz/repository/maven-public/xyz/theprogramsrc/SuperCoreAPI/maven-metadata.xml";
        String spigotMetadata = "https://hub.spigotmc.org/nexus/content/repositories/public/org/spigotmc/spigot-api/maven-metadata.xml";
        String bungeeMetadata = "https://hub.spigotmc.org/nexus/content/repositories/public/net/md-5/bungeecord-api/maven-metadata.xml";
        try{
            HttpRequest scapiRequest = HttpRequest.connect(scapiMetadata),
                    spigotRequest = HttpRequest.connect(spigotMetadata),
                    bungeeRequest = HttpRequest.connect(bungeeMetadata);
            if(scapiRequest.errorOnConnect()) {
                System.err.println("Cannot connect with the SuperCoreAPI Repository: " + scapiRequest.resMessage());
            }else if(spigotRequest.errorOnConnect()){
                System.err.println("Cannot connect with the Spigot Repository: " + spigotRequest.resMessage());
            }else if(bungeeRequest.errorOnConnect()){
                System.err.println("Cannot connect with the Bungee Repository: " + bungeeRequest.resMessage());
            }else{
                String scapiXML = fixXml(scapiRequest.response()),
                        spigotXML = fixXml(spigotRequest.response()),
                        bungeeXML = fixXml(bungeeRequest.response());
                JSONObject scapiJSON = XML.toJSONObject(scapiXML),
                        spigotJSON = XML.toJSONObject(spigotXML),
                        bungeeJSON = XML.toJSONObject(bungeeXML);


                scapiVersion = scapiJSON.getJSONObject("metadata").getJSONObject("versioning").getString("latest");
                spigotVersion = spigotJSON.getJSONObject("metadata").getJSONObject("versioning").getString("latest");
                bungeeVersion = bungeeJSON.getJSONObject("metadata").getJSONObject("versioning").getString("latest");

                return scapiVersion != null && spigotVersion != null && bungeeVersion != null;
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return false;
    }

    private static ArgumentParser generateArgs(){
        ArgumentParser parser = ArgumentParsers.newFor("Project Setup").build()
                .defaultHelp(true)
                .description("Setup a new SuperCoreAPI Project");
        parser.addArgument("--projectName", "--projN")
                .type(String.class)
                .help("The name of the project");
        parser.addArgument("--projectVersion", "--projV")
                .type(String.class)
                .help("The initial version of the project");
        parser.addArgument("--projectPackage", "--projP")
                .type(String.class)
                .help("The group id of the project");
        parser.addArgument("--path")
                .type(String.class)
                .help("The path of the project");
        return parser;
    }

    private static String fixXml(List<String> res){
        res.remove(0);
        return String.join("", res);
    }

    private static String generateMain(String type){
        return "package {PROJECT_PACKAGE}.{PROJECT_NAME_LOWER}."+type.toLowerCase()+";\n" +
                "\n" +
                "import xyz.theprogramsrc.supercoreapi."+type.toLowerCase()+"."+type+"Plugin;\n" +
                "\n" +
                "public class {PROJECT_NAME} extends "+type+"Plugin {\n" +
                "\n" +
                "   @Override\n" +
                "   public void onPluginLoad(){\n" +
                "       // On plugin load stuff\n" +
                "   }\n" +
                "\n" +
                "   @Override\n" +
                "   public void onPluginEnable(){\n" +
                "       // On plugin enable stuff\n" +
                "   }\n" +
                "\n" +
                "   @Override\n" +
                "   public void onPluginDisable(){\n" +
                "       // On plugin disable stuff\n" +
                "   }\n" +
                "\n" +
                "}";
    }

    private static String generatePluginYML(){
        return "name: {PROJECT_NAME}\n" +
                "version: ${project.version}\n" +
                "main: {PROJECT_PACKAGE}.{PROJECT_NAME_LOWER}.spigot.{PROJECT_NAME}\n" +
                "api-version: \"1.13\"\n" +
                "description: ${project.description}";
    }

    private static String generateBungeeYML(){
        return "main: {PROJECT_PACKAGE}.{PROJECT_NAME_LOWER}.bungee.{PROJECT_NAME}\n" +
                "name: {PROJECT_NAME}\n" +
                "version: ${project.version}\n" +
                "description: ${project.description}";
    }

    private static String generateXml(){
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "    <groupId>{PROJECT_PACKAGE}</groupId>\n" +
                "    <artifactId>{PROJECT_NAME}</artifactId>\n" +
                "    <version>{PROJECT_VERSION}</version>\n" +
                "    <packaging>jar</packaging>\n" +
                "\n" +
                "    <name>{PROJECT_NAME}</name>\n" +
                "\n" +
                "    <description>An amazing product built with SuperCoreAPI</description>\n" +
                "    <properties>\n" +
                "        <java.version>1.8</java.version>\n" +
                "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
                "    </properties>\n" +
                "\n" +
                "    <build>\n" +
                "        <finalName>${project.artifactId}</finalName>\n" +
                "        <defaultGoal>clean package</defaultGoal>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                "                <version>3.7.0</version>\n" +
                "                <configuration>\n" +
                "                    <source>${java.version}</source>\n" +
                "                    <target>${java.version}</target>\n" +
                "                </configuration>\n" +
                "            </plugin>\n" +
                "            <plugin>\n" +
                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                "                <artifactId>maven-shade-plugin</artifactId>\n" +
                "                <version>3.1.0</version>\n" +
                "                <executions>\n" +
                "                    <execution>\n" +
                "                        <phase>package</phase>\n" +
                "                        <goals>\n" +
                "                            <goal>shade</goal>\n" +
                "                        </goals>\n" +
                "                        <configuration>\n" +
                "                            <createDependencyReducedPom>false</createDependencyReducedPom>\n" +
                "                            <relocations>\n" +
                "                                <relocation>\n" +
                "                                    <pattern>xyz.theprogramsrc.supercoreapi</pattern>\n" +
                "                                    <shadedPattern>{PROJECT_PACKAGE}.{PROJECT_NAME_LOWER}.supercoreapi</shadedPattern>\n" +
                "                                </relocation>\n" +
                "                            </relocations>\n" +
                "                        </configuration>\n" +
                "                    </execution>\n" +
                "                </executions>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "        <resources>\n" +
                "            <resource>\n" +
                "                <directory>src/main/resources</directory>\n" +
                "                <filtering>true</filtering>\n" +
                "            </resource>\n" +
                "        </resources>\n" +
                "    </build>\n" +
                "\n" +
                "    <repositories>\n" +
                "        <!-- TheProgramSrc -->\n" +
                "        <repository>\n" +
                "            <id>theprogramsrc</id>\n" +
                "            <url>https://repo.theprogramsrc.xyz/repository/maven-public</url>\n" +
                "        </repository>\n" +
                "        <!-- Spigot -->\n" +
                "        <repository>\n" +
                "            <id>spigotmc-repo</id>\n" +
                "            <url>https://hub.spigotmc.org/nexus/content/repositories/snapshots/</url>\n" +
                "        </repository>\n" +
                "        <!-- Sonatype -->\n" +
                "        <repository>\n" +
                "            <id>sonatype</id>\n" +
                "            <url>https://oss.sonatype.org/content/groups/public/</url>\n" +
                "        </repository>\n" +
                "    </repositories>\n" +
                "\n" +
                "    <dependencies>\n" +
                "        <!-- SuperCoreAPI -->\n" +
                "        <dependency>\n" +
                "            <groupId>xyz.theprogramsrc</groupId>\n" +
                "            <artifactId>SuperCoreAPI</artifactId>\n" +
                "            <version>{SCAPI}</version>\n" +
                "            <scope>compile</scope>\n" +
                "        </dependency>\n" +
                "        <!-- Spigot -->\n" +
                "        <dependency>\n" +
                "            <groupId>org.spigotmc</groupId>\n" +
                "            <artifactId>spigot-api</artifactId>\n" +
                "            <version>{SPIGOT}</version>\n" +
                "            <scope>provided</scope>\n" +
                "        </dependency>\n" +
                "        <!-- BUNGEE -->\n" +
                "        <dependency>\n" +
                "            <groupId>net.md-5</groupId>\n" +
                "            <artifactId>bungeecord-api</artifactId>\n" +
                "            <version>{BUNGEE}</version>\n" +
                "            <scope>provided</scope>\n" +
                "        </dependency>\n" +
                "    </dependencies>\n" +
                "</project>";
    }
}
