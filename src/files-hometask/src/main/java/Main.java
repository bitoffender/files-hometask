import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {
    static StringBuilder logger = new StringBuilder();
    static int createdFilesAmount = 0;
    static final String PARENT_DIR = System.getProperty("user.home") + "/IdeaProjects/files-hometask/src/main/java";

    public static void openZip(String pathToZip, String pathWhereUnzip) {
        int tempIndex = pathToZip.lastIndexOf("/");
        String path = pathToZip.substring(0, tempIndex);
        String zipName = pathToZip.substring(tempIndex);
        File zipArchive = new File(path, zipName);
        File outputDir = new File(pathWhereUnzip);

        try (ZipInputStream zin = new ZipInputStream(new
                FileInputStream(zipArchive))) {
            ZipEntry entry;
            String name;
            while ((entry = zin.getNextEntry()) != null) {
                name = entry.getName();
                FileOutputStream fout = new FileOutputStream(new File(outputDir, name));
                for (int c = zin.read(); c != -1; c = zin.read())
                    fout.write(c);
                fout.flush();
                zin.closeEntry();
                fout.close();
            }
        } catch (Exception ex) {
            logger.append("Exception during unpacking archive ").append(zipName).append(" --> ").append(ex.getMessage())
                    .append("\n");
        }
        logger.append("Successfully unpacked archive ").append(zipName).append(" to ").append(outputDir)
                .append("\n");
    }

    public static GameProgress openProgress(String pathToFile) throws Exception {
        GameProgress gameProgress = null;
        try (FileInputStream fis = new FileInputStream(pathToFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            gameProgress = (GameProgress) ois.readObject();
        } catch (Exception ex) {
            logger.append("Exception in deserialization of ").append(pathToFile).append(" --> ").append(ex.getMessage())
                    .append("\n");
            throw new FileNotFoundException("Failed to complete deserialization of " + pathToFile);
        }
        System.out.println(gameProgress);
        return gameProgress;
    }

    public static File[] findSaveFiles(String directory){
        List<File> saveFiles = new ArrayList<>();
        for (File item: new File(directory).listFiles()) {
            String name = item.getName();
            if (name.startsWith("save") && name.endsWith(".dat"))
                saveFiles.add(item);
        }
        return saveFiles.toArray(new File[0]);
    }

    public static void zipFiles(File[] files){
        File zipArchive = new File(PARENT_DIR + "/Games/savegames", "zip_saves.zip");

        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipArchive))) {
            for (File file : files) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry entry = new ZipEntry(file.getName());
                    zout.putNextEntry(entry);
                    byte[] buffer = new byte[fis.available()];
                    zout.write(buffer);
                    zout.flush();
                    zout.closeEntry();
                    if (file.delete())
                        logger.append("File ").append(file.getName())
                                .append(" was added to zip archive and deleted successfully.")
                                .append("\n");
                    else {
                        logger.append("Failed deleting ").append(file.getName())
                                .append("\n");
                    }
                } catch (Exception ex) {
                    logger.append("File ").append(file.getName())
                            .append(" could not be packed into the archive, exception -> ")
                            .append(ex.getMessage())
                            .append("\n");
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }


    public static void saveGame(GameProgress gp) throws FileNotFoundException {
        // Creating file for storing the object in proper directory with random filename.
        File save = createFile(PARENT_DIR + "/Games/savegames",  "save" +
                createdFilesAmount + ".dat");
        createdFilesAmount += 1;

        // Serialization of the GameProgress object
        try (FileOutputStream fos = new FileOutputStream(save);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(gp);    // Saving object to file

            // Logging success
            logger.append("Successfully saved game into ").append(save.getName())
                    .append("\n");

        } catch (Exception ex) {
            // Logging unsuccess
            logger.append("Failed saving the game into ").append(save.getName())
                    .append("\n");
        }


    }

    public static void createDirectory(String path){
        File f = new File(path);
        if (f.mkdir())
            logger.append("Successfully created directory ").append(path)
                    .append("\n");
        else {
            logger.append("Unable to create directory ").append(path)
                    .append("\n");
        }
    }

    public static File createFile(String path, String filename) throws FileNotFoundException {
        try {
            File f = new File(path, filename);
            if (f.createNewFile()) {
                logger.append("Successfully created ").append(filename).append(" in ").append(path)
                        .append("\n");
                return f;
            }
        } catch (IOException ex) {
            logger.append("Failed creating file ").append(filename).append(" in ").append(path)
                    .append(". Exception: ").append(ex.getMessage())
                    .append("\n");
        }
        // This exception is made to ensure that no value is returned
        throw new FileNotFoundException("Failed creating file " + filename + " in " + path);
    }
    public static void main(String[] args) throws Exception {
        List<String> directories = Arrays.asList("/src", "/res", "/savegames", "/temp", "/src/main", "/src/test");
        directories.replaceAll(s -> PARENT_DIR + "/Games" + s);

        for (String s : directories) createDirectory(s);

        // Creating file for logging
        File log = createFile(PARENT_DIR + "/Games/temp", "temp.txt");

        createFile(PARENT_DIR + "/Games/src/main", "Main.java");
        createFile(PARENT_DIR + "/Games/src/main", "Utils.java");
        // Initializing objects with random numbers, creating files for them and saving them.
        Random r = new Random();
        // This looks more accurate than writing same thing 4 times
        List<GameProgress> progresses = new ArrayList<>();
        for (int i = 0; i < 4; i++)
            progresses.add(
                    new GameProgress( r.nextInt(100), r.nextInt(100), r.nextInt(100), r.nextDouble())
            );


        progresses.forEach(s -> {
            try {
                saveGame(s);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });    // Saving it

        // There were no instructions on how should I get the files to zip,
        //      so I made a method that finds all of them, since it is better practice in my opinion.
        File[] filesToZip = findSaveFiles(PARENT_DIR + "/Games/savegames");

        zipFiles(filesToZip); // Method for finding, zipping and deleting GameProgress save files.

        // Works
        openZip(PARENT_DIR + "/Games/savegames/zip_saves.zip", PARENT_DIR + "/Games/src/test");

        File randomFileToShowThatOpenProgressWorks = (File) Arrays.stream(Objects.requireNonNull(new File
                        (PARENT_DIR + "/Games/src/test").listFiles()))
                .filter(s -> s.getName().startsWith("save") && s.getName().endsWith(".dat"))
                .toArray()[0];

        // Does not work
        openProgress(randomFileToShowThatOpenProgressWorks.getPath());

        // Writing log.
        try (FileWriter fw = new FileWriter(log, true)){
            fw.write(logger.toString());
            fw.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
