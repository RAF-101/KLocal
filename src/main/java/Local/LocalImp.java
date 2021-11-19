package Local;

import Spec.Config;
import Spec.Specifikacija;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Scanner;

public class LocalImp implements Specifikacija {

    private Path workingDir = null;
    private Config workingConfig = null;

    public LocalImp(){

    }
    /*
    * 1 - Putanja je fajl
    * 0 - Uspesno
    * 401 - Nema Permisije
     */
    public int connectStorage(String Path) {
        if(Files.isDirectory( java.nio.file.Path.of(Path) ) ){
            workingDir = java.nio.file.Path.of(Path);
            return requestLogin();
        }else{
            if(Files.isRegularFile(java.nio.file.Path.of(Path))) return 1;
            else return promptInitStorage(Path);
        }
    }

    public int promptInitStorage(String Path) {
        System.out.println("Storage under the provided path was not found. Do you want to create it?");
        Scanner input = new Scanner(System.in);
        String answer = input.nextLine();

        if( answer.toLowerCase().contains("y") || answer.toLowerCase().contains("yes") ){
            initStorage(Path);

            System.out.print("Please enter the username for the owner : ");
            String un = input.nextLine();
            System.out.print("Please enter the password for the owner : ");
            String pw = input.nextLine();

            workingConfig.createUser(un, pw, 11111);
            workingConfig.loginUser(un, pw);
            return updateConfig();
        }else{
            return 1; // error, not found
        }
    }

    /*
    * 401 - Invalid Path
    * 0 - Success
     */
    public int initStorage(String Path) {
        try {
            Files.createDirectories(java.nio.file.Path.of(Path));
            workingDir = java.nio.file.Path.of(Path);
            return initConfig();
        } catch (IOException e) {
            return 401;
        }
    }

    @Override
    public int initConfig() {
        try {
            FileWriter file = new FileWriter( workingDir + "\\config.json" );
            file.write(Config.initConfig());
            file.close();

            workingConfig = new Config(new File(workingDir+"\\config.json"));
            workingConfig.setOccupied(true);
            return updateConfig();
        } catch (IOException e) {
            return 401;
        }
    }

    /*
     * 2 - Occupied
     * 1 - Invalid User
     * 0 - Success
     */
    public int requestLogin() {

        if(!Files.isRegularFile(Path.of(workingDir + "\\config.json"))){
            return promptInitStorage(workingDir.toString());
        }else {
            Scanner input = new Scanner(System.in);
            System.out.print("Please enter the username for the account : ");
            String un = input.nextLine();
            System.out.print("Please enter the password for the account : ");
            String pw = input.nextLine();
            workingConfig = new Config(new File(workingDir + "\\config.json"));

            if(workingConfig.loginUser(un, pw) == 0){
                if(workingConfig.checkOccupied()){
                    workingConfig = null;
                    workingDir = null;
                    return 2;
                }
                return 0;
            }
            workingConfig = null;
            workingDir = null;
            return 1;
        }
    }

    /*
    * 404 - Config nonexistant
    * 403 - Nonadequate permisson
    * 402 - Invalid Permission
     */
    public int requestNewUser() {
        if(workingConfig == null) return 404;
        if(!workingConfig.hasPermission(10000)) return 403;

        Scanner input = new Scanner(System.in);
        System.out.print("Please enter the username for the new user : ");
        String un = input.nextLine();
        System.out.print("Please enter the password for the new user : ");
        String pw = input.nextLine();
        System.out.print("Please enter the permissions for the new user : ");
        String perm = input.nextLine();
        if(perm.matches("[1]+"))
            return workingConfig.createUser(un, pw, Integer.parseInt(perm));
        else return 402;
    }

    public int disconnectStorage() {
        if(workingConfig == null) return 404;

        // room for more

        workingConfig.setOccupied(false);
        return updateConfig();
    }

    /*
    * 404 - Niste konektovani na skladiste
    * 401 - Neispravno ime ili Fajl vec postoji
     */
    public int createFile(String name, String destPath) {
        if(workingConfig == null) return 404;
        try {
            if(!workingConfig.hasPermission(10)) return 1;
            Files.createFile( Path.of(workingDir + "/" + destPath + "/" + name) );
            return 0;
        } catch (IOException e) {
            return 401;
        }
    }

    /*
     * 1 - No permission
     * 401 - Neispravno ime ili Fajl vec postoji
     * 404 - Niste konektovani na skladiste
     */
    public int createFolder(String name, String destPath) {
        if(workingConfig == null) return 404;
        try {
            if(!workingConfig.hasPermission(10)) return 1;
            Files.createDirectories( Path.of(workingDir + "/" + destPath) );
            return 0;
        } catch (IOException e) {
            return 401;
        }
    }

    /*
     * 1 - No permission
     * 2 - Not a file
     * 3 - Not a directory
     * 4 - Prekoracena kolicija fajlova
     * 5 - Prekoracena velicina fajlova
     * 401 - Neispravno ime ili Fajl vec postoji
     * 404 - Niste konektovani na skladiste
     */
    public int uploadFile(String srcPath, String destPath) {
        if(workingConfig == null) return 404;
        try {
            if(!workingConfig.hasPermission(10)) return 1;
            if(!Files.isRegularFile(Path.of(srcPath))) return 2;
            if(!Files.isDirectory(Path.of(workingDir + "/" + destPath))) return 3;
                BasicFileAttributes attr = Files.readAttributes(Path.of(srcPath), BasicFileAttributes.class);
                if(workingConfig.checkStorageSizeLimit( ( (Long) attr.size()).intValue() ) )
                    if(workingConfig.checkFileCountLimit(1)) {
                        Files.move(Path.of(srcPath), Path.of(workingDir + "/" + destPath));
                        workingConfig.addFileCount(1);
                        workingConfig.addFileSize( ( (Long) attr.size()).intValue() );
                        return 0;
                    } else return 4;
                return 5;
        } catch (IOException e) {
            return 401;
        }
    }
    //System.out.println("size: " + attr.size());

    /*
     * 1 - No permission
     * 2 - Not a file
     * 3 - Not a directory
     * 4 - Prekoracena kolicija fajlova
     * 5 - Prekoracena velicina fajlova
     * 401 - Neispravno ime ili Fajl vec postoji
     * 404 - Niste konektovani na skladiste
     */
    public int uploadFiles(List<String> srcPaths, List<String> destPaths) {
        if(workingConfig == null) return 404;
        if (!workingConfig.hasPermission(10)) return 1;
        int i = 0, size = 0, count = 0;
        for(String srcPath : srcPaths) {
            String destPath = destPaths.get(i++);
            try {
                if(!Files.isRegularFile(Path.of(srcPath))) return 2;
                if(!Files.isDirectory(Path.of(workingDir + "/" + destPath))) return 3;
                BasicFileAttributes attr = Files.readAttributes(Path.of(srcPath), BasicFileAttributes.class);
                size += ( (Long) attr.size()).intValue();
                count ++;
            } catch (IOException e) {
                return 401;
            }
        }
        i = 0;
        if(workingConfig.checkStorageSizeLimit( size ) )
            if(workingConfig.checkFileCountLimit(count))
                for(String srcPath : srcPaths) {
                    String destPath = destPaths.get(i++);
                    int returnVal = 0;
                    returnVal = uploadFile(srcPath, destPath);
                    if(returnVal != 0) return returnVal;
                }
            else return 4;
        else return 5;
        return 0;
    }

    /*
     * 1 - No permission
     * 2 - Not a file
     * 3 - Not existing
     * 401 - Neispravno ime ili Fajl vec postoji
     * 404 - Niste konektovani na skladiste
     */
    public int deleteFile(String path) {
        if(workingConfig == null) return 404;
        try {
            if(!workingConfig.hasPermission(1000)) return 1;
            if(!Files.isRegularFile(Path.of(path))) return 2;
            BasicFileAttributes attr = Files.readAttributes(Path.of(path), BasicFileAttributes.class);
            int size = ( (Long) attr.size()).intValue();
            if(Files.deleteIfExists( Path.of(path) ) ){
                workingConfig.addFileSize(-1 * size);
                workingConfig.addFileCount(-1);
                return 0;
            }
            return 3;
        } catch (IOException e) {
            return 401;
        }
    }

    public int deleteFolder(String path) {
        if(workingConfig == null) return 404;
        // delete folder + check all files/folders affected for size and count D:
        return 0;
    }

    public List<String> listFiles(String path) {
        if(workingConfig == null) return null;

        return null;
    }

    public int moveFile(String srcPath, String destPath) {
        if(workingConfig == null) return 404;

        return 0;
    }

    public int downloadPath(String srcPath, String destPath) {
        if(workingConfig == null) return 404;

        return 0;
    }

    public int downloadFolder(String srcPath, String destPath) {
        if(workingConfig == null) return 404;

        return 0;
    }

    public int downloadFile(String srcPath, String destPath) {
        if(workingConfig == null) return 404;

        return 0;
    }

    @Override
    public int updateConfig() {
        try {
            FileWriter file = null;
            file = new FileWriter( workingDir + "\\config.json" );
            file.write(workingConfig.getJSONForm());
            file.close();
        } catch (IOException e) {
            return 401;
        }
        return 0;
    }
}
