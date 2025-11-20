package com.sptech.school;

import java.sql.*;

public class ConexaoDB {
    private Connection conexao;

    public ConexaoDB(String ip, String db,  String user, String password) {
        try {
            String url = "jdbc:mysql://"+ ip + "/" + db;
            Connection conexao = DriverManager.getConnection(url, user, password);
            this.conexao = conexao;
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}
