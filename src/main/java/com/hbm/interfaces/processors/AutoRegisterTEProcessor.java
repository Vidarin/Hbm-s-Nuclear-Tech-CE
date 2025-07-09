package com.hbm.interfaces.processors;


import com.hbm.interfaces.AutoRegisterTE;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;

@SupportedAnnotationTypes("com.hbm.interfaces.AutoRegisterTE")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AutoRegisterTEProcessor extends AbstractProcessor {

    private static final String GENERATED_CLASS = "com.hbm.main.TileEntityRegistrar";
    private Set<Registration> registrations = new LinkedHashSet<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            generateRegistrarClass();
            return true;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(AutoRegisterTE.class)) {
            if (element.getKind() != ElementKind.CLASS) continue;

            TypeElement classElement = (TypeElement) element;
            AutoRegisterTE annotation = classElement.getAnnotation(AutoRegisterTE.class);

            String className = classElement.getQualifiedName().toString();
            String value = annotation.value();

            registrations.add(new Registration(className, value));
        }
        return true;
    }

    private void generateRegistrarClass() {
        try {
            JavaFileObject jfo = processingEnv.getFiler().createSourceFile(GENERATED_CLASS);
            try (Writer writer = jfo.openWriter()) {
                writer.write("package com.example.generated;\n\n");
                writer.write("import net.minecraft.tileentity.TileEntity;\n");
                writer.write("import net.minecraft.util.ResourceLocation;\n");
                writer.write("import net.minecraftforge.fml.common.registry.GameRegistry;\n");
                writer.write("import com.example.RefStrings;\n\n");

                writer.write("public class TileEntityRegistrar {\n");
                writer.write("    public static void registerAll() {\n");

                for (Registration reg : registrations) {
                    writer.write("        GameRegistry.registerTileEntity(" + reg.className + ".class, " +
                            "new ResourceLocation(RefStrings.MODID, \"" + reg.value + "\"));\n");
                }

                writer.write("    }\n");
                writer.write("}\n");
            }
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate registrar: " + e.getMessage());
        }
    }

    private static class Registration {
        final String className;
        final String value;

        Registration(String className, String value) {
            this.className = className;
            this.value = value;
        }
    }
}
