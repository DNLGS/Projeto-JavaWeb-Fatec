<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.List,models.Filme" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Filmes - Locadora Rebobina</title>
</head>
<body>
<%@ include file="../menu.jspf" %>
<main>
  <h2>Filmes</h2>
  <% if ("1".equals(request.getParameter("sucesso"))) { %><p>Operação concluída com sucesso.</p><% } %>
  <p><a href="<%= ctx %>/filmes?acao=novo">Cadastrar filme</a></p>
  <form action="<%= ctx %>/filmes" method="get">
    <label for="q">Título ou gênero</label><br>
    <input id="q" name="q" maxlength="200" value="<%= Html.escape(request.getAttribute("busca")) %>">
    <button type="submit">Pesquisar</button>
    <a href="<%= ctx %>/filmes">Ver todos</a>
  </form>
  <% List<?> filmes = (List<?>) request.getAttribute("filmes"); %>
  <% if (filmes.isEmpty()) { %>
    <p>Nenhum cadastro encontrado.</p>
  <% } else { %>
    <table border="1">
      <thead><tr><th scope="col">Código</th><th scope="col">Título</th><th scope="col">Gênero</th>
        <th scope="col">Nota</th><th scope="col">Diária (R$)</th><th scope="col">Observações</th><th scope="col">Ações</th></tr></thead>
      <tbody>
      <% for (Object item : filmes) { Filme filme = (Filme) item; %>
        <tr><td><%= filme.getId() %></td><td><%= Html.escape(filme.getTitulo()) %></td>
          <td><%= Html.escape(filme.getGenero()) %></td><td><%= filme.getNota_imb() %></td>
          <td><%= String.format(java.util.Locale.ROOT, "%.2f", filme.getValor_dia()) %></td>
          <td><%= Html.escape(filme.getObservacoes()) %></td>
          <td><a href="<%= ctx %>/filmes?acao=editar&amp;id=<%= filme.getId() %>">Editar</a> |
            <a href="<%= ctx %>/filmes?acao=confirmar-exclusao&amp;id=<%= filme.getId() %>">Excluir</a></td></tr>
      <% } %>
      </tbody>
    </table>
  <% } %>
</main>
</body>
</html>
