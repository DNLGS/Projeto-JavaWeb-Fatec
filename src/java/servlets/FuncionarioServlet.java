package servlets;

import dao.FuncionarioDao;
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
import models.Funcionario;

@WebServlet({"/funcionarios", "/funcionarios.html"})
public class FuncionarioServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/funcionarios.html".equals(request.getServletPath())) {
            response.sendRedirect(request.getContextPath() + "/funcionarios");
            return;
        }
        try {
            String acao = CrudRequest.texto(request, "acao", 30, false);
            switch (acao) {
                case "" -> listar(request, response);
                case "novo" -> {
                    request.setAttribute("funcionario", new Funcionario());
                    CrudRequest.token(request);
                    CrudRequest.view(request, response, "funcionarios/form.jsp");
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
            request.setAttribute("funcionarios", new FuncionarioDao(con).search(busca));
        }
        request.setAttribute("busca", busca);
        CrudRequest.view(request, response, "funcionarios/lista.jsp");
    }

    private void carregar(HttpServletRequest request, HttpServletResponse response, String acao)
            throws SQLException, ServletException, IOException {
        int id = CrudRequest.inteiro(request.getParameter("id"), "Código", 1);
        Funcionario funcionario;
        try (Connection con = new Database().conectar()) {
            funcionario = new FuncionarioDao(con).findById(id);
        }
        if (funcionario == null) {
            CrudRequest.erro(request, response, 404, "Funcionário não encontrado.");
            return;
        }
        CrudRequest.token(request);
        if ("editar".equals(acao)) {
            request.setAttribute("funcionario", funcionario);
            CrudRequest.view(request, response, "funcionarios/form.jsp");
        } else {
            request.setAttribute("id", funcionario.getId());
            request.setAttribute("registro", funcionario.getNome());
            request.setAttribute("rota", "/funcionarios");
            CrudRequest.view(request, response, "excluir.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/funcionarios.html".equals(request.getServletPath())) {
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
            Funcionario funcionario = "excluir".equals(acao) ? null : funcionario(request, id);
            boolean encontrado;
            try (Connection con = new Database().conectar()) {
                FuncionarioDao dao = new FuncionarioDao(con);
                encontrado = switch (acao) {
                    case "inserir", "alterar" -> salvar(con, dao, funcionario, "alterar".equals(acao));
                    default -> dao.delete(id) > 0;
                };
            }
            if (encontrado) {
                response.sendRedirect(request.getContextPath() + "/funcionarios?sucesso=1");
            } else {
                CrudRequest.erro(request, response, 404, "Funcionário não encontrado.");
            }
        } catch (IllegalArgumentException erro) {
            CrudRequest.erro(request, response, 400, erro.getMessage());
        } catch (SQLException erro) {
            falhaBanco(request, response, erro);
        }
    }

    private Funcionario funcionario(HttpServletRequest request, int id) {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(id);
        CrudRequest.preencherPessoa(request, funcionario);
        funcionario.setCargo(CrudRequest.texto(request, "cargo", 100, false));
        funcionario.setNivel_acesso(CrudRequest.inteiro(request.getParameter("nivel_acesso"), "Nível de acesso", 0));
        if (id == 0 || CrudRequest.informouEndereco(request)) {
            funcionario.setEndereco(EnderecoServlet.endereco(request, 0));
        }
        return funcionario;
    }

    private boolean salvar(Connection con, FuncionarioDao dao, Funcionario funcionario, boolean alterar) throws SQLException {
        con.setAutoCommit(false);
        try {
            if (alterar) {
                Funcionario atual = dao.findForUpdate(funcionario.getId());
                if (atual == null) {
                    con.rollback();
                    return false;
                }
                if (atual.getEndereco() != null) {
                    funcionario.setEndereco(atual.getEndereco());
                }
            }
            if (funcionario.getEndereco() != null && funcionario.getEndereco().getId() == 0) {
                new EnderecoDao(con).insert(funcionario.getEndereco());
            }
            if (alterar) {
                dao.update(funcionario);
            } else {
                dao.insert(funcionario);
            }
            con.commit();
            return true;
        } catch (SQLException | RuntimeException erro) {
            try {
                con.rollback();
            } catch (SQLException rollback) {
                erro.addSuppressed(rollback);
            }
            throw erro;
        }
    }

    private void falhaBanco(HttpServletRequest request, HttpServletResponse response, SQLException erro)
            throws ServletException, IOException {
        if (erro.getErrorCode() == 1062) {
            CrudRequest.erro(request, response, 409, "Já existe um funcionário com esse documento.");
            return;
        }
        getServletContext().log("Falha ao acessar funcionarios. Código SQL " + erro.getErrorCode());
        CrudRequest.erro(request, response, 500, "Não foi possível acessar o banco. Tente novamente mais tarde.");
    }
}
