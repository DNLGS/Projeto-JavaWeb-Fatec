package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import models.Pessoa;

final class CrudRequest {
    private CrudRequest() {}

    static void preencherPessoa(HttpServletRequest request, Pessoa pessoa) {
        pessoa.setNome(texto(request, "nome", 200, true));
        pessoa.setDocumento(texto(request, "documento", 30, true));
        LocalDate nascimento = data(request, "dt_nascimento");
        if (nascimento != null && nascimento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Nascimento não pode ser no futuro.");
        }
        pessoa.setDt_nascimento(nascimento);
    }

    static boolean informouEndereco(HttpServletRequest request) {
        for (String campo : new String[]{"logradouro", "bairro", "numero", "ibge", "cep"}) {
            String valor = request.getParameter(campo);
            if (valor != null && !valor.isBlank()) {
                return true;
            }
        }
        return false;
    }

    static LocalDate data(HttpServletRequest request, String campo) {
        String valor = texto(request, campo, 10, false);
        try {
            LocalDate data = valor.isEmpty() ? null : LocalDate.parse(valor);
            if (data != null && data.getYear() < 1000) {
                throw new DateTimeParseException("Ano fora do intervalo", valor, 0);
            }
            return data;
        } catch (DateTimeParseException erro) {
            throw new IllegalArgumentException("Data inválida no campo " + campo + ".");
        }
    }

    static String texto(HttpServletRequest request, String campo, int maximo, boolean obrigatorio) {
        String valor = request.getParameter(campo);
        valor = valor == null ? "" : valor.trim();
        if ((obrigatorio && valor.isEmpty()) || valor.length() > maximo) {
            throw new IllegalArgumentException("Confira o campo " + campo + " (máximo " + maximo + " caracteres).");
        }
        return valor;
    }

    static int inteiro(String valor, String campo, int minimo) {
        try {
            int numero = Integer.parseInt(valor);
            if (numero >= minimo) {
                return numero;
            }
        } catch (NumberFormatException ignored) {
            // The same message applies to missing, malformed and out-of-range input.
        }
        throw new IllegalArgumentException(campo + " deve ser um inteiro a partir de " + minimo + ".");
    }

    static BigDecimal decimal(HttpServletRequest request, String campo, String maximo, int casas) {
        String valor = texto(request, campo, 20, true);
        if (!valor.matches("[0-9]+(\\.[0-9]{1," + casas + "})?")) {
            throw new IllegalArgumentException("Preencha " + campo + " com até " + casas + " casas decimais.");
        }
        BigDecimal numero = new BigDecimal(valor);
        if (numero.compareTo(new BigDecimal(maximo)) > 0) {
            throw new IllegalArgumentException(campo + " deve estar entre 0 e " + maximo + ".");
        }
        return numero;
    }

    static void token(HttpServletRequest request) {
        HttpSession session = request.getSession();
        synchronized (session) {
            if (session.getAttribute("csrf") == null) {
                session.setAttribute("csrf", UUID.randomUUID().toString());
            }
        }
        request.setAttribute("csrf", session.getAttribute("csrf"));
    }

    static boolean tokenValido(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("csrf") instanceof String token
                && token.equals(request.getParameter("csrf"));
    }

    static void view(HttpServletRequest request, HttpServletResponse response, String arquivo)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/" + arquivo).forward(request, response);
    }

    static void erro(HttpServletRequest request, HttpServletResponse response, int status, String mensagem)
            throws ServletException, IOException {
        response.setStatus(status);
        request.setAttribute("mensagem", mensagem);
        view(request, response, "erro.jsp");
    }
}
