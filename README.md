# bufferz
Projeto criado para "bufferizar" objetos antes de serem consumidos.

### Buffer de execução automática

> *Utilização*: Para armazenar objetos que serão processados em _bulk_ (inserção/atualização em batch no banco de dados, por exemplo).

```java
  Buffer<MyObject> buffer = AutomaticExecutionBufferBuilder.<MyObject>
                execute(Repository::save)
                .whenBufferSizeIs(2000)
                .handling(SQLException.class).with(()-> handleSQLException())
                .build();
```
Com o código acima, o buffer está pronto para receber objetos até o limite de 2000. Quando chegar no limite, o método `save` da classe `Repository` será executado passando como parâmetro os 2000 objetos do buffer.
```java
MyObject aObject = ...;
buffer.add(aObject)`
```
> Obs.: O método passado no `execute` deve obrigatoriamente receber uma Collection do tipo de dados que está sendo armazenado no buffer, e deve possuir um retorno (isso será melhorado em uma futura versão).

Qualquer exceção do tipo `SQLException` será tratada pelo método `handleSQLException()`. Caso ocorra exceção de outro tipo, e/ou no for declarado nenhum tratamento de exceção, a mesma será somente logada pelo log4j no nível `error`.
