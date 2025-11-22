package com.sptech.school;

import java.sql.*;

public class ConexaoDB {
    private Connection conexao;

    public ConexaoDB(String ip, String db, String user, String password) {
        try {
            String url = "jdbc:mysql://" + ip + ":3306/" + db + "?useSSL=false&serverTimezone=UTC";
            this.conexao = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar ao banco de dados: " + e.getMessage(), e);
        }
    }


    public String buscarEmpresa(String idCaixa){
        String empresa = null;
        String sql = "SELECT e.Nome_Empresa FROM Empresa e " +
                "INNER JOIN Caixa c ON c.Fk_Empresa = e.Id_Empresa " +
                "WHERE c.Macaddress = ?";
        try (PreparedStatement pst = conexao.prepareStatement(sql)) {
            pst.setString(1, idCaixa);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                empresa = rs.getString(1);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return empresa;
    }
}
