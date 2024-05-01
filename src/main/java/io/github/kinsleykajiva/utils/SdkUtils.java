package io.github.kinsleykajiva.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SdkUtils {

  static Logger log = Logger.getLogger(SdkUtils.class.getName());
  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  public static boolean IS_LINUX = true;

  static {
    String os = System.getProperty("os.name").toLowerCase();
    if (os.startsWith("win")) {
      IS_LINUX = false;
    }
  }

  public static void runAfter(long delay, Runnable runnable) {

    scheduler.schedule(runnable, delay, TimeUnit.SECONDS);
  }

  private static final String ALPHABET =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final int ALPHABET_SIZE = ALPHABET.length();
  private static final Random RANDOM = new Random();

  public static String IdGenerator() {
    UUID uuid = UUID.randomUUID();
    return uuid.toString().replaceAll("-", "") + System.nanoTime();
  }

  protected static String nonAlphaNumeric(String str) {
    return str.replaceAll("[^a-zA-Z0-9]", "");
  }

  public static String convertToWebSocketUrl(String url) {
    if (url == null || url.isEmpty()) return "";
    if (url.startsWith("http://")) return url.replace("http://", "ws://");
    if (url.startsWith("https://")) return url.replace("https://", "wss://");
    return url;
  }

  public static String getWebSocketUrl(String url) {
    if (url == null || url.isEmpty()) return "";
    return url;
  }

  public static String getWebSocketProtocol(String url) {
    if (url == null || url.isEmpty()) return "";
    if (url.startsWith("ws://")) return "ws";
    if (url.startsWith("wss://")) return "wss";
    return url;
  }

  public static String getWebSocketHost(String url) {
    if (url == null || url.isEmpty()) return "";
    if (url.startsWith("ws://")) return url.substring(5);
    if (url.startsWith("wss://")) return url.substring(6);
    return url;
  }

  public static String getWebSocketPort(String url) {
    if (url == null || url.isEmpty()) return "";
    if (url.startsWith("ws://")) return "";
    if (url.startsWith("wss://")) return "";
    return url;
  }

  public static String convertFromWebSocketUrl(String url) {
    if (url == null || url.isEmpty()) return "";
    if (url.startsWith("ws://")) return url.replace("ws://", "http://");
    if (url.startsWith("wss://")) return url.replace("wss://", "https://");
    return url;
  }

  public static boolean isJsonArray(String str) {
    try {
      new JSONArray(str);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static boolean isJson(String str) {
    try {
      new JSONObject(str);
    } catch (JSONException ex) {
      try {
        new JSONArray(str);
      } catch (JSONException ex1) {
        return false;
      }
    }
    return true;
  }

  /**
   * Generates a unique ID based on the current time, a seed string, and a maximum size.
   *
   * @param seed The seed string to prepend to the generated ID.
   * @param maxSize The maximum size of the generated ID.
   * @return The generated unique ID.
   */
  public static String uniqueIDGenerator(String seed, int maxSize) {
    LocalTime time = LocalTime.now();
    String timeString =
        String.format("%02d%02d%02d", time.getHour(), time.getMinute(), time.getSecond());
    String dateTimeString = seed + timeString + RANDOM.nextInt(10000);
    if (dateTimeString.length() > 12) {
      dateTimeString = dateTimeString.substring(0, 12);
    }
    StringBuilder uniID = new StringBuilder();
    for (int i = 0; i < maxSize; i++) {
      uniID.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET_SIZE)));
    }

    for (int i = 0; i < dateTimeString.length(); i++) {
      int index = Character.digit(dateTimeString.charAt(i), 36);
      if (index < uniID.length()) {
        uniID.replace(index, index + 1, String.valueOf(dateTimeString.charAt(i)));
      }
    }

    return uniID.toString().replaceAll("[^a-z0-9]", "");
  }

  /**
   * Executes the given command in the bash or cmd.exe shell and returns the output as a list of
   * strings. If the command is empty, an empty list will be returned. The method will throw an
   * IOException if there is an error reading or writing to the process. The method will throw an
   * InterruptedException if the current thread is interrupted while waiting for the process to
   * complete.
   *
   * @param command The command to execute.
   * @return A list of strings representing the output of the command.
   * @throws IOException If there is an error reading or writing to the process.
   * @throws InterruptedException If the current thread is interrupted while waiting for the process
   *     to complete.
   * @throws RuntimeException If the execution fails with a non-zero exit code.
   */
  public static List<String> bashExecute(final String command)
      throws IOException, InterruptedException {
    if (command.isEmpty()) {
      return new ArrayList<>();
    }
    log.info("Executing command: " + command);

    List<String> output = new ArrayList<>();
    ProcessBuilder processBuilder = new ProcessBuilder();

    if (!IS_LINUX) {
      processBuilder.command("cmd.exe", "/c", command);
    } else {
      processBuilder.command("bash", "-c", command);
    }

    processBuilder.redirectErrorStream(true); // Redirect error stream to input stream

    Process process = processBuilder.start();
    // Process standard output
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        output.add(line);
      }
    }

    int exitCode = process.waitFor();
    // convert to seconds time taken

    if (exitCode != 0) {
      throw new RuntimeException(
          "Execution failed with error code " + exitCode + "\n for command: " + command);
    }

    return output;
  }

  public static String cleanFilePath(String filePath) {
    return filePath.replaceAll("//", "/");
  }

  public static boolean isFFMPEGInstalled() {
    boolean isFound = false;
    StringBuilder commandLinePrint = new StringBuilder();
    try {
      List<String> output = bashExecute("ffmpeg -version");
      for (String line : output) {
        commandLinePrint.append(line);
        if (line.contains("ffmpeg version")) {
          isFound = true;
        }
      }
    } catch (IOException | InterruptedException e) {
      log.log(Level.SEVERE, "exec error: " + e.getMessage(), e);
    }
    if (!commandLinePrint.isEmpty()) {
      Pattern ffmpegPattern = Pattern.compile("ffmpeg version (\\S+)");
      Matcher ffmpegMatcher = ffmpegPattern.matcher(commandLinePrint.toString());
      if (ffmpegMatcher.find()) {
        System.out.println("FFmpeg version: " + ffmpegMatcher.group(1));
      }

      Pattern toolPattern = Pattern.compile("(lib\\D+)\\s+(\\d+\\.\\d+\\.\\d+)");
      Matcher toolMatcher = toolPattern.matcher(commandLinePrint.toString());

      Map<String, String> toolVersions = new HashMap<>();
      while (toolMatcher.find()) {
        toolVersions.put(toolMatcher.group(1).trim(), toolMatcher.group(2));
      }

      for (Map.Entry<String, String> entry : toolVersions.entrySet()) {
        System.out.println(entry.getKey() + " version: " + entry.getValue());
      }
    }

    return isFound;
  }

  public static boolean isJanusInstalled() {
    try {
      List<String> output = bashExecute("janus-pp-rec --version");
      for (String line : output) {
        log.info("line " + line);
        if (line.contains("Janus version:")
            || line.contains("Janus commit:")
            || line.contains("Compiled on:")) {
          return true;
        }
      }
    } catch (IOException | InterruptedException e) {
      log.log(Level.SEVERE, "exec error: " + e.getMessage(), e);
    }

    return false;
  }

  public static boolean folderExists(String recordingFolder) {

    File file = new File(recordingFolder);
    if (file.exists() && file.isDirectory()) {
      return true;
    }
    return false;
  }
}
