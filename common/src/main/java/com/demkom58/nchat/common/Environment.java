package com.demkom58.nchat.common;

import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
public class Environment {
    public static final int PORT = 55555;

    public static final String PROTOCOL_VERSION = "1.2b4";
    public static final String STYLING_VERSION = "1.1";
    public static final String APP_VERSION = "1.1 - SNAPSHOT";

    public static final int MAX_MESSAGE_LENGTH = 1000;
    public static final String STANDARD_IP = "localhost";

    public static final int CONNECTIONS_PER_IP = 3;
    public static final int MESSAGES_PER_SECOND = 3;
    public static final File DATA_DIRECTORY = new File(System.getenv("APPDATA"), "NChat");
}
