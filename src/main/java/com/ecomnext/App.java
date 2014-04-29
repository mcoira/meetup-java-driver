package com.ecomnext;

import com.datastax.driver.core.utils.UUIDs;

import java.util.Arrays;
import java.util.List;

public class App {
    public static void main(String[] args) {
        String user = "pepe";
        String area = "index";
        String area2 = "backOffice";

        LoginDao dao = new LoginDao(CassandraFactory.getSession());

        dao.login1(user, UUIDs.timeBased(), area);
        dao.login2(user, UUIDs.timeBased(), area);
        dao.login3(user, UUIDs.timeBased(), area);
        dao.login4(user, UUIDs.timeBased(), area);

        List<Login> logins = Arrays.asList(new Login(user, area), new Login(user, area), new Login(user, area2));
        dao.loginBatch(logins);

        System.exit(0);

    }
}
