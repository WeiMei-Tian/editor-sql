package com.gmobile.sqliteeditor.assistant;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * ShellUtils
 * <ul>
 * <strong>Execte command</strong>
 * <li>{@link ShellUtils#execCommand(String, boolean)}</li>
 * <li>{@link ShellUtils#execCommand(String, boolean, boolean)}</li>
 * <li>{@link ShellUtils#execCommand(List, boolean)}</li>
 * <li>{@link ShellUtils#execCommand(List, boolean, boolean)}</li>
 * <li>{@link ShellUtils#execCommand(String[], boolean)}</li>
 * <li>{@link ShellUtils#execCommand(String[], boolean, boolean)}</li>
 * </ul>
 *
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-5-16
 */
public class ShellUtils {

    public static final String COMMAND_SU = "su";
    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_ECHO_RETURN = "echo return\n";
    public static final String COMMAND_LINE_END = "\n";
    public static final String BUSY_BOX_PATH = "/data/data/xcxin.filexpert/files/busybox";
    public static final String BUSY_BOX_NAME = "busybox";

    private static Process mSuProcess;
    private static final Object lock = new Object();

    private ShellUtils() {
        throw new AssertionError();
    }

    public static boolean chmod(String path, String permission) {
        if (TextUtils.isEmpty(permission)) {
            return false;
        }

        if (permission.matches("[0-7]{3,4}")) {
            return execCommand("chmod " + permission + " '" + path + "'", true, true).result == 0;
        }

        if (permission.length() != 9) {
            return false;
        }

        byte[] perms = new byte[]{'0', '0', '0'};
        byte[] bytes = permission.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            perms[i / 3] += perm2num(bytes[i]);
        }

        return execCommand("chmod " + new String(perms) + " '" + path + "'", true, true).result == 0;
    }

    private static int perm2num(byte perm) {
        switch (perm) {
            case 'r':
                return 4;
            case 'w':
                return 2;
            case 'x':
                return 1;
            default:
                return 0;
        }
    }

    public static boolean deleteFile(String path) {
        try {
            return execCommand("rm -rf '" + path + "'", true, true).result == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean copyFile(String srcPath, String dstPath) {
        return execCommand("dd if='" + srcPath + "' of='" + dstPath + "'", true, true).result == 0;
    }

    public static boolean copyFileWithCp(String srcPath, String dstPath) {
        return execCommand("cp '" + srcPath + "' '" + dstPath + "'", true, true).result == 0;
    }

    public static boolean makeDir(String path) {
        return execCommand("mkdir '" + path + "'", true, true).result == 0;
    }

    public static boolean createNewFile(String path) {
        return execCommand("touch '" + path + "'", true, true).result == 0;
    }

    public static boolean writeFile(String path, String content) {
        return execCommand("echo '" + content + "'>'" + path + "'", true, true).result == 0;
    }

    public static boolean move(String oldPath, String newPath) {
        return execCommand("mv '" + oldPath + "' '" + newPath + "'", true, true).result == 0;
    }

    public static boolean exists(String path) {
        return !TextUtils.isEmpty(execCommand("ls -ld '" + path + "'", true, true).successMsg);
    }

    public static boolean existsWithSh(String path) {
        return !TextUtils.isEmpty(execCommand("ls -ld '" + path + "'", false, true).successMsg);
    }

    public static boolean canExeRoot() {
        CommandResult result = execCommand("id", true, true);
        return result.result == 0 &&
                !TextUtils.isEmpty(result.successMsg) && result.successMsg.contains("uid=0");
    }

    public static String getPermission(String path) {
        CommandResult result = execCommand(BUSY_BOX_PATH + " ls -ld '" + path + "'", true, true);
        if (result.result == 0) {
            String[] parse = result.successMsg.split("[ ]+");
            return parse[0];
        } else {
            return "-rw-rw----";
        }
    }

    /**
     * execute shell command, default return result msg
     *
     * @param command command
     * @param isRoot  whether need to run with root
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(String command, boolean isRoot) {
        return execCommand(new String[]{command}, isRoot, true);
    }

    /**
     * execute shell commands, default return result msg
     *
     * @param commands command list
     * @param isRoot   whether need to run with root
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(List<String> commands, boolean isRoot) {
        return execCommand(commands == null ? null :
                commands.toArray(new String[commands.size()]), isRoot, true);
    }

    /**
     * execute shell commands, default return result msg
     *
     * @param commands command array
     * @param isRoot   whether need to run with root
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot) {
        return execCommand(commands, isRoot, true);
    }

    /**
     * execute shell command
     *
     * @param command         command
     * @param isRoot          whether need to run with root
     * @param isNeedResultMsg whether need result msg
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(String command, boolean isRoot,
                                            boolean isNeedResultMsg) {
        return execCommand(new String[]{command}, isRoot, isNeedResultMsg);
    }

    /**
     * execute shell commands
     *
     * @param commands        command list
     * @param isRoot          whether need to run with root
     * @param isNeedResultMsg whether need result msg
     * @see ShellUtils#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(List<String> commands, boolean isRoot,
                                            boolean isNeedResultMsg) {
        return execCommand(commands == null ? null :
                commands.toArray(new String[commands.size()]), isRoot, isNeedResultMsg);
    }

    /**
     * execute shell commands
     *
     * @param commands        command array
     * @param isRoot          whether need to run with root
     * @param isNeedResultMsg whether need result msg
     * @return <ul>
     * <li>if isNeedResultMsg is false, {@link CommandResult#successMsg} is null and
     * {@link CommandResult#errorMsg} is null.</li>
     * <li>if {@link CommandResult#result} is -1, there maybe some excepiton.</li>
     * </ul>
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot, boolean isNeedResultMsg) {
        if (isRoot) {
            return execCommandWithSu(commands);
        } else {
            return execCommandWithSh(commands, isNeedResultMsg);
        }
    }

    public static CommandResult execCommandWithSh(String[] commands, boolean isNeedResultMsg) {

        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;

        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(COMMAND_SH);

            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }

                // donnot use os.writeBytes(commmand), avoid chinese charset error
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }

            os.writeBytes(COMMAND_EXIT);
            os.flush();

            result = process.waitFor();
            // get command result
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                    successMsg.append("\n");
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }

        return new CommandResult(result, successMsg == null ? null : successMsg.toString(),
                errorMsg == null ? null : errorMsg.toString());
    }

    public static CommandResult execCommandWithSu(String[] commands) {
        synchronized (lock) {
            int result = 0;
            if (commands == null || commands.length == 0) {
                return new CommandResult(result, null, null);
            }

            Process process;
            BufferedReader successResult;
            StringBuilder successMsg = null;

            DataOutputStream os;
            try {
                if (mSuProcess == null) {
                    mSuProcess = Runtime.getRuntime().exec(COMMAND_SU);
                }
                process = mSuProcess;

                os = new DataOutputStream(process.getOutputStream());
                for (String command : commands) {
                    if (command == null) {
                        continue;
                    }

                    // donnot use os.writeBytes(commmand), avoid chinese charset error
                    os.write(command.getBytes());
                    os.writeBytes(COMMAND_LINE_END);
                    os.flush();
                }

                os.writeBytes(COMMAND_ECHO_RETURN);
                os.flush();

                // get command result
                successMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String temp;

                while (true) {
                    temp = successResult.readLine();

                    if (temp.equals("return")) {
                        break;
                    }
                    successMsg.append(temp).append("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
                mSuProcess = null;
                result = -1;
            }

            return new CommandResult(result, successMsg == null ? null : successMsg.toString(), null);
        }
    }

    /**
     * result of command
     * <ul>
     * <li>{@link CommandResult#result} means result of command, 0 means normal, else means error, same to excute in
     * linux shell</li>
     * <li>{@link CommandResult#successMsg} means success message of command result</li>
     * <li>{@link CommandResult#errorMsg} means error message of command result</li>
     * </ul>
     *
     * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-5-16
     */
    public static class CommandResult {

        /**
         * result of command
         **/
        public int result;
        /**
         * success message of command result
         **/
        public String successMsg;
        /**
         * error message of command result
         **/
        public String errorMsg;

        public CommandResult(int result) {
            this.result = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }
}