package org.example;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.element.ElementKind;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;


@SupportedAnnotationTypes("org.example.JsonSerializable")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class JsonAnnotationsProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(JsonSerializable.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@JsonSerializable можна ставити тільки на класи", element);
                continue;
            }

            generateMapper((TypeElement) element);
        }

        return true;
    }

    private void generateMapper(TypeElement classElement) {
        String qualifiedName = classElement.getQualifiedName().toString();
        String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
        String simpleClassName = classElement.getSimpleName().toString();
        String mapperClassName = simpleClassName + "Mapper";

        List<FieldInfo> fields = collectFields(classElement);

        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + mapperClassName);

            try (PrintWriter writer = new PrintWriter(file.openWriter())) {
                writeMapper(writer, packageName, simpleClassName, mapperClassName, fields);
            }

        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Помилка генерації " + mapperClassName + ": " + e.getMessage());
        }
    }

    private List<FieldInfo> collectFields(TypeElement classElement) {
        List<FieldInfo> fields = new ArrayList<>();

        for (Element enclosed : classElement.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.FIELD) continue;

            VariableElement field = (VariableElement) enclosed;
            String fieldName = field.getSimpleName().toString();

            JsonField jsonField = field.getAnnotation(JsonField.class);
            String jsonKey = (jsonField != null && !jsonField.name().isEmpty()) ? jsonField.name() : fieldName;

            boolean ignored = field.getAnnotation(JsonIgnore.class) != null;

            String fieldType = field.asType().toString();

            fields.add(new FieldInfo(fieldName, jsonKey, fieldType, ignored));
        }

        return fields;
    }

    private void writeMapper(PrintWriter w, String packageName, String sourceClass, String mapperClass, List<FieldInfo> fields) {
        w.println("package " + packageName + ";");
        w.println();

        w.println("import org.example.JsonIgnore;");
        w.println("import org.example.JsonRequired;");
        w.println();

        w.println("public class " + mapperClass + " {");
        w.println();

        for (FieldInfo field : fields) {
            if (field.ignored()) w.println("    @JsonIgnore");
            else w.println("    @JsonRequired");
            w.println("    private " + field.type() + " " + field.name() + ";");
        }

        w.println("    public String toJson(" + sourceClass + " obj) {");
        w.println("        if (obj == null)  return \"null\";");
        w.println("        org.example.JsonSerializer.validate(obj);");
        w.println("        StringBuilder sb = new StringBuilder();");
        w.println("        sb.append(\"{\");");

        List<FieldInfo> activeFields = fields.stream().filter(f -> !f.ignored()).toList();

        for (int i = 0; i < activeFields.size(); i++) {
            FieldInfo field = activeFields.get(i);
            boolean isLast = (i == activeFields.size() - 1);
            String comma = isLast ? "" : ",";

            String capitalizedName = field.name().substring(0, 1).toUpperCase() + field.name().substring(1);

            if (field.type().equals("java.lang.String")) {
                w.println("        sb.append(\"\\\"" + field.jsonKey() + "\\\": \\\"\")");
                w.println("          .append(obj.get" + capitalizedName + "())"); // Використовуємо capitalizedName
                w.println("          .append(\"\\\"" + comma + "\");");
            } else {
                w.println("        sb.append(\"\\\"" + field.jsonKey() + "\\\": \")");
                w.println("          .append(obj.get" + capitalizedName + "())"); // Використовуємо capitalizedName
                w.println("          .append(\"" + comma + "\");");
            }
            w.println();
        }

        w.println("        sb.append(\"}\");");
        w.println("        return sb.toString();");
        w.println("    }");
        w.println("}");
    }

    private record FieldInfo(String name, String jsonKey, String type, boolean ignored) {}
}