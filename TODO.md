

1. good transaction wrapping architecture (?)
2. batch processing
3. MappersCollection - refine interface, make more precise specific methods
   3.1 make a method to search "default" mapper for jdbcType<->javaClass conversion ?
   3.2 completely remove SQLType and use "int sqlType" instead ?
   3.3 remove ability of injectors to inject null without setter ? 
4. query sources (load from external file, resources, etc; additional module?)
5. query factory from string
6. annotated repositories
7. logging
8. factory for everything for ease of extending/overwriting (?)

