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

    public String buscarEmpresa(String idCaixa){
        try {
            String sql = "SELECT e.Nome_Empresa FROM Empresa e " +
                    "INNER JOIN Caixa c ON c.Fk_Empresa = e.Id_Empresa" +
                    "WHERE c.Id_Caixa = " + idCaixa;
            ResultSet rs = conexao.createStatement().executeQuery(sql);
                return rs.getString(1);
        } catch (SQLException e) {
            System.out.println(e);
        }
        return null;
    }
}
