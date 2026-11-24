<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="models.Filme" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Cadastro de filme - Locadora Rebobina</title>
</head>
<body>
<%@ include file="../menu.jspf" %>
<main>
  <% Filme filme = (Filme) request.getAttribute("filme");
     boolean edicao = filme.getId() > 0; %>
  <h2><%= edicao ? "Editar filme" : "Cadastrar filme" %></h2>
  <form action="<%= ctx %>/filmes" method="post" accept-charset="UTF-8">
    <input type="hidden" name="acao" value="<%= edicao ? "alterar" : "inserir" %>">
    <input type="hidden" name="csrf" value="<%= Html.escape(request.getAttribute("csrf")) %>">
    <% if (edicao) { %><input type="hidden" name="id" value="<%= filme.getId() %>"><% } %>
    <p><label for="titulo">Título *</label><br>
      <input id="titulo" name="titulo" maxlength="255" value="<%= Html.escape(filme.getTitulo()) %>" required></p>
    <p><label for="genero">Gênero</label><br>
      <input id="genero" name="genero" maxlength="100" value="<%= Html.escape(filme.getGenero()) %>"></p>
    <p><label for="nota_imb">Nota *</label><br>
      <input type="number" id="nota_imb" name="nota_imb" min="0" max="10" step="0.1" value="<%= filme.getNota_imb() %>" required></p>
    <p><label for="valor_dia">Valor da diária (R$) *</label><br>
      <input type="number" id="valor_dia" name="valor_dia" min="0" max="9999999999.99" step="0.01" value="<%= edicao ? String.format(java.util.Locale.ROOT, "%.2f", filme.getValor_dia()) : "" %>" required></p>
    <p><label for="observacoes">Observações</label><br>
      <textarea id="observacoes" name="observacoes" maxlength="2000" rows="3"><%= Html.escape(filme.getObservacoes()) %></textarea></p>
    <p><button type="submit">Salvar</button> <a href="<%= ctx %>/filmes">Cancelar</a></p>
  </form>
</main>
</body>
</html>
