package com.ecomnext;

import com.datastax.driver.core.utils.UUIDs;

import java.util.UUID;

/**
 * Created by mikel on 28/04/14.
 */
public class Login {
    private String user;
    private UUID ts;
    private String area;

    public Login(String user, String area) {
        this.user = user;
        this.ts = UUIDs.timeBased();
        this.area = area;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public UUID getTs() {
        return ts;
    }

    public void setTs(UUID ts) {
        this.ts = ts;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @Override
    public String toString() {
        return "Login{" +
                "user='" + user + '\'' +
                ", ts=" + ts +
                ", area='" + area + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Login login = (Login) o;

        if (area != null ? !area.equals(login.area) : login.area != null) return false;
        if (ts != null ? !ts.equals(login.ts) : login.ts != null) return false;
        if (user != null ? !user.equals(login.user) : login.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (ts != null ? ts.hashCode() : 0);
        result = 31 * result + (area != null ? area.hashCode() : 0);
        return result;
    }
}
