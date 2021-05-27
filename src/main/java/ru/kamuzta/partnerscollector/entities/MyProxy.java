package ru.kamuzta.partnerscollector.entities;

import java.util.Objects;

public class MyProxy {
    private String ip;
    private int port;

    public MyProxy(String ip, String port) {
        if (ip == null || port == null)
            throw new IllegalArgumentException();
        this.ip = ip;
        this.port = Integer.parseInt(port);
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return ip + ':' + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyProxy myProxy = (MyProxy) o;
        return port == myProxy.port &&
                Objects.equals(ip, myProxy.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }
}
