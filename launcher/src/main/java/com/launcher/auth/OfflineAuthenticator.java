package com.launcher.auth;

import java.util.UUID;

public class OfflineAuthenticator {
    public static class Session {
        public String username;
        public String uuid;
        public String accessToken;
        public String userType; // "mojang" or "msa" (Microsoft) or "legacy" (Offline)
    }

    public static Session login(String username) {
        System.out.println("Logging offline as " + username);

        Session session = new Session();
        session.username = username;
        // Generate UUID from username
        session.uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes()).toString();
        session.accessToken = "00000000-0000-0000-0000-000000000000"; // Dummy access token
        session.userType = "legacy";

        return session;
    }
}
