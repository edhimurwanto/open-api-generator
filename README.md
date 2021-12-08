# CustomTemplate

Custom template for OpenAPI generator

### Build the project 

```
mvn clean package
```

### Generate java project using this template 

```
java -jar target/CustomTemplate-1.0-SNAPSHOT.jar generate -i openapi.json -o generated/test -g java-server
```
- `generate` : command used to generate the code
- `-i` : using openapi.json as an input that contain API specification
- `-o` : output folder `generated/test`
- `-g` : generated language `java-server` for this template, you can generate other language using this reference https://openapi-generator.tech/docs/generators
 
### Build generated project
```
cd generated/test && mvn clean package
```
### Run the jar
```
java -jar target/swagger-docsServer-1.0-SNAPSHOT-jar-with-dependencies.jar
```
#### Check on your browser on http://localhost:8080/notes
 
 
##### Happy coding :)
