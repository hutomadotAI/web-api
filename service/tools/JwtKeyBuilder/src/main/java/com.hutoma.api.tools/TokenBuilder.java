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
public class TokenBuilder {

    public static void main(String[] args) {
        Options options = new Options();

        Option devIdOpt = new Option("devid", true, "Developer Id");
        devIdOpt.setRequired(true);
        options.addOption(devIdOpt);

        Option encOpt = new Option("enc", true, "Encoding Key");
        encOpt.setRequired(true);
        options.addOption(encOpt);

        Option roleOpt = new Option("role", true, "Role");
        roleOpt.setRequired(true);
        options.addOption(roleOpt);

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
        String role = cmdLine.getOptionValue("role");

        String devToken = Jwts.builder()
                .claim("ROLE", role)
                .setSubject(devId)
                .compressWith(CompressionCodecs.DEFLATE)
                .signWith(SignatureAlgorithm.HS256, encodingKey)
                .compact();

        String clientToken = Jwts.builder()
                .claim("ROLE", "ROLE_CLIENTONLY")
                .setSubject(devId)
                .compressWith(CompressionCodecs.DEFLATE)
                .signWith(SignatureAlgorithm.HS256, encodingKey)
                .compact();

        System.out.println(String.format("\n------\nTokens for id %s (role: %s)\n------\n"
                        + "DevToken: %s\nClientToken: %s\n",
                devId, role, devToken, clientToken));
    }
}
