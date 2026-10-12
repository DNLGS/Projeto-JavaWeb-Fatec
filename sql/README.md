# Banco de dados no Windows

O projeto usa MySQL. O arquivo `banco.sql` também funciona no Windows.

1. Inicie o MySQL e conecte-se pelo MySQL Workbench com um usuário que possa criar bancos e tabelas.
2. Abra `sql/banco.sql` em **File > Open SQL Script** e execute o arquivo inteiro. Ele cria o banco `projeto-jean-2026` e as seis tabelas, sem apagar os dados existentes.
3. Copie `projeto-jean.properties.example` para a pasta do usuário que executa o Tomcat e renomeie a cópia para `projeto-jean.properties`. No PowerShell, a pasta é `$env:USERPROFILE`; por exemplo, `C:\Users\Professor\projeto-jean.properties`.
4. Preencha `user` e `password` com as credenciais de um usuário MySQL que tenha acesso ao banco `projeto-jean-2026`. O nome `locadora` do exemplo pode ser substituído por um usuário existente; o SQL não cria usuários.
5. Reinicie o Tomcat depois de configurar o arquivo.

O MySQL deve estar em `127.0.0.1`, na porta padrão `3306`. As credenciais devem ficar fora do repositório.

Para usar outro local para o arquivo, configure esta opção na JVM do **Tomcat**, substituindo o caminho:

```text
-Dlocadora.config=C:/config/projeto-jean.properties
```

Esse ajuste configura o banco. A versão do Java, o servidor selecionado no NetBeans e o mapeamento do servlet ainda precisam dos ajustes apontados na revisão.
