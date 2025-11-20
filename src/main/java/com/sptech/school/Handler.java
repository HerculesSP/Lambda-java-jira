package com.sptech.school;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.regions.Region;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Handler implements RequestHandler<Object, String> {
    private S3 s3Origem;
    private S3 s3Destino;
    private Jira jira;


    public Handler() {
        String bucketOrigem = System.getenv("BUCKET_ORIGEM");
        String bucketDestino = System.getenv("BUCKET_DESTINO");
        Region region = Region.of(System.getenv("AWS_REGION"));
        String jiraUrl = System.getenv("AWS_JIRA_URL");
        String jiraUsername = System.getenv("AWS_JIRA_USERNAME");
        String jiraApiKey = System.getenv("AWS_JIRA_API_KEY");
        this.s3Origem = new S3(bucketOrigem, region);
        this.s3Destino = new S3(bucketDestino, region);
        this.jira = new Jira(jiraUrl, jiraUsername, jiraApiKey);
    }

    @Override
    public String handleRequest(Object input, Context context) {
        try {
            List<String> csvs = s3Origem.buscarUltimaLinha();

            for (String chave : csvs) {
                System.out.println(chave);
                //Path tempFile = Files.createTempFile("tmp-", ".csv");
            }

            return "Processamento terminado com sucesso.";
        } catch (Exception e) {
            return "Processamento falhou.";
        }
    }
}

