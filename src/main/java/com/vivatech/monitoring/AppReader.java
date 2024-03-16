
package com.vivatech.monitoring;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

@Service
public class AppReader {




    private Map<String, LocalDateTime> applicationOpenTimes = new HashMap<>();
    private Map<String, Long> applicationUsageTimes = new HashMap<>();

    private static String durationToString(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        return hours + " hours, " + minutes + " minutes, " + seconds + " seconds";
    }

    private static long durationInSeconds(Duration duration){
        return duration.getSeconds();
    }

    public void readApp(String appName) {
        // Start the event listener in a separate thread
        new Thread(this::setupEventListener).start();

        /*Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter the name of the application you want to track (or 'exit' to quit):");
            String applicationName = scanner.nextLine();

            if (applicationName.equalsIgnoreCase("exit")) {
                break;
            }

            // Record the open time of the application
            applicationOpenTimes.put(applicationName, LocalDateTime.now());
            System.out.println(applicationName + " opened at: " + applicationOpenTimes.get(applicationName));

            // Simulate application running
            simulateApplicationRunning();

            // Record the close time of the application
            LocalDateTime closeTime = LocalDateTime.now();
            System.out.println(applicationName + " closed at: " + closeTime);

            Duration durationOpen = Duration.between(applicationOpenTimes.get(applicationName), closeTime);

            // Update the usage time for the application
            applicationUsageTimes.put(applicationName, durationOpen);

            System.out.println(applicationName + " was open for: " + durationToString(durationOpen));

        }*/

        System.out.println("Application usage summary:");
        for (Map.Entry<String, Long> entry : applicationUsageTimes.entrySet()) {
            System.out.println(entry.getKey() + " was used for: " + entry.getValue());
        }
    }

    private void simulateApplicationRunning() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Press enter to close the application.");
        scanner.nextLine();
    }

    // Generic function to get the last entry of a LinkedHashMap
    public static <K, V> Map.Entry<K, V> getLastEntry(LinkedHashMap<K, V> map) {
        if (map.isEmpty()) {
            return null; // If map is empty, return null
        }

        Map.Entry<K, V> lastEntry = null;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            lastEntry = entry;
        }
        return lastEntry;
    }

    // Function to calculate duration in milliseconds
    public static long calculateDurationInSeconds(LocalDateTime startTime, LocalDateTime endTime) {
        Duration duration = Duration.between(startTime, endTime);
        return duration.toSeconds();
    }

    private void setupEventListener() {
        final String[] windowName = {""};
        final boolean[] isWordOpen = {false};
        LinkedHashMap<String, LocalDateTime> appOpenTime = new LinkedHashMap<>();
        LinkedHashMap<String, Long> appCloseTime = new LinkedHashMap<>();

        User32.INSTANCE.SetWinEventHook(
                WinUserEx.EVENT_SYSTEM_FOREGROUND,
                WinUserEx.EVENT_SYSTEM_FOREGROUND,
                null,
                new WinUser.WinEventProc() {
                    @Override
                    public void callback(WinNT.HANDLE handle, WinDef.DWORD eventId, WinDef.HWND hwnd, WinDef.LONG objectId, WinDef.LONG childId, WinDef.DWORD eventThreadId, WinDef.DWORD eventTime) {
                        char[] windowText = new char[512];
                        User32.INSTANCE.GetWindowText(hwnd, windowText, 512);
                        String windowTitle = Native.toString(windowText);
                        windowName[0] = windowTitle;

                        for (Map.Entry<String, Long> entry : applicationUsageTimes.entrySet()) {
                            System.out.println(entry.getKey() + " was used for: " + entry.getValue());
                        }

                        if (windowTitle.contains("AppReader")) return;
                        LocalDateTime localDateTime = applicationOpenTimes.get(windowTitle);
                        if (localDateTime != null){
                            LocalDateTime startTime = applicationOpenTimes.get(windowTitle);
                            LocalDateTime endTime = LocalDateTime.now();
                            Duration duration = Duration.between(startTime, endTime);
                            long durationInSeconds = duration.getSeconds();

                            Long closeTime = applicationUsageTimes.get(windowTitle);

                            if (closeTime != null && closeTime > 0){
                                closeTime+= durationInSeconds;
                                applicationUsageTimes.put(windowTitle, closeTime);
                            } else {
                                applicationUsageTimes.put(windowTitle, durationInSeconds);
                            }

                            applicationOpenTimes.put(windowTitle, LocalDateTime.now());
                        } else {
                            applicationOpenTimes.put(windowTitle, LocalDateTime.now());
                        }

                        /*System.out.println("Window Name: " + windowTitle);
                        if (windowTitle.contains("Microsoft Word")) {
                            if (!isWordOpen[0]) {
                                System.out.println("Word document opened: " + windowTitle);
                                // Here you can add code to record the opening time
                                isWordOpen[0] = true;
                            }
                        } else if (windowTitle.contains("Google Chrome")) {
                            if (!isWordOpen[0]) {
                                System.out.println("Word document opened: " + windowTitle);
                                // Here you can add code to record the opening time
                                isWordOpen[0] = true;
                            }
                        } else {
                            if (isWordOpen[0]) {
                                System.out.println("Word document closed: " + windowTitle);
                                // Here you can add code to record the closing time
                                isWordOpen[0] = false;
                            }
                        }*/
                    }
                }, 0, 0, 0x0000); // Use the hexadecimal value

        // Run the message loop to keep the listener active
        WinUser.MSG msg = new WinUser.MSG();
        while (User32.INSTANCE.GetMessage(msg, null, 0, 0) != 0) {
            User32.INSTANCE.TranslateMessage(msg);
            User32.INSTANCE.DispatchMessage(msg);
        }



    }
}
