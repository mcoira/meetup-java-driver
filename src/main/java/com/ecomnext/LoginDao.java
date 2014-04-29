package com.ecomnext;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.stream.Collectors.*;

/**
 *
 */
public class LoginDao {

    private Session session;

    public LoginDao(Session session) {
        this.session = session;
        pInsert = session.prepare(INSERT);
        pIncrease = session.prepare(INCREASE);
        pIncreaseN = session.prepare(INCREASE_N);
        pSelLogin = session.prepare(GET_LOGINS);
        pGetStats = session.prepare(GET_STATS);
        pGetTs = session.prepare(GET_TS);
    }

    public void login1(String user, UUID ts, String area) {
        session.execute("INSERT INTO login (user, ts, area) VALUES (?, ?, ?) USING TTL 2592000", user, ts, area);
        session.execute("UPDATE loginStats SET count=count+1 WHERE user=? AND date = ?",
                user, LocalDateTime.now().format(ISO_LOCAL_DATE)
        );
    }

    public void login2(String user, UUID ts, String area) {
        ResultSetFuture rsf1 = session.executeAsync(
                "insert into login (user, ts, area) values (?, ?, ?) USING TTL 2592000", user, ts, area
        );
        ResultSetFuture rsf2 = session.executeAsync("UPDATE loginStats SET count=count+1 WHERE user=? AND date = ?",
                user, LocalDateTime.now().format(ISO_LOCAL_DATE)
        );
        rsf1.getUninterruptibly();
        rsf2.getUninterruptibly();
    }

    private static final String INSERT = QueryBuilder.insertInto("login").value("user", QueryBuilder.bindMarker())
            .value("ts", QueryBuilder.bindMarker())
            .value("area", QueryBuilder.bindMarker()).using(QueryBuilder.ttl(2592000)).getQueryString();

    private static final String INCREASE = QueryBuilder.update("loginStats")
            .with(incr("count")).where(eq("user", bindMarker()))
            .and(eq("date", bindMarker())).getQueryString();

    public void login3(String user, UUID ts, String area) {
        ResultSetFuture rsf1 = session.executeAsync(INSERT, user, ts, area);
        ResultSetFuture rsf2 = session.executeAsync(INCREASE, user, LocalDateTime.now().format(ISO_LOCAL_DATE));
        rsf1.getUninterruptibly();
        rsf2.getUninterruptibly();
    }

    // A PreparedStatement object allows you to define specific defaults
    // for the different properties of a Statement (Consistency level, tracing, ...)
    private PreparedStatement pInsert;
    private PreparedStatement pIncrease;

    public void login4(String user, UUID ts, String area) {
        ResultSetFuture rsf1 = session.executeAsync(
                pInsert.bind(user, ts, area)
        );
        ResultSetFuture rsf2 = session.executeAsync(
                pIncrease.bind(user, LocalDateTime.now().format(ISO_LOCAL_DATE))
        );
        rsf1.getUninterruptibly();
        rsf2.getUninterruptibly();
    }

    private static final String INCREASE_N = QueryBuilder.update("loginStats")
            .with(incr("count", bindMarker())).where(eq("user", bindMarker()))
            .and(eq("date", bindMarker())).getQueryString();

    private PreparedStatement pIncreaseN;

    public void loginBatch(List<Login> logins) {
        // Batches are atomic by default and there is a performance penalty for atomicity (30% aprox).
        // If you do not want to incur this penalty, prevent Cassandra from writing to the batchlog system
        // by using the UNLOGGED option
        BatchStatement batch1 = new BatchStatement(BatchStatement.Type.UNLOGGED);
        logins.stream().forEach(login -> batch1.add(pInsert.bind(login.getUser(), login.getTs(), login.getArea())));
        ResultSetFuture rsf1 = session.executeAsync(batch1);

        BatchStatement batch2 = new BatchStatement(BatchStatement.Type.COUNTER);

        // naive method
//        logins.stream().forEach(login ->
//               batch2.add(pIncrease.bind(login.getUser(), LocalDateTime.now().format(ISO_LOCAL_DATE)))
//        );

        // more efficient method
        Map<String, Long> collect = logins.stream().collect(groupingBy(Login::getUser, Collectors.counting()));
        collect.forEach((user, hits) -> batch2.add(pIncreaseN.bind(hits, user, LocalDateTime.now().format(ISO_LOCAL_DATE))));

        ResultSetFuture rsf2 = session.executeAsync(batch2);

        rsf1.getUninterruptibly();
        rsf2.getUninterruptibly();
    }

    private static final String GET_LOGINS = QueryBuilder.select("ts", "area").from("login")
            .where(eq("user", bindMarker())).getQueryString();

    private PreparedStatement pSelLogin;

    /**
     * @return a map with all logins for each area.
     */
    public Map<String, List<UUID>> getLogins(String user) {
        List<Row> all = session.executeAsync(pSelLogin.bind(user)).getUninterruptibly().all();
        return all.stream().collect(groupingBy(
                row -> row.getString("area"),
                mapping((Row row) -> row.getUUID("ts"), toList()))
        );
    }

    private static final String GET_STATS = QueryBuilder.select("count").from("loginStats")
            .where(eq("user", bindMarker())).and(eq("date", bindMarker())).getQueryString();

    private PreparedStatement pGetStats;

    public OptionalLong getStats(String user, String date) {
        Row row = session.executeAsync(pGetStats.bind(user, date)).getUninterruptibly().one();
        if (row != null) {
            return OptionalLong.of(row.getLong("count"));
        } else {
            return OptionalLong.empty();
        }
    }

    private static final String GET_TS = QueryBuilder.select("ts").from("login")
            .where(eq("user", bindMarker())).and(gte("ts", bindMarker())).and(lt("ts", bindMarker()))
            .getQueryString();

    private PreparedStatement pGetTs;

    public ListenableFuture<List<UUID>> getTimestamps(String user, UUID start, UUID end) {
        ResultSetFuture rsf = session.executeAsync(pGetTs.bind(user, start, end));
        return Futures.transform(rsf, (ResultSet rs) ->
                  StreamSupport.stream(Spliterators.spliteratorUnknownSize(rs.iterator(), 0), false)
                          .map(row -> row.getUUID("ts")).collect(toList())
//                rs.all().stream().map(row -> row.getUUID("ts")).collect(toList())
        );
    }

}
