<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="models.Funcionario,models.Pessoa,models.Endereco,java.util.List" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Cadastro de funcionário - Locadora Rebobina</title>
</head>
<body>
<%@ include file="../menu.jspf" %>
<main>
  <% Funcionario funcionario = (Funcionario) request.getAttribute("funcionario");
     Pessoa pessoa = funcionario;
     boolean edicao = funcionario.getId() > 0; %>
  <h2><%= edicao ? "Editar funcionário" : "Cadastrar funcionário" %></h2>
  <form action="<%= ctx %>/funcionarios" method="post" accept-charset="UTF-8">
    <input type="hidden" name="acao" value="<%= edicao ? "alterar" : "inserir" %>">
    <input type="hidden" name="csrf" value="<%= Html.escape(request.getAttribute("csrf")) %>">
    <% if (edicao) { %><input type="hidden" name="id" value="<%= funcionario.getId() %>"><% } %>
    <%@ include file="../pessoa-campos.jspf" %>
    <p><label for="cargo">Cargo</label><br>
      <input id="cargo" name="cargo" maxlength="100" value="<%= Html.escape(funcionario.getCargo()) %>"></p>
    <p><label for="nivel_acesso">Nível de acesso *</label><br>
      <input type="number" id="nivel_acesso" name="nivel_acesso" min="0" max="2147483647" step="1" value="<%= funcionario.getNivel_acesso() %>" required></p>
    <p><button type="submit">Salvar</button> <a href="<%= ctx %>/funcionarios">Cancelar</a></p>
  </form>
</main>
</body>
</html>
