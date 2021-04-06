/**
 * @author Lorenzo Vaccher
 * Copyright (c) 2021 Lorenzo Vaccher.
 */

package org.ping.cool.utils.logger;

import androidx.fragment.app.Fragment;

import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static SimpleDateFormat time;
    private static SimpleDateFormat date;

    public Logger() {
        time = new SimpleDateFormat("HH:mm:ss");
        date = new SimpleDateFormat("dd-MM-yyyy");
    }

    /**
     * @param message The message to print into the log file
     * @param color   The color of the message that is printed in the console
     */
    public static void log(final String message, final Color color) {
        System.out.println(getTime() + color.getColor() + message + Color.RESET.getColor());
    }

    /**
     * @param message The message to print into the log file.
     */
    public static void log(final String message) {
        log(message, Color.WHITE);
    }

    public static void clearConsole() {
        for (int i = 0; i < 200; i++) {
            System.out.println();
        }
    }

    /**
     * This method gets the time.
     *
     * @return Custom format time -> "[{time}]: ".
     */
    private static String getTime() {
        return "[" + time.format(new Date()) + "]: ";
    }

    /**
     * This method gets the current date.
     *
     * @return The current date
     */
    private static String getDate() {
        return date.format(new Date());
    }

    public static void PutLogConsole(Fragment context, EditText editText, String log) {

        context.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                editText.append(log);
                editText.setSelection(editText.getText().toString().length());
            }
        });
    }

}
