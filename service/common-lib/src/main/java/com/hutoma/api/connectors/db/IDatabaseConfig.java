package com.hutoma.api.connectors.db;

public interface IDatabaseConfig {
    String getDatabaseConnectionString();

    int getDatabaseConnectionPoolMinimumSize();

    int getDatabaseConnectionPoolMaximumSize();

    /***
     * Throw an exception if we are using a username that starts with 'admin' or 'root'
     * @param value connection string
     * @return same connection string
     * @throws Exception if username is admin... or root...
     */
    static String enforceNewDBCredentials(String value) throws Exception {
        int startUserName = value.indexOf("user=");
        if (startUserName >= 0) {
            String prefixUsername = value.substring(startUserName + ("user=".length())).toLowerCase();
            if ((prefixUsername.startsWith("admin")) || (prefixUsername.startsWith("root"))) {
                throw new Exception(
                        "db connection string uses root/admin access. please update your config properties file.");
            }
        }
        return value;
    }
}
