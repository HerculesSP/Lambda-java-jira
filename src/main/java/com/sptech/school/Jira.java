package com.sptech.school;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Jira {

    private final String baseUrl;
    private final String auth;

    public Jira(String baseUrl, String user, String apiToken) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.auth = "Basic " + java.util.Base64.getEncoder().encodeToString((user + ":" + apiToken).getBytes());
    }

    public String buscarChamados(String jql) throws Exception {
        String url = baseUrl + "rest/api/2/search?jql=" + java.net.URLEncoder.encode(jql, "UTF-8");
        HttpRequest req = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Authorization", auth)
                .header("Accept", "application/json")
                .GET()
                .build();
        return HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    public String criarChamado(String projetoKey, String resumo, String descricao, String tipo,
                               String empresa, String maquina) throws Exception {
        String url = baseUrl + "rest/api/2/issue";
        String json = """
            {
                "fields": {
                    "project": {"key": "%s"},
                    "summary": "%s",
                    "description": "%s",
                    "issuetype": {"name": "%s"},
                    "empresa": "%s",
                    "maquina": "%s"
                }
            }
            """.formatted(projetoKey, resumo, descricao, tipo,
                empresa, maquina);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Authorization", auth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString()).body();
    }


    public String encerrarChamado(String issueKey, String transitionId) throws Exception {
        String url = baseUrl + "rest/api/2/issue/" + issueKey + "/transitions";
        String json = """
            {
                "transition": {"id": "%s"}
            }
            """.formatted(transitionId);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Authorization", auth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString()).body();
    }
}

