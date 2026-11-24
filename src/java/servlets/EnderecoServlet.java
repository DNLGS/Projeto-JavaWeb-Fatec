package servlets;

import dao.EnderecoDao;
import database.Database;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import models.Endereco;

@WebServlet({"/enderecos", "/enderecos.html"})
public class EnderecoServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/enderecos.html".equals(request.getServletPath())) {
            response.sendRedirect(request.getContextPath() + "/enderecos");
            return;
        }
        try {
            String acao = CrudRequest.texto(request, "acao", 30, false);
            switch (acao) {
                case "" -> listar(request, response);
                case "novo" -> {
                    request.setAttribute("endereco", new Endereco());
                    CrudRequest.token(request);
                    CrudRequest.view(request, response, "enderecos/form.jsp");
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
            request.setAttribute("enderecos", new EnderecoDao(con).search(busca));
        }
        request.setAttribute("busca", busca);
        CrudRequest.view(request, response, "enderecos/lista.jsp");
    }

    private void carregar(HttpServletRequest request, HttpServletResponse response, String acao)
            throws SQLException, ServletException, IOException {
        int id = CrudRequest.inteiro(request.getParameter("id"), "Código", 1);
        Endereco endereco;
        try (Connection con = new Database().conectar()) {
            endereco = new EnderecoDao(con).findById(id);
        }
        if (endereco == null) {
            CrudRequest.erro(request, response, 404, "Endereço não encontrado.");
            return;
        }
        CrudRequest.token(request);
        if ("editar".equals(acao)) {
            request.setAttribute("endereco", endereco);
            CrudRequest.view(request, response, "enderecos/form.jsp");
        } else {
            request.setAttribute("id", endereco.getId());
            request.setAttribute("registro", endereco.getLogradouro());
            request.setAttribute("rota", "/enderecos");
            CrudRequest.view(request, response, "excluir.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/enderecos.html".equals(request.getServletPath())) {
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
            Endereco endereco = "excluir".equals(acao) ? null : endereco(request, id);
            boolean encontrado;
            try (Connection con = new Database().conectar()) {
                EnderecoDao dao = new EnderecoDao(con);
                encontrado = switch (acao) {
                    case "inserir" -> {
                        dao.insert(endereco);
                        yield true;
                    }
                    case "alterar" -> dao.update(endereco) > 0 || dao.findById(id) != null;
                    default -> dao.delete(id) > 0;
                };
            }
            if (encontrado) {
                response.sendRedirect(request.getContextPath() + "/enderecos?sucesso=1");
            } else {
                CrudRequest.erro(request, response, 404, "Endereço não encontrado.");
            }
        } catch (IllegalArgumentException erro) {
            CrudRequest.erro(request, response, 400, erro.getMessage());
        } catch (SQLException erro) {
            falhaBanco(request, response, erro);
        }
    }

    static Endereco endereco(HttpServletRequest request, int id) {
        Endereco endereco = new Endereco();
        endereco.setId(id);
        endereco.setLogradouro(CrudRequest.texto(request, "logradouro", 255, true));
        endereco.setBairro(CrudRequest.texto(request, "bairro", 150, false));
        endereco.setNumero(CrudRequest.texto(request, "numero", 30, false));
        String ibge = CrudRequest.texto(request, "ibge", 7, true);
        String cep = CrudRequest.texto(request, "cep", 8, true);
        if (!ibge.matches("[0-9]{7}") || !cep.matches("[0-9]{8}")) {
            throw new IllegalArgumentException("Informe IBGE com 7 dígitos e CEP com 8 dígitos.");
        }
        endereco.setIbge(Integer.parseInt(ibge));
        endereco.setCep(Integer.parseInt(cep));
        return endereco;
    }

    private void falhaBanco(HttpServletRequest request, HttpServletResponse response, SQLException erro)
            throws ServletException, IOException {
        if ("23000".equals(erro.getSQLState()) && erro.getErrorCode() == 1451) {
            CrudRequest.erro(request, response, 409, "O endereço está vinculado a um cliente ou funcionário.");
            return;
        }
        getServletContext().log("Falha ao acessar enderecos. Código SQL " + erro.getErrorCode());
        CrudRequest.erro(request, response, 500, "Não foi possível acessar o banco. Tente novamente mais tarde.");
    }
}
