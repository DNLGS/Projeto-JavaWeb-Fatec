<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Não foi possível concluir - Locadora Rebobina</title>
</head>
<body>
<%@ include file="menu.jspf" %>
<main>
  <h2>Não foi possível concluir</h2>
  <p><%= Html.escape(request.getAttribute("mensagem")) %></p>
</main>
</body>
</html>
