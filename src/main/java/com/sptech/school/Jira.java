package com.sptech.school;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URLEncoder;

public class Jira {

    private final String baseUrl;
    private final String auth;

    public Jira(String baseUrl, String user, String apiToken) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.auth = "Basic " + java.util.Base64.getEncoder().encodeToString((user + ":" + apiToken).getBytes());
    }

    public String buscarUltimoChamadoAberto(String maquina) throws Exception {

        String jql = String.format("customfield_10088 = \"%s\" AND customfield_10089 = falta AND status in ('In progress', 'To do') ORDER BY created DESC",
                maquina);

        String url = baseUrl + "rest/api/2/search?jql=" + URLEncoder.encode(jql, StandardCharsets.UTF_8) + "&maxResults=1&fields=id";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Authorization", auth)
                .header("Accept", "application/json")
                .GET()
                .build();

        String response = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString()).body();

        JSONObject jsonResponse = new JSONObject(response);
        JSONArray issues = jsonResponse.getJSONArray("issues");

        if (!issues.isEmpty()) {
            return issues.getJSONObject(0).getString("id");
        }
        return null;
    }

    public String criarChamado(String empresa, String maquina) throws Exception {
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
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString()).body();
    }



    public String encerrarChamado(String issueKey) throws Exception {
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
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString()).body();
    }
}

