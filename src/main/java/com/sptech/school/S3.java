package com.sptech.school;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class S3 {
    private final S3Client s3Client;
    private final String bucketName;

    public S3(String bucketName, Region region) {
        this.bucketName = bucketName;
        this.s3Client = S3Client.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public List<String> buscarUltimaLinha() {
        List<String> ultimasLinhas = new ArrayList<>();

        ListObjectsV2Response resposta = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build());

        for (S3Object obj : resposta.contents()) {
            if (obj.key().startsWith("capturaMaquina")) {
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(obj.key())
                        .build();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(s3Client.getObject(getObjectRequest), StandardCharsets.UTF_8))) {
                    String linha;
                    String ultimaLinha = null;

                    while ((linha = reader.readLine()) != null) {
                        ultimaLinha = linha;
                    }

                    if (ultimaLinha != null) {
                        ultimasLinhas.add(ultimaLinha);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ultimasLinhas;
    }

    public String buscarUltimaLinha(String maquina) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key("hercules/diario/" + maquina + ".csv")
                    .build();
            String csvContent = s3Client.getObject(getRequest, ResponseTransformer.toBytes()).asUtf8String();

            try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
                String linha;
                String ultimaLinha = null;

                while ((linha = reader.readLine()) != null) {
                    ultimaLinha = linha;
                }

                if (ultimaLinha != null) {
                    return ultimaLinha;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (S3Exception e) {
            if ("NoSuchKey".equals(e.awsErrorDetails().errorCode())) {
                return null;
            }
            throw new RuntimeException("Erro ao tentar obter o arquivo do S3: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Falha inesperada ao tentar obter o arquivo do S3.", e);
        }
        return null;
    }



    public void enviar(String csv, String maquina ) {
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key("hercules/diario/" + maquina + ".csv")
                .contentLength((long) bytes.length)
                .contentType("text/csv")
                .build();

        try {
            s3Client.putObject(putRequest, RequestBody.fromBytes(bytes));
        } catch (Exception e) {
            throw new RuntimeException("Falha no upload do arquivo CSV.", e);
        }
    }

    public String buscaCSV(String maquina) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key("hercules/diario/" + maquina + ".csv")
                    .build();
            return s3Client.getObject(getRequest, ResponseTransformer.toBytes())
                    .asUtf8String();

        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return null;
            } else {
                throw new RuntimeException("Erro ao tentar obter o arquivo do S3: " + e.awsErrorDetails().errorMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha inesperada ao tentar obter o arquivo do S3.", e);
        }
    }

    public String buscaChave(){
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key("chave.pem")
                    .build();
            return s3Client.getObject(getRequest, ResponseTransformer.toBytes())
                    .asUtf8String();

        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return null;
            } else {
                throw new RuntimeException("Erro ao tentar obter o arquivo do S3: " + e.awsErrorDetails().errorMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha inesperada ao tentar obter o arquivo do S3.", e);
        }
    }
}

