package com.sptech.school;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.regions.Region;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Handler implements RequestHandler<Object, String> {
    private final S3 s3Origem;
    private final S3 s3Destino;
    private final S3 s3Chave;
    private final Jira jira;
    private final ConexaoDB conexaoDB;

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
        String bucketChave = System.getenv("BUCKET_CHAVE");
        this.s3Origem = new S3(bucketOrigem, region);
        this.s3Destino = new S3(bucketDestino, region);
        this.jira = new Jira(jiraUrl, jiraUsername, jiraApiKey);
        this.conexaoDB = new ConexaoDB(dbIp, dbName, dbUsername, dbPassword);
        this.s3Chave = new S3(bucketChave, region);
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

            for (String linha : csvs) {
                campos = linha.split(";");

                macaddress = campos[0];

                LocalDateTime ldt = LocalDateTime.parse(campos[1], formatter);
                ZonedDateTime zdt = ldt.atZone(ZoneId.of("America/Sao_Paulo"));
                datetime = Date.from(zdt.toInstant());

                usuario = campos[9];
                ip = campos[10];
                isp = campos[11];
                Coleta coleta = new Coleta(usuario, macaddress, datetime, ip, isp);

                String empresa = conexaoDB.buscarEmpresa(coleta.getMacaddress());
                coleta.setEmpresa(empresa);

                ZoneId zona = ZoneId.of("America/Sao_Paulo");
                Date data = Date.from(ZonedDateTime.now(zona).toInstant());

                long diffMillis = Math.abs(data.getTime() - coleta.getDatetime().getTime());
                long diffSeconds = diffMillis / 1000;

                String idChamado = jira.buscarUltimoChamadoAberto(coleta.getMacaddress());

                if (diffSeconds > 70) {
                    if (idChamado == null) {
                        jira.criarChamado(empresa, coleta.getMacaddress());
                        //Reparo reparo = new Reparo(coleta.getIp_publico(), coleta.getUsuario(), s3Chave.buscaChave());
                        //reparo.reparar();
                    }
                } else {
                    if (idChamado != null) {
                        jira.encerrarChamado(idChamado);
                    }
                }

                String ultimaLinha = s3Destino.buscarUltimaLinha(coleta.getMacaddress());

                if (!Objects.equals(ultimaLinha, coleta.toCsvRow())) {
                    String csv = s3Destino.buscaCSV(coleta.getMacaddress());
                    String csvCompleto = coleta.montaCSV(csv);
                    s3Destino.enviar(csvCompleto, coleta.getMacaddress());
                }

                System.out.println("diffSeconds: " + diffSeconds + ", idChamado: " + idChamado + " " + coleta.getDatetime());

            }
            return "Processamento terminado com sucesso.";
        } catch (Exception e) {
            System.out.println(e);
            return "Processamento falhou.";
        }
    }
}
