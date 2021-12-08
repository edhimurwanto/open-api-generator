package id.edimurwanto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.swagger.models.Swagger;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import org.openapitools.codegen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomTemplateCodegen extends DefaultCodegen {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomTemplateCodegen.class);
    protected String apiVersion = "1.0.0";
    protected int serverPort = 8080;
    protected String projectName = "swagger-server";

    public CustomTemplateCodegen() {
        super();

        // set the output folder here
        outputFolder = "generated-code/java-server";

        /*
         * Models.  You can write model files using the modelTemplateFiles map.
         * if you want to create one template for file, you can do so here.
         * for multiple files for model, just put another entry in the `modelTemplateFiles` with
         * a different extension
         */
        modelTemplateFiles.put("pojo.mustache", ".java");

        /*
         * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
         * as with models, add multiple entries with different extensions for multiple files per
         * class
         */
        apiTemplateFiles.put("api.mustache", ".java");
        apiTestTemplateFiles.put("api_test.mustache", ".java");
        apiDocTemplateFiles.put("api_doc.mustache", ".md");

        /*
         * Template Location.  This is the location which templates will be read from.  The generator
         * will use the resource stream to attempt to read the templates.
         */
        embeddedTemplateDir = templateDir = "java-server";

        /*
         * Reserved words.  Override this with reserved words specific to your language
         */
        setReservedWordsLowerCase(
                Arrays.asList(
                        "break", "case", "class", "catch", "const", "continue",
                        "default", "delete", "do", "else", "extends", "finally",
                        "for", "function", "if", "import", "in", "instanceof", "let", "new",
                        "return", "super", "switch", "this", "throw", "try",
                        "void", "while", "private", "public", "protected"
                )
        );

        /*
         * Additional Properties.  These values can be passed to the templates and
         * are available in models, apis, and supporting files
         */
        additionalProperties.put("apiVersion", apiVersion);
        additionalProperties.put("serverPort", serverPort);
        additionalProperties.put("apiDocPath", "apidoc/");
    }

    @Override
    public String apiPackage() {
        return "src/main/java/org.simpleserver.controllers";
    }

    @Override
    public String modelPackage() {
        return "src/main/java/org.simpleserver.entities";
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    /**
     * Configures a friendly name for the generator.  This will be used by the generator
     * to select the library with the -l flag.
     *
     * @return the friendly name for the generator
     */
    @Override
    public String getName() {
        return "java-server";
    }

    /**
     * Returns human-friendly help for the generator.  Provide the consumer with help
     * tips, parameters here
     *
     * @return A string value for the help message
     */
    @Override
    public String getHelp() {
        return "Generates a simple java server library using the swagger-tools project.";
    }

    @Override
    public String toApiName(String name) {
        return super.toApiName(name);
    }

    @Override
    public String toApiFilename(String name) {
        return toApiName(name);
    }

    /**
     * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
     * those terms here.  This logic is only called if a variable matches the reserved words
     *
     * @return the escaped term
     */
    @Override
    public String escapeReservedWord(String name) {
        if(this.reservedWordsMappings().containsKey(name)) {
            return this.reservedWordsMappings().get(name);
        }
        return "_" + name;
    }

    /**
     * Location to write api files.  You can use the apiPackage() as defined when the class is
     * instantiated
     */
    @Override
    public String apiFileFolder() {
        return outputFolder + File.separator + apiPackage().replace('.', File.separatorChar);
    }

    @Override
    public String apiFilename(String templateName, String tag) {
        String suffix = apiTemplateFiles().get(templateName);
        return apiFileFolder() + '/' + toApiFilename(tag) + suffix;
    }

    @Override
    public String apiTestFileFolder() {
        return (outputFolder + "/src/test/java/org/simpleserver/controllers");
    }

    @Override
    public String apiDocFileFolder() {
        return (outputFolder + "/apidoc");
    }

    @Override
    public void processOpts() {
        super.processOpts();

     }

    @Override
    public List<SupportingFile> supportingFiles() {
        supportingFiles.add(new SupportingFile("org.simpleserver/Endpoint.mustache", "src/main/java/org/simpleserver", "Endpoint.java"));
        supportingFiles.add(new SupportingFile("org.simpleserver/RequestHandler.mustache", "src/main/java/org/simpleserver", "RequestHandler.java"));
        supportingFiles.add(new SupportingFile("org.simpleserver/SimpleServer.mustache", "src/main/java/org/simpleserver", "SimpleServer.java"));
        supportingFiles.add(new SupportingFile(".gitignore", "", ".gitignore"));
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("pom.mustache", "", "pom.xml"));
        supportingFiles.add(new SupportingFile("swagger.mustache", "apidoc", "swagger.yaml"));
        return supportingFiles;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> getOperations(Map<String, Object> objs) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Map<String, Object> apiInfo = (Map<String, Object>) objs.get("apiInfo");
        List<Map<String, Object>> apis = (List<Map<String, Object>>) apiInfo.get("apis");
        for (Map<String, Object> api : apis) {
            result.add((Map<String, Object>) api.get("operations"));
        }
        return result;
    }

    private static List<Map<String, Object>> sortOperationsByPath(List<CodegenOperation> ops) {
        Multimap<String, CodegenOperation> opsByPath = ArrayListMultimap.create();

        for (CodegenOperation op : ops) {
            opsByPath.put(op.path, op);
        }

        List<Map<String, Object>> opsByPathList = new ArrayList<Map<String, Object>>();
        for (Entry<String, Collection<CodegenOperation>> entry : opsByPath.asMap().entrySet()) {
            Map<String, Object> opsByPathEntry = new HashMap<String, Object>();
            opsByPathList.add(opsByPathEntry);
            opsByPathEntry.put("path", entry.getKey());
            CodegenOperation op = entry.getValue().iterator().next();
            opsByPathEntry.put("className", op.tags.get(0).getName());
        }

        return opsByPathList;
    }

    @Override
    public void preprocessOpenAPI(OpenAPI swagger) {
        String host = swagger.getOpenapi();
        String port = "8080";
        if (host != null) {
            String[] parts = host.split(":");
            if (parts.length > 1) {
                port = parts[1];
            }
        }
        this.additionalProperties.put("serverPort", port);

        if (swagger.getInfo() != null) {
            Info info = swagger.getInfo();
            if (info.getTitle() != null) {
                // when info.title is defined, use it for projectName
                // used in package.json
                projectName = info.getTitle()
                        .replaceAll("[^a-zA-Z0-9]", "-")
                        .replaceAll("^[-]*", "")
                        .replaceAll("[-]*$", "")
                        .replaceAll("[-]{2,}", "-")
                        .toLowerCase();
                this.additionalProperties.put("projectName", projectName);
            }
        }

        // need vendor extensions for x-swagger-router-controller
        Paths paths = swagger.getPaths();
        if(paths != null) {
            for(String pathname : paths.keySet()) {
                PathItem path = paths.get(pathname);
                Map<PathItem.HttpMethod, Operation> operationMap = path.readOperationsMap();
                if(operationMap != null) {
                    for(PathItem.HttpMethod method : operationMap.keySet()) {
                        Operation operation = operationMap.get(method);
                        if(operation.getTags() != null) operation.getTags().clear();
                        String tags;
                        if(pathname.endsWith("}")) {
                            tags = pathname.replaceAll("/\\{([a-zA-Z]+)\\}", "") + "Item";
                        } else {
                            tags = pathname.replaceAll("/\\{([a-zA-Z]+)\\}", "") + "Collection";
                        }

                        operation.setOperationId("handle" + method.toString().toUpperCase());

                        StringBuilder sb = new StringBuilder();
                        for (String tag : tags.split("/")) {
                            if(!tag.trim().isEmpty()) {
                                sb.append(tag.substring(0, 1).toUpperCase() + tag.substring(1));
                            }
                        }
                        operation.addTagsItem(sb.toString());
                    }
                }
            }
        }
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        Swagger swagger = (Swagger)objs.get("swagger");
        if(swagger != null) {
            try {
                SimpleModule module = new SimpleModule();
                module.addSerializer(Double.class, new JsonSerializer<Double>() {
                    @Override
                    public void serialize(Double val, JsonGenerator jgen,
                                          SerializerProvider provider) throws IOException, JsonProcessingException {
                        jgen.writeNumber(new BigDecimal(val));
                    }
                });
                objs.put("swagger-yaml", Yaml.mapper().registerModule(module).writeValueAsString(swagger));
            } catch (JsonProcessingException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        List<Map<String, Object>> opsByPathList = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> operations : getOperations(objs)) {
            @SuppressWarnings("unchecked")
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");

            opsByPathList.addAll(sortOperationsByPath(ops));
        }

        Map<String, Object> apiInfo = (Map<String, Object>) objs.get("apiInfo");
        apiInfo.put("operationsByPath", opsByPathList);

        return super.postProcessSupportingFileData(objs);
    }


    @Override
    public String escapeUnsafeCharacters(String input) {
        return input.replace("*/", "*_/").replace("/*", "/_*");
    }

    @Override
    public String escapeQuotationMark(String input) {
        // remove " to avoid code injection
        return input.replace("\"", "");
    }
}
