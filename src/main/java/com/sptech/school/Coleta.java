package com.sptech.school;

import java.util.Date;

public class Coleta {
    private String usuario;
    private String macaddress;
    private Date datetime;
    private String ip_publico;
    private String isp;

    public Coleta(String usuario, String macaddress, Date datetime, String ip_publico, String isp) {
        this.usuario = usuario;
        this.macaddress = macaddress;
        this.datetime = datetime;
        this.ip_publico = ip_publico;
        this.isp = isp;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getMacaddress() {
        return macaddress;
    }

    public Date getDatetime() {
        return datetime;
    }

    public String getIp_publico() {
        return ip_publico;
    }

    public String getIsp() {
        return isp;
    }
}
