<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="models.Locacao,models.Cliente,models.Filme,java.util.List" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Cadastro de locação - Locadora Rebobina</title>
</head>
<body>
<%@ include file="../menu.jspf" %>
<main>
  <% Locacao locacao = (Locacao) request.getAttribute("locacao");
     boolean edicao = locacao.getId() > 0;
     String filmes = locacao.getFilmes().stream().map(filme -> Integer.toString(filme.getId()))
             .collect(java.util.stream.Collectors.joining(", ")); %>
  <h2><%= edicao ? "Editar locação" : "Cadastrar locação" %></h2>
  <form action="<%= ctx %>/locacoes" method="post" accept-charset="UTF-8">
    <input type="hidden" name="acao" value="<%= edicao ? "alterar" : "inserir" %>">
    <input type="hidden" name="csrf" value="<%= Html.escape(request.getAttribute("csrf")) %>">
    <% if (edicao) { %><input type="hidden" name="id" value="<%= locacao.getId() %>"><% } %>
    <p><label for="cliente_id">Código do cliente *</label><br>
      <input type="number" id="cliente_id" name="cliente_id" min="1" max="2147483647" step="1" value="<%= edicao ? ((Cliente) locacao.getCliente()).getId() : "" %>" required></p>
    <p><a href="<%= ctx %>/clientes">Consultar códigos de clientes</a></p>
    <p><label for="filmes">Códigos dos filmes, separados por vírgula *</label><br>
      <input id="filmes" name="filmes" maxlength="1000" value="<%= Html.escape(filmes) %>" required></p>
    <p><a href="<%= ctx %>/filmes">Consultar códigos de filmes</a></p>
    <p><label for="qtd_dias">Quantidade de dias *</label><br>
      <input type="number" id="qtd_dias" name="qtd_dias" min="1" max="2147483647" step="1" value="<%= edicao ? locacao.getQtd_dias() : 1 %>" required></p>
    <p><label for="data_devolucao">Data da devolução realizada (opcional)</label><br>
      <input type="date" id="data_devolucao" name="data_devolucao" min="<%= Html.escape(locacao.getData_cadastro()) %>" max="9999-12-31" value="<%= Html.escape(locacao.getData_devolucao()) %>"></p>
    <p>Deixe a devolução em branco enquanto os filmes estiverem com o cliente.</p>
    <p><button type="submit">Salvar</button> <a href="<%= ctx %>/locacoes">Cancelar</a></p>
  </form>
</main>
</body>
</html>
