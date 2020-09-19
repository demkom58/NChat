package com.demkom58.nchat;

import com.demkom58.nchat.client.ClientBoot;
import com.demkom58.nchat.server.ServerExecutor;
import joptsimple.OptionParser;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {
        final OptionParser optionParser = new OptionParser();
        optionParser.acceptsAll(Arrays.asList("s", "server"));

        if (optionParser.parse(args).has("server"))
            ServerExecutor.main(args);
        else
            ClientBoot.main(args);
    }

}
