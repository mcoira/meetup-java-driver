package com.ecomnext;

import com.datastax.driver.core.utils.UUIDs;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by mikel on 29/04/14.
 */
public class App {
    public static void main(String[] args) {
        String user = "pepe";
        UUID ts = UUIDs.timeBased();
        String area = "index";
        String area2 = "backoffice";

        LoginDao dao = new LoginDao(CassandraFactory.getSession());

        dao.login1(user, ts, area);
        dao.login2(user, ts, area);
        dao.login3(user, ts, area);
        dao.login4(user, ts, area);

        List<Login> logins = Arrays.asList(new Login(user, area), new Login(user, area), new Login(user, area2));
        dao.loginBatch(logins);


    }
}
