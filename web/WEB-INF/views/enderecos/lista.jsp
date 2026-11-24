<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="models.Endereco,java.util.List,java.util.Locale" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Endereços - Locadora Rebobina</title>
</head>
<body>
<%@ include file="../menu.jspf" %>
<main>
  <h2>Endereços</h2>
  <% if ("1".equals(request.getParameter("sucesso"))) { %><p>Operação concluída com sucesso.</p><% } %>
  <p><a href="<%= ctx %>/enderecos?acao=novo">Cadastrar endereço</a></p>
  <form action="<%= ctx %>/enderecos" method="get">
    <label for="q">Código, logradouro, bairro ou CEP</label><br>
    <input id="q" name="q" maxlength="200" value="<%= Html.escape(request.getAttribute("busca")) %>">
    <button type="submit">Pesquisar</button>
    <a href="<%= ctx %>/enderecos">Ver todos</a>
  </form>
  <% List<?> enderecos = (List<?>) request.getAttribute("enderecos"); %>
  <% if (enderecos.isEmpty()) { %>
    <p>Nenhum cadastro encontrado.</p>
  <% } else { %>
    <table border="1">
      <thead><tr><th scope="col">Código</th><th scope="col">Logradouro</th><th scope="col">Bairro</th>
        <th scope="col">Número</th><th scope="col">IBGE</th><th scope="col">CEP</th><th scope="col">Ações</th></tr></thead>
      <tbody>
      <% for (Object item : enderecos) { Endereco endereco = (Endereco) item; %>
        <tr><td><%= endereco.getId() %></td><td><%= Html.escape(endereco.getLogradouro()) %></td>
          <td><%= Html.escape(endereco.getBairro()) %></td><td><%= Html.escape(endereco.getNumero()) %></td>
          <td><%= String.format(Locale.ROOT, "%07d", endereco.getIbge()) %></td>
          <td><%= String.format(Locale.ROOT, "%08d", endereco.getCep()) %></td>
          <td><a href="<%= ctx %>/enderecos?acao=editar&amp;id=<%= endereco.getId() %>">Editar</a> |
            <a href="<%= ctx %>/enderecos?acao=confirmar-exclusao&amp;id=<%= endereco.getId() %>">Excluir</a></td></tr>
      <% } %>
      </tbody>
    </table>
  <% } %>
</main>
</body>
</html>
