<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="models.Endereco,java.util.List,java.util.Locale" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Cadastro de endereço - Locadora Rebobina</title>
</head>
<body>
<%@ include file="../menu.jspf" %>
<main>
  <% Endereco endereco = (Endereco) request.getAttribute("endereco");
     boolean edicao = endereco.getId() > 0; %>
  <h2><%= edicao ? "Editar endereço" : "Cadastrar endereço" %></h2>
  <form action="<%= ctx %>/enderecos" method="post" accept-charset="UTF-8">
    <input type="hidden" name="acao" value="<%= edicao ? "alterar" : "inserir" %>">
    <input type="hidden" name="csrf" value="<%= Html.escape(request.getAttribute("csrf")) %>">
    <% if (edicao) { %><input type="hidden" name="id" value="<%= endereco.getId() %>"><% } %>
    <p><label for="logradouro">Logradouro *</label><br>
      <input id="logradouro" name="logradouro" maxlength="255" value="<%= Html.escape(endereco.getLogradouro()) %>" required></p>
    <p><label for="bairro">Bairro</label><br>
      <input id="bairro" name="bairro" maxlength="150" value="<%= Html.escape(endereco.getBairro()) %>"></p>
    <p><label for="numero">Número</label><br>
      <input id="numero" name="numero" maxlength="30" value="<%= Html.escape(endereco.getNumero()) %>"></p>
    <p><label for="ibge">IBGE *</label><br>
      <input id="ibge" name="ibge" inputmode="numeric" pattern="[0-9]{7}" maxlength="7" value="<%= edicao ? String.format(Locale.ROOT, "%07d", endereco.getIbge()) : "" %>" required></p>
    <p><label for="cep">CEP *</label><br>
      <input id="cep" name="cep" inputmode="numeric" pattern="[0-9]{8}" maxlength="8" value="<%= edicao ? String.format(Locale.ROOT, "%08d", endereco.getCep()) : "" %>" required></p>
    <p><button type="submit">Salvar</button> <a href="<%= ctx %>/enderecos">Cancelar</a></p>
  </form>
</main>
</body>
</html>
