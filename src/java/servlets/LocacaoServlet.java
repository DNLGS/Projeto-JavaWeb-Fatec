package servlets;

import dao.LocacaoDao;
import database.Database;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import models.Cliente;
import models.Filme;
import models.Locacao;

@WebServlet({"/locacoes", "/locacoes.html"})
public class LocacaoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/locacoes.html".equals(request.getServletPath())) {
            response.sendRedirect(request.getContextPath() + "/locacoes");
            return;
        }
        try {
            String acao = CrudRequest.texto(request, "acao", 30, false);
            switch (acao) {
                case "" -> listar(request, response);
                case "novo" -> {
                    request.setAttribute("locacao", new Locacao());
                    CrudRequest.token(request);
                    CrudRequest.view(request, response, "locacoes/form.jsp");
                }
                case "editar", "confirmar-exclusao" -> carregar(request, response, acao);
                default -> CrudRequest.erro(request, response, 400, "Ação inválida.");
            }
        } catch (IllegalArgumentException erro) {
            CrudRequest.erro(request, response, 400, erro.getMessage());
        } catch (SQLException erro) {
            falhaBanco(request, response, erro);
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        String busca = CrudRequest.texto(request, "q", 200, false);
        try (Connection con = new Database().conectar()) {
            request.setAttribute("locacoes", new LocacaoDao(con).search(busca));
        }
        request.setAttribute("busca", busca);
        CrudRequest.view(request, response, "locacoes/lista.jsp");
    }

    private void carregar(HttpServletRequest request, HttpServletResponse response, String acao)
            throws SQLException, ServletException, IOException {
        int id = CrudRequest.inteiro(request.getParameter("id"), "Código", 1);
        Locacao locacao;
        try (Connection con = new Database().conectar()) {
            locacao = new LocacaoDao(con).findById(id);
        }
        if (locacao == null) {
            CrudRequest.erro(request, response, 404, "Locação não encontrada.");
            return;
        }
        CrudRequest.token(request);
        if ("editar".equals(acao)) {
            request.setAttribute("locacao", locacao);
            CrudRequest.view(request, response, "locacoes/form.jsp");
        } else {
            request.setAttribute("id", locacao.getId());
            request.setAttribute("registro", "Locação " + locacao.getId() + " — " + locacao.getCliente().getNome());
            request.setAttribute("rota", "/locacoes");
            CrudRequest.view(request, response, "excluir.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/locacoes.html".equals(request.getServletPath())) {
            response.setHeader("Allow", "GET, HEAD, OPTIONS");
            CrudRequest.erro(request, response, 405, "Use o formulário do cadastro.");
            return;
        }
        request.setCharacterEncoding("UTF-8");
        if (!CrudRequest.tokenValido(request)) {
            CrudRequest.erro(request, response, 403, "Formulário inválido ou expirado. Abra a página novamente.");
            return;
        }
        try {
            String acao = CrudRequest.texto(request, "acao", 30, true);
            if (!"inserir".equals(acao) && !"alterar".equals(acao) && !"excluir".equals(acao)) {
                throw new IllegalArgumentException("Ação inválida.");
            }
            int id = "inserir".equals(acao) ? 0 : CrudRequest.inteiro(request.getParameter("id"), "Código", 1);
            Locacao locacao = "excluir".equals(acao) ? null : locacao(request, id);
            boolean encontrado;
            try (Connection con = new Database().conectar()) {
                LocacaoDao dao = new LocacaoDao(con);
                encontrado = switch (acao) {
                    case "inserir" -> {
                        validarDevolucao(locacao, locacao.getData_cadastro());
                        dao.insert(locacao);
                        yield true;
                    }
                    case "alterar" -> {
                        Locacao atual = dao.findById(id);
                        if (atual == null) {
                            yield false;
                        }
                        validarDevolucao(locacao, atual.getData_cadastro());
                        yield dao.update(locacao) > 0 || dao.findById(id) != null;
                    }
                    default -> dao.delete(id) > 0;
                };
            }
            if (encontrado) {
                response.sendRedirect(request.getContextPath() + "/locacoes?sucesso=1");
            } else {
                CrudRequest.erro(request, response, 404, "Locação não encontrada.");
            }
        } catch (IllegalArgumentException erro) {
            CrudRequest.erro(request, response, 400, erro.getMessage());
        } catch (SQLException erro) {
            falhaBanco(request, response, erro);
        }
    }

    private Locacao locacao(HttpServletRequest request, int id) {
        Locacao locacao = new Locacao();
        locacao.setId(id);
        Cliente cliente = new Cliente();
        cliente.setId(CrudRequest.inteiro(request.getParameter("cliente_id"), "Código do cliente", 1));
        locacao.setCliente(cliente);
        locacao.setQtd_dias(CrudRequest.inteiro(request.getParameter("qtd_dias"), "Quantidade de dias", 1));
        locacao.setData_devolucao(CrudRequest.data(request, "data_devolucao"));
        Set<Integer> ids = new HashSet<>();
        for (String codigo : CrudRequest.texto(request, "filmes", 1000, true).split(",", -1)) {
            int filmeId = CrudRequest.inteiro(codigo.trim(), "Código do filme", 1);
            if (!ids.add(filmeId)) {
                throw new IllegalArgumentException("Informe cada filme apenas uma vez.");
            }
            Filme filme = new Filme();
            filme.setId(filmeId);
            locacao.addFilme(filme);
        }
        return locacao;
    }

    private void validarDevolucao(Locacao locacao, LocalDate cadastro) {
        LocalDate devolucao = locacao.getData_devolucao();
        if (devolucao != null && devolucao.isBefore(cadastro)) {
            throw new IllegalArgumentException("A devolução não pode ser anterior à locação.");
        }
    }

    private void falhaBanco(HttpServletRequest request, HttpServletResponse response, SQLException erro)
            throws ServletException, IOException {
        if ("23000".equals(erro.getSQLState()) && erro.getErrorCode() == 1452) {
            CrudRequest.erro(request, response, 409, "Confira se o cliente e todos os filmes ainda estão cadastrados.");
            return;
        }
        getServletContext().log("Falha ao acessar locacoes. Código SQL " + erro.getErrorCode());
        CrudRequest.erro(request, response, 500, "Não foi possível acessar o banco. Tente novamente mais tarde.");
    }
}
