package com.nike.buildmaster.handlers;

import com.nike.buildmaster.Os;
import com.nike.buildmaster.ui.MainFrame;
import com.nike.buildmaster.ui.buildtree.BuildableItem;
import org.jdesktop.swingworker.SwingWorker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;


/** This class actually does the builds for a given tag. */
@SuppressWarnings(
        {"UseOfSystemOutOrSystemErr", "CallToPrintStackTrace", "LocalVariableNamingConvention", "TypeMayBeWeakened", "UseOfProcessBuilder"})
public class BuildHandler extends SwingWorker<Object, Object>
{
    private File tagRoot;
    private BuildableItem target;
    private MainFrame mainFrame;

    public BuildHandler(File tagRoot, BuildableItem target, MainFrame mainFrame)
    {
        this.tagRoot = tagRoot;
        this.target = target;
        this.mainFrame = mainFrame;
    }

    @Override
    public Object doInBackground() throws Exception
    {

        mainFrame.addStatus("Doing build at tagRoot = " + tagRoot + " for target " + target);

        String tagPath = tagRoot.getAbsolutePath();
        Os os = mainFrame.getOs();
        String[] command = os.getCommandArgs(tagPath, target);

        runCommand(mainFrame, command, tagPath);

        return null;
    }

    /** Runs the given command at the given tagpath.  Prints out all output, both main stream and error stream, to files at that location. */
    @SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
    private static void runCommand(MainFrame theFrame, String[] command, String tagPath) throws IOException, InterruptedException
    {
        String output = "The command strings to run are: ";

        for (String token : command)
        {
            output += token + " ";
        }

        System.out.println(output);

        //        Map<String, String> envMap = System.getenv();
        //
        //        for (String key : envMap.keySet())
        //        {
        //            System.out.println(key + "=" + envMap.get(key));
        //        }

        ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(command));
        processBuilder.directory(new File(tagPath));

        Map<String, String> processEnvMap = processBuilder.environment();

        if (theFrame.getOs() == Os.WINDOWS)
        {
            processEnvMap.put("SystemRoot", "c:\\WINDOWS");
            processEnvMap.put("env.USERPROFILE", tagPath + ".ivy");
        }

        FileOutputStream outputStream = new FileOutputStream(new File(tagPath, "build_" + command[command.length - 1] + ".out"));
        FileOutputStream errorOutputStream = new FileOutputStream(new File(tagPath, "build_" + command[command.length - 1] + ".error"));
        Process process = processBuilder.start();
        StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), OutputType.ERROR, errorOutputStream);
        StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), OutputType.OUTPUT, outputStream);

        errorGobbler.start();
        outputGobbler.start();

        int exitVal = process.waitFor();

        System.out.println("Process exitValue: " + exitVal);
        // }

        // wait for the gobblers to catch up before closing
        try
        {
            Thread.sleep(100);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        outputStream.flush();
        outputStream.close();
    }
}
