<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="models.Cliente,models.Pessoa,models.Endereco,java.util.List" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Cadastro de cliente - Locadora Rebobina</title>
</head>
<body>
<%@ include file="../menu.jspf" %>
<main>
  <% Cliente cliente = (Cliente) request.getAttribute("cliente");
     Pessoa pessoa = cliente;
     boolean edicao = cliente.getId() > 0; %>
  <h2><%= edicao ? "Editar cliente" : "Cadastrar cliente" %></h2>
  <form action="<%= ctx %>/clientes" method="post" accept-charset="UTF-8">
    <input type="hidden" name="acao" value="<%= edicao ? "alterar" : "inserir" %>">
    <input type="hidden" name="csrf" value="<%= Html.escape(request.getAttribute("csrf")) %>">
    <% if (edicao) { %><input type="hidden" name="id" value="<%= cliente.getId() %>"><% } %>
    <%@ include file="../pessoa-campos.jspf" %>
    <p><button type="submit">Salvar</button> <a href="<%= ctx %>/clientes">Cancelar</a></p>
  </form>
</main>
</body>
</html>
