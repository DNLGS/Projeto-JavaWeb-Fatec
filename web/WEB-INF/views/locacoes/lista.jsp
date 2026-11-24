<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="models.Locacao,models.Cliente,models.Filme,java.util.List" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Locações - Locadora Rebobina</title>
</head>
<body>
<%@ include file="../menu.jspf" %>
<main>
  <h2>Locações</h2>
  <% if ("1".equals(request.getParameter("sucesso"))) { %><p>Operação concluída com sucesso.</p><% } %>
  <p><a href="<%= ctx %>/locacoes?acao=novo">Cadastrar locação</a></p>
  <form action="<%= ctx %>/locacoes" method="get">
    <label for="q">Nome do cliente ou título do filme</label><br>
    <input id="q" name="q" maxlength="200" value="<%= Html.escape(request.getAttribute("busca")) %>">
    <button type="submit">Pesquisar</button>
    <a href="<%= ctx %>/locacoes">Ver todos</a>
  </form>
  <% List<?> locacoes = (List<?>) request.getAttribute("locacoes"); %>
  <% if (locacoes.isEmpty()) { %>
    <p>Nenhum cadastro encontrado.</p>
  <% } else { %>
    <table border="1">
      <thead><tr><th scope="col">Código</th><th scope="col">Cliente</th><th scope="col">Filmes</th>
        <th scope="col">Dias</th><th scope="col">Cadastro</th><th scope="col">Devolução</th><th scope="col">Ações</th></tr></thead>
      <tbody>
      <% for (Object item : locacoes) {
           Locacao locacao = (Locacao) item;
           Cliente cliente = (Cliente) locacao.getCliente(); %>
        <tr><td><%= locacao.getId() %></td><td><%= cliente.getId() %> - <%= Html.escape(cliente.getNome()) %></td>
          <td><% for (int i = 0; i < locacao.getFilmes().size(); i++) {
                   Filme filme = locacao.getFilmes().get(i);
                   if (i > 0) { %>, <% } %><%= filme.getId() %> - <%= Html.escape(filme.getTitulo()) %><% } %></td>
          <td><%= locacao.getQtd_dias() %></td><td><%= Html.escape(locacao.getData_cadastro()) %></td>
          <td><%= locacao.getData_devolucao() == null ? "Em aberto" : Html.escape(locacao.getData_devolucao()) %></td>
          <td><a href="<%= ctx %>/locacoes?acao=editar&amp;id=<%= locacao.getId() %>">Editar</a> |
            <a href="<%= ctx %>/locacoes?acao=confirmar-exclusao&amp;id=<%= locacao.getId() %>">Excluir</a></td></tr>
      <% } %>
      </tbody>
    </table>
  <% } %>
</main>
</body>
</html>
