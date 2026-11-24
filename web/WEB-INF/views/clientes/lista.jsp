<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="models.Cliente,models.Pessoa,models.Endereco,java.util.List" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Clientes - Locadora Rebobina</title>
</head>
<body>
<%@ include file="../menu.jspf" %>
<main>
  <h2>Clientes</h2>
  <% if ("1".equals(request.getParameter("sucesso"))) { %><p>Operação concluída com sucesso.</p><% } %>
  <p><a href="<%= ctx %>/clientes?acao=novo">Cadastrar cliente</a></p>
  <form action="<%= ctx %>/clientes" method="get">
    <label for="q">Nome ou documento</label><br>
    <input id="q" name="q" maxlength="200" value="<%= Html.escape(request.getAttribute("busca")) %>">
    <button type="submit">Pesquisar</button>
    <a href="<%= ctx %>/clientes">Ver todos</a>
  </form>
  <% List<?> clientes = (List<?>) request.getAttribute("clientes"); %>
  <% if (clientes.isEmpty()) { %>
    <p>Nenhum cadastro encontrado.</p>
  <% } else { %>
    <table border="1">
      <thead><tr><th scope="col">Código</th><th scope="col">Nome</th><th scope="col">Documento</th>
        <th scope="col">Endereço</th><th scope="col">Nascimento</th><th scope="col">Cadastro</th><th scope="col">Ações</th></tr></thead>
      <tbody>
      <% for (Object item : clientes) { Cliente cliente = (Cliente) item; Endereco endereco = cliente.getEndereco(); %>
        <tr><td><%= cliente.getId() %></td><td><%= Html.escape(cliente.getNome()) %></td>
          <td><%= Html.escape(cliente.getDocumento()) %></td>
          <td><% if (endereco == null) { %>Sem endereço<% } else { %>
            <%= endereco.getId() %> - <%= Html.escape(endereco.getLogradouro()) %>, <%= Html.escape(endereco.getNumero()) %>
          <% } %></td>
          <td><%= Html.escape(cliente.getDt_nascimento()) %></td><td><%= Html.escape(cliente.getDt_cadastro()) %></td>
          <td><a href="<%= ctx %>/clientes?acao=editar&amp;id=<%= cliente.getId() %>">Editar</a> |
            <a href="<%= ctx %>/clientes?acao=confirmar-exclusao&amp;id=<%= cliente.getId() %>">Excluir</a></td></tr>
      <% } %>
      </tbody>
    </table>
  <% } %>
</main>
</body>
</html>
