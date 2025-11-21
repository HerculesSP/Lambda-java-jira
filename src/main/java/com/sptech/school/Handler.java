package com.sptech.school;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.regions.Region;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class Handler implements RequestHandler<Object, String> {
    private S3 s3Origem;
    private S3 s3Destino;
    private Jira jira;
    private ConexaoDB conexaoDB;


    public Handler() {
        String bucketOrigem = System.getenv("BUCKET_ORIGEM");
        String bucketDestino = System.getenv("BUCKET_DESTINO");
        Region region = Region.of(System.getenv("AWS_REGION"));
        String jiraUrl = System.getenv("AWS_JIRA_URL");
        String jiraUsername = System.getenv("AWS_JIRA_USERNAME");
        String jiraApiKey = System.getenv("AWS_JIRA_API_KEY");
        String dbIp = System.getenv("DB_IP");
        String dbUsername = System.getenv("DB_USERNAME");
        String dbName = System.getenv("DB_NAME");
        String dbPassword = System.getenv("DB_PASSWORD");
        this.s3Origem = new S3(bucketOrigem, region);
        this.s3Destino = new S3(bucketDestino, region);
        this.jira = new Jira(jiraUrl, jiraUsername, jiraApiKey);
        this.conexaoDB = new ConexaoDB(dbIp, dbName, dbUsername, dbPassword);
    }

    @Override
    public String handleRequest(Object input, Context context) {
        try {
            String macaddress;
            Date datetime;
            String usuario;
            String ip;
            String isp;
            List<String> csvs = s3Origem.buscarUltimaLinha();
            String[] campos;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            for (String linha : csvs) {
                campos = linha.split(",");
                macaddress = campos[0];
                LocalDateTime ldt = LocalDateTime.parse(campos[1], formatter);
                datetime = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                usuario = campos[9];
                ip = campos[10];
                isp = campos[11];
                Coleta coleta = new Coleta(usuario, macaddress, datetime, ip, isp);
                long diffMillis = Math.abs(date.getTime() - coleta.getDatetime().getTime());
                long diffSeconds = diffMillis / 1000;
                String idChamado = jira.buscarUltimoChamadoAberto(coleta.getMacaddress());
                if (diffSeconds > 70) {
                    String empresa = conexaoDB.buscarEmpresa(coleta.getMacaddress());
                    if (idChamado == null) {
                        jira.criarChamado(empresa, coleta.getMacaddress());
                        // chamar aqui a lambda para arrumar, sei lá como fazwer isso
                    }
                } else {
                    if (idChamado != null) {
                        jira.encerrarChamado(idChamado);
                    }
                }
                String csv = s3Origem.buscaCSV(coleta.getMacaddress());
                String csvCompleto = coleta.montaCSV(csv);
                s3Destino.enviar(csvCompleto, coleta.getMacaddress());
            }
            return "Processamento terminado com sucesso.";
        } catch (Exception e) {
            return "Processamento falhou.";
        }
    }
}

