/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.base.util;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Execute command by modified process.
 * Replacement for {@link Runtime#exec(String)}.
 */
public final class ProcessBuilderUtils {

    /**
     * Empty constructor for utility.
     */
    private ProcessBuilderUtils() {
        // Static utils class so no object instantiation is allowed.
    }

    /**
     * Execute the command array by provisioned {@link ProcessBuilder}.
     *
     * @param processBuilder A provisioned process builder.
     * @param cmdArray       The command array.
     * @return A new Process object for managing sub process.
     * @throws IOException If an I/O error occurs.
     */
    public static Process exec(final ProcessBuilder processBuilder, final String[] cmdArray) throws IOException {
        return processBuilder.command(cmdArray)
                // skip setting the environment since it is empty.
                .directory(null)
                .start();
    }

    /**
     * Execute the command array.
     *
     * @param cmdArray The command array.
     * @return A new Process object for managing sub process.
     * @throws IOException If an I/O error occurs.
     */
    public static Process exec(final String[] cmdArray) throws IOException {
        return exec(new ProcessBuilder(), cmdArray);
    }

    /**
     * Execute command array by process builder with redirected error stream.
     *
     * @param cmdArray The command array.
     * @return A new Process object for managing sub process with redirected error stream.
     * @throws IOException If an I/O error occurs.
     */
    public static Process execWithRedirectedErrorStream(final String[] cmdArray) throws IOException {
        return exec(new ProcessBuilder().redirectErrorStream(true), cmdArray);
    }

    /**
     * Execute command string by process builder with redirected error stream.
     *
     * @param command The command to execute.
     * @return A new Process object for managing sub process.
     * @throws IOException If an I/O error occurs.
     */
    public static Process execWithRedirectedErrorStream(final String command) throws IOException {

        //Following code refers to {@link Runtime#exec(String)}.
        if (command.length() == 0) {
            throw new IllegalArgumentException("Empty command");
        }
        final StringTokenizer stringTokenizer = new StringTokenizer(command);
        final String[] cmdArray = new String[stringTokenizer.countTokens()];
        for (int i = 0; stringTokenizer.hasMoreTokens(); i++) {
            cmdArray[i] = stringTokenizer.nextToken();
        }
        return execWithRedirectedErrorStream(cmdArray);
    }

}
