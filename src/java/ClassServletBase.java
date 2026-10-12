import dao.ClienteDao;
import dao.EnderecoDao;
import dao.FilmeDao;
import dao.FuncionarioDao;
import dao.LocacaoDao;
import database.Database;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.Cliente;
import models.Endereco;
import models.Filme;
import models.Funcionario;
import models.Locacao;
import models.Pessoa;

@WebServlet(urlPatterns = {"/ClassServletBase"})
public class ClassServletBase extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        String tipo = request.getParameter("tipo");
        try {
            pagina(tipo);
            Database database = new Database();
            try (Connection con = database.conectar()) {
                switch (tipo) {
                    case "endereco":
                        Endereco endereco = new Endereco();
                        endereco.setLogradouro(texto(request, "logradouro", 255, true));
                        endereco.setBairro(texto(request, "bairro", 150, false));
                        endereco.setNumero(texto(request, "numero", 30, false));
                        String ibge = texto(request, "ibge", 7, true);
                        String cep = texto(request, "cep", 8, true);
                        if (!ibge.matches("[0-9]{7}") || !cep.matches("[0-9]{8}")) {
                            throw new IllegalArgumentException("Informe IBGE com 7 dígitos e CEP com 8 dígitos.");
                        }
                        endereco.setIbge(Integer.parseInt(ibge));
                        endereco.setCep(Integer.parseInt(cep));
                        new EnderecoDao(con).insert(endereco);
                        break;
                    case "cliente":
                        Cliente cliente = new Cliente();
                        preencherPessoa(request, cliente);
                        new ClienteDao(con).insert(cliente);
                        break;
                    case "funcionario":
                        Funcionario funcionario = new Funcionario();
                        preencherPessoa(request, funcionario);
                        funcionario.setCargo(texto(request, "cargo", 100, false));
                        funcionario.setNivel_acesso(inteiro(request.getParameter("nivel_acesso"), "Nível de acesso", 0));
                        new FuncionarioDao(con).insert(funcionario);
                        break;
                    case "filme":
                        Filme filme = new Filme();
                        filme.setTitulo(texto(request, "titulo", 255, true));
                        filme.setGenero(texto(request, "genero", 100, false));
                        filme.setNota_imb(decimal(request, "nota_imb", "10", 1).doubleValue());
                        filme.setValor_dia(decimal(request, "valor_dia", "9999999999.99", 2).doubleValue());
                        filme.setObservacoes(texto(request, "observacoes", 2000, false));
                        new FilmeDao(con).insert(filme);
                        break;
                    case "locacao":
                        Locacao locacao = new Locacao();
                        Cliente locatario = new Cliente();
                        locatario.setId(inteiro(request.getParameter("cliente_id"), "Código do cliente", 1));
                        locacao.setCliente(locatario);
                        locacao.setQtd_dias(inteiro(request.getParameter("qtd_dias"), "Quantidade de dias", 1));
                        LocalDate devolucao = data(request, "data_devolucao");
                        if (devolucao != null && devolucao.isBefore(locacao.getData_cadastro())) {
                            throw new IllegalArgumentException("A devolução não pode ser anterior à locação.");
                        }
                        locacao.setData_devolucao(devolucao);
                        Set<Integer> ids = new HashSet<>();
                        for (String codigo : texto(request, "filmes", 1000, true).split(",", -1)) {
                            int id = inteiro(codigo.trim(), "Código do filme", 1);
                            if (!ids.add(id)) {
                                throw new IllegalArgumentException("Informe cada filme apenas uma vez.");
                            }
                            Filme fita = new Filme();
                            fita.setId(id);
                            locacao.addFilme(fita);
                        }
                        new LocacaoDao(con).insert(locacao);
                        break;
                }
            }
            response.sendRedirect(request.getContextPath() + "/ClassServletBase?tipo=" + tipo + "&salvo=1");
        } catch (IllegalArgumentException erro) {
            erro(response, 400, erro.getMessage());
        } catch (SQLException erro) {
            falhaBanco(response, erro);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String tipo = request.getParameter("tipo");
        String sql;
        String pagina;
        String busca;
        try {
            pagina = pagina(tipo);
            busca = texto(request, "q", 200, false);
            switch (tipo) {
                case "endereco":
                    sql = "SELECT id AS Código, logradouro AS Logradouro, bairro AS Bairro, numero AS Número, "
                            + "LPAD(ibge, 7, '0') AS IBGE, LPAD(cep, 8, '0') AS CEP FROM endereco "
                            + "WHERE CONCAT(logradouro, ' ', COALESCE(bairro, '')) LIKE ? ORDER BY id DESC";
                    break;
                case "cliente":
                    sql = "SELECT c.id AS Código, c.nome AS Nome, c.documento AS Documento, "
                            + "COALESCE(CONCAT(e.id, ' - ', e.logradouro, ', ', COALESCE(e.numero, '')), 'Sem endereço') AS Endereço, "
                            + "c.dt_nascimento AS Nascimento, c.dt_cadastro AS Cadastro FROM cliente c "
                            + "LEFT JOIN endereco e ON e.id = c.endereco_id "
                            + "WHERE CONCAT(c.nome, ' ', c.documento) LIKE ? ORDER BY c.id DESC";
                    break;
                case "funcionario":
                    sql = "SELECT c.id AS Código, c.nome AS Nome, c.documento AS Documento, "
                            + "COALESCE(CONCAT(e.id, ' - ', e.logradouro, ', ', COALESCE(e.numero, '')), 'Sem endereço') AS Endereço, "
                            + "c.dt_nascimento AS Nascimento, c.dt_cadastro AS Cadastro, c.cargo AS Cargo, "
                            + "c.nivel_acesso AS Nível FROM funcionario c LEFT JOIN endereco e ON e.id = c.endereco_id "
                            + "WHERE CONCAT(c.nome, ' ', c.documento) LIKE ? ORDER BY c.id DESC";
                    break;
                case "filme":
                    sql = "SELECT id AS Código, titulo AS Título, genero AS Gênero, nota_imb AS Nota, "
                            + "valor_dia AS `Diária (R$)`, observacoes AS Observações FROM filme "
                            + "WHERE CONCAT(titulo, ' ', COALESCE(genero, '')) LIKE ? ORDER BY id DESC";
                    break;
                default:
                    sql = "SELECT l.id AS Código, CONCAT(c.id, ' - ', c.nome) AS Cliente, "
                            + "GROUP_CONCAT(CONCAT(f.id, ' - ', f.titulo) ORDER BY f.id SEPARATOR ', ') AS Filmes, "
                            + "l.qtd_dias AS Dias, l.data_cadastro AS Cadastro, "
                            + "COALESCE(CAST(l.data_devolucao AS CHAR), 'Em aberto') AS Devolução "
                            + "FROM locacao l JOIN cliente c ON c.id = l.cliente_id "
                            + "JOIN locacao_filme lf ON lf.locacao_id = l.id JOIN filme f ON f.id = lf.filme_id "
                            + "WHERE c.nome LIKE ? OR EXISTS (SELECT 1 FROM locacao_filme pesquisa "
                            + "JOIN filme pf ON pf.id = pesquisa.filme_id WHERE pesquisa.locacao_id = l.id AND pf.titulo LIKE ?) "
                            + "GROUP BY l.id, c.id, c.nome, l.qtd_dias, l.data_cadastro, l.data_devolucao ORDER BY l.id DESC";
            }
        } catch (IllegalArgumentException erro) {
            erro(response, 400, erro.getMessage());
            return;
        }

        Database database = new Database();
        try (Connection con = database.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + busca + "%");
            if ("locacao".equals(tipo)) {
                ps.setString(2, "%" + busca + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();
                cabecalho(out, "Consulta de " + pagina.replace(".html", ""));
                if ("1".equals(request.getParameter("salvo"))) {
                    out.println("<p>Cadastro salvo com sucesso.</p>");
                }
                out.println("<p><a href='" + pagina + "'>Cadastrar</a></p>");
                out.println("<form action='ClassServletBase' method='get'>");
                out.println("<input type='hidden' name='tipo' value='" + tipo + "'>");
                out.println("<label for='q'>Pesquisar</label><br><input id='q' name='q' maxlength='200' value='" + html(busca) + "'>");
                out.println("<button>Pesquisar</button> <a href='ClassServletBase?tipo=" + tipo + "'>Ver todos</a></form>");
                ResultSetMetaData colunas = rs.getMetaData();
                out.println("<table border='1'><thead><tr>");
                for (int i = 1; i <= colunas.getColumnCount(); i++) {
                    out.println("<th scope='col'>" + html(colunas.getColumnLabel(i)) + "</th>");
                }
                out.println("</tr></thead><tbody>");
                boolean encontrou = false;
                while (rs.next()) {
                    encontrou = true;
                    out.println("<tr>");
                    for (int i = 1; i <= colunas.getColumnCount(); i++) {
                        out.println("<td>" + html(rs.getString(i)) + "</td>");
                    }
                    out.println("</tr>");
                }
                out.println("</tbody></table>");
                if (!encontrou) {
                    out.println("<p>Nenhum cadastro encontrado.</p>");
                }
                out.println("</main></body></html>");
            }
        } catch (SQLException erro) {
            falhaBanco(response, erro);
        }
    }

    private static String pagina(String tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("Escolha um cadastro no menu inicial.");
        }
        switch (tipo) {
            case "cliente": return "clientes.html";
            case "funcionario": return "funcionarios.html";
            case "endereco": return "enderecos.html";
            case "filme": return "filmes.html";
            case "locacao": return "locacoes.html";
            default: throw new IllegalArgumentException("Cadastro inválido.");
        }
    }

    private static void preencherPessoa(HttpServletRequest request, Pessoa pessoa) {
        pessoa.setNome(texto(request, "nome", 200, true));
        pessoa.setDocumento(texto(request, "documento", 30, true));
        LocalDate nascimento = data(request, "dt_nascimento");
        if (nascimento != null && nascimento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Nascimento não pode ser no futuro.");
        }
        pessoa.setDt_nascimento(nascimento);
        String codigo = texto(request, "endereco_id", 10, false);
        if (!codigo.isEmpty()) {
            Endereco endereco = new Endereco();
            endereco.setId(inteiro(codigo, "Código do endereço", 1));
            pessoa.setEndereco(endereco);
        }
    }

    private static String texto(HttpServletRequest request, String campo, int maximo, boolean obrigatorio) {
        String valor = request.getParameter(campo);
        valor = valor == null ? "" : valor.trim();
        if ((obrigatorio && valor.isEmpty()) || valor.length() > maximo) {
            throw new IllegalArgumentException("Preencha corretamente o campo " + campo + " (máximo " + maximo + " caracteres).");
        }
        return valor;
    }

    private static int inteiro(String valor, String campo, int minimo) {
        try {
            int numero = Integer.parseInt(valor);
            if (numero < minimo) {
                throw new NumberFormatException();
            }
            return numero;
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException(campo + " deve ser um número inteiro a partir de " + minimo + ".");
        }
    }

    private static BigDecimal decimal(HttpServletRequest request, String campo, String maximo, int casas) {
        String texto = texto(request, campo, 20, true);
        if (!texto.matches("[0-9]+(\\.[0-9]{1," + casas + "})?")) {
            throw new IllegalArgumentException("Preencha " + campo + " com até " + casas + " casas decimais.");
        }
        BigDecimal valor = new BigDecimal(texto);
        if (valor.compareTo(new BigDecimal(maximo)) > 0) {
            throw new IllegalArgumentException(campo + " deve estar entre 0 e " + maximo + ".");
        }
        return valor;
    }

    private static LocalDate data(HttpServletRequest request, String campo) {
        String valor = texto(request, campo, 10, false);
        try {
            LocalDate data = valor.isEmpty() ? null : LocalDate.parse(valor);
            if (data != null && data.getYear() < 1000) {
                throw new DateTimeParseException("Ano fora do intervalo do banco", valor, 0);
            }
            return data;
        } catch (DateTimeParseException erro) {
            throw new IllegalArgumentException("Data inválida no campo " + campo + ".");
        }
    }

    private static String html(String valor) {
        return valor == null ? "" : valor.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static void cabecalho(PrintWriter out, String titulo) {
        out.println("<!DOCTYPE html><html lang='pt-BR'><head><meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1'>"
                + "<title>" + html(titulo) + " - Locadora Rebobina</title></head><body>");
        out.println("<header><h1>Locadora Rebobina</h1><nav aria-label='Menu principal'>"
                + "<a href='index.html'>Início</a> | <a href='clientes.html'>Clientes</a> | "
                + "<a href='funcionarios.html'>Funcionários</a> | <a href='enderecos.html'>Endereços</a> | "
                + "<a href='filmes.html'>Filmes</a> | <a href='locacoes.html'>Locações</a></nav></header>");
        out.println("<main><h2>" + html(titulo) + "</h2>");
    }

    private static void erro(HttpServletResponse response, int status, String mensagem) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.resetBuffer();
        response.setStatus(status);
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        cabecalho(out, "Não foi possível concluir");
        out.println("<p>" + html(mensagem) + "</p><p>Volte ao formulário pelo menu e confira os dados.</p></main></body></html>");
    }

    private void falhaBanco(HttpServletResponse response, SQLException erro) throws IOException {
        getServletContext().log("Erro ao acessar o banco da locadora", erro);
        if (erro.getSQLState() != null && erro.getSQLState().startsWith("23")) {
            erro(response, 400, "Confira os códigos de endereço, cliente e filmes e se o documento já está cadastrado.");
        } else {
            erro(response, 500, "Não foi possível acessar o banco. Tente novamente mais tarde.");
        }
    }
}
