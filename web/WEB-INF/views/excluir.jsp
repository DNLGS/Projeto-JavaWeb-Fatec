<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Confirmar exclusão - Locadora Rebobina</title>
</head>
<body>
<%@ include file="menu.jspf" %>
<main>
  <% String rota = Html.escape(request.getAttribute("rota")); %>
  <h2>Confirmar exclusão</h2>
  <p>Excluir o registro <strong><%= Html.escape(request.getAttribute("registro")) %></strong>?</p>
  <form action="<%= ctx %><%= rota %>" method="post">
    <input type="hidden" name="acao" value="excluir">
    <input type="hidden" name="id" value="<%= Html.escape(request.getAttribute("id")) %>">
    <input type="hidden" name="csrf" value="<%= Html.escape(request.getAttribute("csrf")) %>">
    <button type="submit">Confirmar exclusão</button>
    <a href="<%= ctx %><%= rota %>">Cancelar</a>
  </form>
</main>
</body>
</html>
