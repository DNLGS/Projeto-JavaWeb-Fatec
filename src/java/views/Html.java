package views;

public final class Html {
    private Html() {}

    public static String escape(Object valor) {
        return valor == null ? "" : valor.toString().replace("&", "&amp;")
                .replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
