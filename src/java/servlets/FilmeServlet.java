package servlets;

import dao.FilmeDao;
import database.Database;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import models.Filme;

@WebServlet({"/filmes", "/filmes.html"})
public class FilmeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/filmes.html".equals(request.getServletPath())) {
            response.sendRedirect(request.getContextPath() + "/filmes");
            return;
        }
        try {
            String acao = CrudRequest.texto(request, "acao", 30, false);
            switch (acao) {
                case "" -> listar(request, response);
                case "novo" -> {
                    request.setAttribute("filme", new Filme());
                    CrudRequest.token(request);
                    CrudRequest.view(request, response, "filmes/form.jsp");
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
            request.setAttribute("filmes", new FilmeDao(con).search(busca));
        }
        request.setAttribute("busca", busca);
        CrudRequest.view(request, response, "filmes/lista.jsp");
    }

    private void carregar(HttpServletRequest request, HttpServletResponse response, String acao)
            throws SQLException, ServletException, IOException {
        int id = CrudRequest.inteiro(request.getParameter("id"), "Código", 1);
        Filme filme;
        try (Connection con = new Database().conectar()) {
            filme = new FilmeDao(con).findById(id);
        }
        if (filme == null) {
            CrudRequest.erro(request, response, 404, "Filme não encontrado.");
            return;
        }
        CrudRequest.token(request);
        if ("editar".equals(acao)) {
            request.setAttribute("filme", filme);
            CrudRequest.view(request, response, "filmes/form.jsp");
        } else {
            request.setAttribute("id", filme.getId());
            request.setAttribute("registro", filme.getTitulo());
            request.setAttribute("rota", "/filmes");
            CrudRequest.view(request, response, "excluir.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/filmes.html".equals(request.getServletPath())) {
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
            Filme filme = "excluir".equals(acao) ? null : filme(request, id);
            boolean encontrado;
            try (Connection con = new Database().conectar()) {
                FilmeDao dao = new FilmeDao(con);
                encontrado = switch (acao) {
                    case "inserir" -> {
                        dao.insert(filme);
                        yield true;
                    }
                    case "alterar" -> dao.update(filme) > 0 || dao.findById(id) != null;
                    default -> dao.delete(id) > 0;
                };
            }
            if (encontrado) {
                response.sendRedirect(request.getContextPath() + "/filmes?sucesso=1");
            } else {
                CrudRequest.erro(request, response, 404, "Filme não encontrado.");
            }
        } catch (IllegalArgumentException erro) {
            CrudRequest.erro(request, response, 400, erro.getMessage());
        } catch (SQLException erro) {
            falhaBanco(request, response, erro);
        }
    }

    private Filme filme(HttpServletRequest request, int id) {
        Filme filme = new Filme();
        filme.setId(id);
        filme.setTitulo(CrudRequest.texto(request, "titulo", 255, true));
        filme.setGenero(CrudRequest.texto(request, "genero", 100, false));
        filme.setNota_imb(CrudRequest.decimal(request, "nota_imb", "10", 1).doubleValue());
        filme.setValor_dia(CrudRequest.decimal(request, "valor_dia", "9999999999.99", 2).doubleValue());
        filme.setObservacoes(CrudRequest.texto(request, "observacoes", 2000, false));
        return filme;
    }

    private void falhaBanco(HttpServletRequest request, HttpServletResponse response, SQLException erro)
            throws ServletException, IOException {
        if ("23000".equals(erro.getSQLState()) && erro.getErrorCode() == 1451) {
            CrudRequest.erro(request, response, 409, "O filme está vinculado a uma locação.");
            return;
        }
        getServletContext().log("Falha ao acessar filmes. Código SQL " + erro.getErrorCode());
        CrudRequest.erro(request, response, 500, "Não foi possível acessar o banco. Tente novamente mais tarde.");
    }
}
