package com.hutoma.api.tools;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Created by pedrotei on 16/03/17.
 */
public class AiClientTokenBuilder {

    public static void main(String[] args) {
        Options options = new Options();

        Option devIdOpt = new Option("devid", true, "Developer ID");
        devIdOpt.setRequired(true);
        options.addOption(devIdOpt);

        Option aiidOpt = new Option("aiid", true, "AI ID");
        aiidOpt.setRequired(true);
        options.addOption(aiidOpt);

        Option encOpt = new Option("enc", true, "Encoding Key");
        encOpt.setRequired(true);
        options.addOption(encOpt);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
            formatter.printHelp("TokenBuilder", options);
            System.exit(1);
        }

        String encodingKey = cmdLine.getOptionValue("enc");
        String devId = cmdLine.getOptionValue("devid");
        String aiid = cmdLine.getOptionValue("aiid");

        String token = Jwts.builder()
                .claim("ROLE", "ROLE_CLIENTONLY")
                .claim("AIID", aiid)
                .setSubject(devId)
                .compressWith(CompressionCodecs.DEFLATE)
                .signWith(SignatureAlgorithm.HS256, encodingKey)
                .compact();

        System.out.println(String.format("\n------\nClient token for AI id %s (devId: %s)\n------\n%s\n",
                aiid, devId, token));
    }
}
