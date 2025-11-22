package com.sptech.school;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

public class Jira {

    private final String baseUrl;
    private final String auth;

    public Jira(String baseUrl, String user, String apiToken) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.auth = "Basic " + Base64.getEncoder().encodeToString((user + ":" + apiToken).getBytes(StandardCharsets.UTF_8));
    }

    public String buscarUltimoChamadoAberto(String maquina) throws Exception {
        String url = baseUrl + "rest/api/3/search/jql";

        String json = """
            {
              "jql": "project = CHAMADO AND status = \\"Open\\" AND text ~ \\"%s\\" AND text ~ \\"falta\\" ORDER BY created DESC",
              "maxResults": 1,
              "fields": ["id"]
            }
            """.formatted(maquina);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Authorization", auth)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.out.println("Erro HTTP ao buscar chamado: " + response.statusCode());
            System.out.println("Resposta: " + response.body());
            return null;
        }

        JSONObject jsonResponse = new JSONObject(response.body());
        if (!jsonResponse.has("issues")) {
            System.out.println("JSON sem campo 'issues': " + jsonResponse.toString(2));
            return null;
        }
        JSONArray issues = jsonResponse.getJSONArray("issues");
        if (issues.isEmpty()) {
            return null;
        }
        JSONObject ultimoChamado = issues.getJSONObject(0);
        return ultimoChamado.getString("id");
    }

    public void criarChamado(String empresa, String maquina) throws Exception {
        String url = baseUrl + "rest/api/2/issue";

        String json = """
            {
                "fields": {
                    "project": {
                        "key": "CHAMADO"
                    },
                    "summary": "Alerta: REDE | Sem envio",
                    "issuetype": {
                        "name": "Task"
                    },
                    "description": "ID ATM: %s",
                    "customfield_10089": "falta",
                    "customfield_10087": "%s",
                    "customfield_10088": "%s"
                }
            }
            """.formatted(maquina, empresa, maquina);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Authorization", auth)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
        response.body();
    }

    public void encerrarChamado(String issueKey) throws Exception {
        String transitionId = "61";
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
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
        response.body();
    }
}
