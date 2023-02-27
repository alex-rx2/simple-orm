# Pet project simple-orm

## Goals
* mapping of objects from resultsets, filling objects into insert statements, setting parameters to other queries
* avoiding the crap of query generation, etc.
* but still providing a higher level of abstraction over JDBC (simple wrap of it)

## How to achieve (TODO list)
* multi-project project
* wrap over JDBC:
  * simple abstractions (eg. driver, query, map params/result, execute query)
  * call chaining
  * immutable objects
  * connections pooling (pluggable? wrap over datasource instead of driver?)
  * usage can be started from wrapping anything from driver to single statement
  * more?
* instrument to map parameters in queries / result sets into objects:
  * can be used independently
  * different mapping strategies (eg. reflection, annotations, in-code mapping creation)
  * different implementations (generic configurable mapper(s), code generation, pre-compile sources generation)
  * more?
* additional goodies:
  * query providers (eq. load from files, declare in code)
  * repository abstraction?
  * more?
