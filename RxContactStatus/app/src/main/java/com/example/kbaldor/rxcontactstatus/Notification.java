package com.example.kbaldor.rxcontactstatus;

/**
 * Created by kbaldor on 7/29/16.
 */
public class Notification {
    public static class LogIn extends Notification {
        public final String username;
        public LogIn(String username){this.username = username;}
    }
    public static class LogOut extends Notification {
        public final String username;
        public LogOut(String username){this.username = username;}
    }
}
