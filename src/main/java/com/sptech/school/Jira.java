package com.sptech.school;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public String buscarUltimoChamadoAberto(String empresa, String maquina, String tipo) throws Exception {
        String campoEmpresa = "empresa";
        String campoMaquina = "maquina";
        String campoTipo = "tipo";

        String statusAberto = "To Do"; // status padrão para aberto

        String jql = String.format("\"%s\" ~ \"%s\" AND \"%s\" ~ \"%s\" AND \"%s\" ~ \"%s\" AND status=\"%s\" ORDER BY created DESC",
                campoEmpresa, empresa,
                campoMaquina, maquina,
                campoTipo, tipo,
                statusAberto);

        String url = baseUrl + "rest/api/2/search?jql=" + URLEncoder.encode(jql, "UTF-8") + "&maxResults=1&fields=id";

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
        String dataAtual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        String json = """
        {
            "fields": {
                "project": {"key": "CHAMADO"},
                "summary": "Ausência no envio.",
                "description": "O caixa %s deixou de enviar as informações na data %s",
                "tipo": "falta",
                "empresa": "%s",
                "maquina": "%s"
            }
        }
        """.formatted(maquina, dataAtual,
                empresa, maquina);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Authorization", auth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString()).body();
    }


    public String encerrarChamado(String issueKey) throws Exception {
        String transitionId = "";
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

