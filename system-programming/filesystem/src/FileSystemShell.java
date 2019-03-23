import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Scanner;

public class FileSystemShell {
/*
    private FileSystem fs;


    private void log(String message) {
        System.out.println(message);
    }

    private void logWrongCmd() { logWrongCmd(""); }

    private void logWrongCmd(String message) {
        log("Wrong command" + (!message.isEmpty() ? ": " + message : ""));
    }

    private void logWrongArgsCount() {
        logWrongCmd("Illegal arguments count");
    }

    private void logError(String message) {
        log("Error: " + message);
    }

    private boolean testArgsCount(String[] args, int count) {
        if (args.length != count) logWrongArgsCount();
        return args.length == count;
    }


    private void parseDiskCmd(String[] args) {
        if (!testArgsCount(args, 2)) return;
        try {
            fs = new FileSystem(args[1]);
        } catch (IOException e) {
            logError(e.getMessage());
        }
        log("disk was " + (fs.isRestored() ? "restored" : "initialized"));
    }

    private void parseFileCmd(String[] args) {
        if (!testArgsCount(args, 2)) return;

        try {
            String filename = args[1];
            switch (args[0]) {
                case "cr":
                    fs.create(filename);
                    log(filename + " created");
                    break;
                case "de":
                    fs.destroy(filename);
                    log(filename + " destroyed");
                    break;
                case "op":
                    int index = fs.open(filename);
                    log(filename + " opened with index " + index);
                    break;
            }
        } catch (IOException e) {
            logError(e.getMessage());
        }
    }

    private void parseCloseCmd(String[] args) {}

    private void parseReadWriteCmd(String[] args) {}

    private void parseSeekCmd(String[] args) {}

    private void parseDirCmd(String[] args) {}


    public void runUI() {
        Scanner console = new Scanner(System.in);

        boolean flagExit = false;
        while (!flagExit) {
            // Read command
            String cmdStr = console.nextLine();
            String[] cmd = cmdStr.split(" ");

            // Process command
            switch (cmd[0]) {
                case "cr": // create file with name
                case "de": // destroy file by name
                case "op": // open file with name
                    parseFileCmd(cmd);
                    break;
                case "cl": // close file by index
                    parseCloseCmd(cmd);
                    break;
                case "rd": // read binary data from file
                case "wr": // write data to file
                    parseReadWriteCmd(cmd);
                    break;
                case "sk": // seek
                    parseSeekCmd(cmd);
                    break;
                case "dr": // list of files in directory
                    parseDirCmd(cmd);
                    break;
                case "in": // open disk container
                case "sv": // save disk container
                    parseDiskCmd(cmd);
                    break;
                case "ex": // exit from ui
                    log("Exiting...");
                    flagExit = true;
                default:
                    logWrongCmd();
            }
        }
    }
*/
    public static void main(String[] args) {
        /*
        FileSystemShell shell = new FileSystemShell();
        shell.runUI();
        */
        System.out.println("Hello world!");
    }
}
