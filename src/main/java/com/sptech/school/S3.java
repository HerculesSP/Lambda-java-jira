package com.sptech.school;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class S3 {
    private S3Client s3Client;
    private String bucketName;

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


    public void enviar(Path caminhoLocal, String key) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(caminhoLocal));
    }

}

