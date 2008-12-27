package com.nike.buildmaster.handlers;

import java.io.*;


/** Utility class to help grab the output of a process and redirect that output to a file. */
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "ClassExplicitlyExtendsThread", "CallToPrintStackTrace"})
class StreamGobbler extends Thread
{
    private InputStream inputStream;
    private OutputType type;
    private OutputStream outputStream;

    StreamGobbler(InputStream inputStream, OutputType type)
    {
        this(inputStream, type, null);
    }

    StreamGobbler(InputStream inputStream, OutputType type, OutputStream outputStream)
    {
        this.inputStream = inputStream;
        this.type = type;
        this.outputStream = outputStream;
    }

    @Override
    public void run()
    {

        try
        {
            PrintWriter printWriter = null;

            if (outputStream != null)
            {
                printWriter = new PrintWriter(outputStream);
            }

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;

            while ((line = bufferedReader.readLine()) != null)
            {

                if (printWriter != null)
                {
                    printWriter.println(line);
                }

                System.out.println(type + "> " + line);
            }

            if (printWriter != null)
            {
                printWriter.println("\n");
                printWriter.flush();
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
