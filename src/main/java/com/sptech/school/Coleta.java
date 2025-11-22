package com.sptech.school;

import java.util.Date;

public class Coleta {
    private final String usuario;
    private final String macaddress;
    private final Date datetime;
    private final String ip_publico;
    private final String isp;
    private String empresa;

    public Coleta(String usuario, String macaddress, Date datetime, String ip_publico, String isp) {
        this.usuario = usuario;
        this.macaddress = macaddress;
        this.datetime = datetime;
        this.ip_publico = ip_publico;
        this.isp = isp;
    }

    public String getMacaddress() {
        return macaddress;
    }

    public Date getDatetime() {
        return datetime;
    }

    public static String getCsvHeader() {
        return "usuario,macaddress,datetime,ip_publico,isp,empresa\n";
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String toCsvRow() {
        return usuario + "," + macaddress + "," + datetime + "," + ip_publico + "," + isp + empresa + "\n";
    }

    public String montaCSV(String existingContent) {
        String conteudoFinal;

        if (existingContent != null && !existingContent.isEmpty()) {
            conteudoFinal = existingContent + toCsvRow();
        } else {
            conteudoFinal = Coleta.getCsvHeader() + toCsvRow();
        }
        return conteudoFinal;
    }
}
