package com.sptech.school;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.regions.Region;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Handler implements RequestHandler<Object, String> {
    private final S3 s3Origem;
    private final S3 s3Destino;


    public Handler() {
        String bucketOrigem = System.getenv("BUCKET_ORIGEM");
        String bucketDestino = System.getenv("BUCKET_DESTINO");
        Region region = Region.of(System.getenv("AWS_REGION"));
        this.s3Origem = new S3(bucketOrigem, region);
        this.s3Destino = new S3(bucketDestino, region);
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

