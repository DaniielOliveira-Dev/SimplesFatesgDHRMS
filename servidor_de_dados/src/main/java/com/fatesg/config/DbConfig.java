package com.fatesg.config;

public record DbConfig() {
        // TODO: Mover para um arquivo de configuração
        public static final String URL = System.getenv("FATESG_DB_URL") != null ? System.getenv("FATESG_DB_URL")
                        : "jdbc:mysql://localhost:3306/employees";
        public static final String USERNAME = System.getenv("FATESG_DB_USERNAME") != null
                        ? System.getenv("FATESG_DB_USERNAME")
                        : "root";
        public static final String PASSWORD = System.getenv("FATESG_DB_PASSWORD") != null
                        ? System.getenv("FATESG_DB_PASSWORD")
                        : "03102018";
        public static final String DRIVER = "com.mysql.cj.jdbc.Driver";
        public static final int DEFAULT_LIMIT = 15;
}
