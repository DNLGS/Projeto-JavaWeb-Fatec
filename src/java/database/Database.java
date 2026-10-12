package database;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {
    
    private final String bd = "projeto-jean-2026";
    private Connection con;

    public Database()
    {
        con = null;
    }

    public boolean OpenDatabase(String user, String password) throws SQLException 
    {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException erro) {
            throw new SQLException("Driver JDBC não encontrado.", erro);
        }
        con = DriverManager.getConnection(
            "jdbc:mysql://127.0.0.1/" + bd,
            user,
            password);

        return true;
    }

    public void CloseDatabase() throws SQLException{
        if (con != null) {
            con.close();
        }
    }

    public Connection conectar() throws SQLException {
        Properties config = new Properties();
        Path caminho = Path.of(System.getProperty("locadora.config", "/etc/tomcat10/projeto-jean.properties"));
        try (InputStream arquivo = Files.newInputStream(caminho)) {
            config.load(arquivo);
        } catch (IOException erro) {
            throw new SQLException("Não foi possível ler a configuração do banco.", erro);
        }
        String usuario = config.getProperty("user");
        String senha = config.getProperty("password");
        if (usuario == null || usuario.isBlank() || senha == null || senha.isBlank()) {
            throw new SQLException("Usuário e senha do banco são obrigatórios.");
        }
        OpenDatabase(usuario, senha);
        return con;
    }
    
    public Connection getConnection()
    {
        if (con == null) {
            return null;    
        }        
        return con;
    }
}
