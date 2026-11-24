<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="models.Funcionario,models.Pessoa,models.Endereco,java.util.List" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Funcionários - Locadora Rebobina</title>
</head>
<body>
<%@ include file="../menu.jspf" %>
<main>
  <h2>Funcionários</h2>
  <% if ("1".equals(request.getParameter("sucesso"))) { %><p>Operação concluída com sucesso.</p><% } %>
  <p><a href="<%= ctx %>/funcionarios?acao=novo">Cadastrar funcionário</a></p>
  <form action="<%= ctx %>/funcionarios" method="get">
    <label for="q">Nome ou documento</label><br>
    <input id="q" name="q" maxlength="200" value="<%= Html.escape(request.getAttribute("busca")) %>">
    <button type="submit">Pesquisar</button>
    <a href="<%= ctx %>/funcionarios">Ver todos</a>
  </form>
  <% List<?> funcionarios = (List<?>) request.getAttribute("funcionarios"); %>
  <% if (funcionarios.isEmpty()) { %>
    <p>Nenhum cadastro encontrado.</p>
  <% } else { %>
    <table border="1">
      <thead><tr><th scope="col">Código</th><th scope="col">Nome</th><th scope="col">Documento</th>
        <th scope="col">Endereço</th><th scope="col">Nascimento</th><th scope="col">Cadastro</th><th scope="col">Cargo</th><th scope="col">Nível</th><th scope="col">Ações</th></tr></thead>
      <tbody>
      <% for (Object item : funcionarios) { Funcionario funcionario = (Funcionario) item; Endereco endereco = funcionario.getEndereco(); %>
        <tr><td><%= funcionario.getId() %></td><td><%= Html.escape(funcionario.getNome()) %></td>
          <td><%= Html.escape(funcionario.getDocumento()) %></td>
          <td><% if (endereco == null) { %>Sem endereço<% } else { %>
            <%= endereco.getId() %> - <%= Html.escape(endereco.getLogradouro()) %>, <%= Html.escape(endereco.getNumero()) %>
          <% } %></td>
          <td><%= Html.escape(funcionario.getDt_nascimento()) %></td><td><%= Html.escape(funcionario.getDt_cadastro()) %></td>
          <td><%= Html.escape(funcionario.getCargo()) %></td><td><%= funcionario.getNivel_acesso() %></td>
          <td><a href="<%= ctx %>/funcionarios?acao=editar&amp;id=<%= funcionario.getId() %>">Editar</a> |
            <a href="<%= ctx %>/funcionarios?acao=confirmar-exclusao&amp;id=<%= funcionario.getId() %>">Excluir</a></td></tr>
      <% } %>
      </tbody>
    </table>
  <% } %>
</main>
</body>
</html>
