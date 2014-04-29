package com.ecomnext;

import com.datastax.driver.core.utils.UUIDs;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.*;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoginDaoTest {

    private LoginDao dao;

    @Rule
    public CassandraCQLUnit cassandraCQLUnit = new CassandraCQLUnit(
            new ClassPathCQLDataSet("schema.cql","test"));

    @Before
    public void setDao() {
        dao = new LoginDao(CassandraFactory.getSession());
    }

    private String user = "pepe";
    private UUID ts = UUIDs.timeBased();
    private String area1 = "index";
    private String area2 = "backoffice";
    private String date = LocalDateTime.now().format(ISO_LOCAL_DATE);

    private void testLogin(int method) throws Exception {
        UUID ts1 = UUIDs.timeBased();
        switch (method) {
            case 1:
                dao.login1(user, ts1, area1);
                break;
            case 2:
                dao.login2(user, ts1, area1);
                break;
            case 3:
                dao.login3(user, ts1, area1);
                break;
            case 4:
                dao.login4(user, ts1, area1);
                break;
        }

        Map<String, List<UUID>> logins = dao.getLogins(user);
        assertEquals(1, logins.get(area1).size());
        assertTrue(logins.get(area1).contains(ts1));

        List<UUID> timestamps = dao.getTimestamps(
                user,
                UUIDs.startOf(UUIDs.unixTimestamp(ts)),
                UUIDs.endOf(UUIDs.unixTimestamp(UUIDs.timeBased()))
        ).get();
        assertEquals(1, timestamps.size());
        assertTrue(timestamps.contains(ts1));

        OptionalLong stats = dao.getStats(user, date);
        assertEquals(1, stats.orElse(0));
    }

    @Test
    public void testLogin1() throws Exception {
        testLogin(1);
    }

    @Test
    public void testLogin2() throws Exception {
        testLogin(2);
    }

    @Test
    public void testLogin3() throws Exception {
        testLogin(3);
    }

    @Test
    public void testLogin4() throws Exception {
        testLogin(4);
    }

    @Test
    public void testLoginBatch() throws Exception {
        List<Login> logins = Arrays.asList(new Login(user, area1), new Login(user, area1), new Login(user, area2));
        dao.loginBatch(logins);

        Map<String, List<UUID>> stored = dao.getLogins(user);
        assertEquals(2, stored.get(area1).size());
        assertEquals(1, stored.get(area2).size());
        logins.stream().filter(login -> area1.equals(login.getArea()))
                .forEach(login -> assertTrue(stored.get(area1).contains(login.getTs())));
        logins.stream().filter(login -> area2.equals(login.getArea()))
                .forEach(login -> assertTrue(stored.get(area2).contains(login.getTs())));

        List<UUID> timestamps = dao.getTimestamps(
                user,
                UUIDs.startOf(UUIDs.unixTimestamp(ts)),
                UUIDs.endOf(UUIDs.unixTimestamp(UUIDs.timeBased()))
        ).get();
        assertEquals(3, timestamps.size());
        logins.forEach(login -> assertTrue(timestamps.contains(login.getTs())));

        OptionalLong stats = dao.getStats(user, date);
        assertEquals(3, stats.orElse(0));
    }

    @Test
    public void testGetLogins() throws Exception {

    }

    @Test
    public void testGetStats() throws Exception {

    }

    @Test
    public void testGetTimestamps() throws Exception {

    }
}