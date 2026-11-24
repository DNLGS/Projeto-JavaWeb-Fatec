package servlets;

import dao.ClienteDao;
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
import models.Cliente;

@WebServlet({"/clientes", "/clientes.html"})
public class ClienteServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/clientes.html".equals(request.getServletPath())) {
            response.sendRedirect(request.getContextPath() + "/clientes");
            return;
        }
        try {
            String acao = CrudRequest.texto(request, "acao", 30, false);
            switch (acao) {
                case "" -> listar(request, response);
                case "novo" -> {
                    request.setAttribute("cliente", new Cliente());
                    CrudRequest.token(request);
                    CrudRequest.view(request, response, "clientes/form.jsp");
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
            request.setAttribute("clientes", new ClienteDao(con).search(busca));
        }
        request.setAttribute("busca", busca);
        CrudRequest.view(request, response, "clientes/lista.jsp");
    }

    private void carregar(HttpServletRequest request, HttpServletResponse response, String acao)
            throws SQLException, ServletException, IOException {
        int id = CrudRequest.inteiro(request.getParameter("id"), "Código", 1);
        Cliente cliente;
        try (Connection con = new Database().conectar()) {
            cliente = new ClienteDao(con).findById(id);
        }
        if (cliente == null) {
            CrudRequest.erro(request, response, 404, "Cliente não encontrado.");
            return;
        }
        CrudRequest.token(request);
        if ("editar".equals(acao)) {
            request.setAttribute("cliente", cliente);
            CrudRequest.view(request, response, "clientes/form.jsp");
        } else {
            request.setAttribute("id", cliente.getId());
            request.setAttribute("registro", cliente.getNome());
            request.setAttribute("rota", "/clientes");
            CrudRequest.view(request, response, "excluir.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/clientes.html".equals(request.getServletPath())) {
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
            Cliente cliente = "excluir".equals(acao) ? null : cliente(request, id);
            boolean encontrado;
            try (Connection con = new Database().conectar()) {
                ClienteDao dao = new ClienteDao(con);
                encontrado = switch (acao) {
                    case "inserir", "alterar" -> salvar(con, dao, cliente, "alterar".equals(acao));
                    default -> dao.delete(id) > 0;
                };
            }
            if (encontrado) {
                response.sendRedirect(request.getContextPath() + "/clientes?sucesso=1");
            } else {
                CrudRequest.erro(request, response, 404, "Cliente não encontrado.");
            }
        } catch (IllegalArgumentException erro) {
            CrudRequest.erro(request, response, 400, erro.getMessage());
        } catch (SQLException erro) {
            falhaBanco(request, response, erro);
        }
    }

    private Cliente cliente(HttpServletRequest request, int id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        CrudRequest.preencherPessoa(request, cliente);
        if (id == 0 || CrudRequest.informouEndereco(request)) {
            cliente.setEndereco(EnderecoServlet.endereco(request, 0));
        }
        return cliente;
    }

    private boolean salvar(Connection con, ClienteDao dao, Cliente cliente, boolean alterar) throws SQLException {
        con.setAutoCommit(false);
        try {
            if (alterar) {
                Cliente atual = dao.findForUpdate(cliente.getId());
                if (atual == null) {
                    con.rollback();
                    return false;
                }
                if (atual.getEndereco() != null) {
                    cliente.setEndereco(atual.getEndereco());
                }
            }
            if (cliente.getEndereco() != null && cliente.getEndereco().getId() == 0) {
                new EnderecoDao(con).insert(cliente.getEndereco());
            }
            if (alterar) {
                dao.update(cliente);
            } else {
                dao.insert(cliente);
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
            CrudRequest.erro(request, response, 409, "Já existe um cliente com esse documento.");
            return;
        }
        if ("23000".equals(erro.getSQLState()) && erro.getErrorCode() == 1451) {
            CrudRequest.erro(request, response, 409, "O cliente está vinculado a uma locação.");
            return;
        }
        getServletContext().log("Falha ao acessar clientes. Código SQL " + erro.getErrorCode());
        CrudRequest.erro(request, response, 500, "Não foi possível acessar o banco. Tente novamente mais tarde.");
    }
}
