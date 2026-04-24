package com.example.boost.architecture;

import static org.junit.jupiter.api.Assertions.fail;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

class ModularArchitectureTest {

    private final JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.example.boost");

    @Test
    void module_must_not_access_other_module_infrastructure() {
        Set<String> violations = new LinkedHashSet<>();

        for (JavaClass source : importedClasses) {
            String sourceModule = moduleName(source.getPackageName());
            if (!isFeatureModule(sourceModule)) {
                continue;
            }
            if (source.getPackageName().startsWith("com.example.boost." + sourceModule + ".infrastructure")) {
                continue;
            }

            for (Dependency dependency : source.getDirectDependenciesFromSelf()) {
                JavaClass target = dependency.getTargetClass();
                String targetModule = moduleName(target.getPackageName());
                if (!isFeatureModule(targetModule) || sourceModule.equals(targetModule)) {
                    continue;
                }

                if (target.getPackageName().startsWith("com.example.boost." + targetModule + ".infrastructure")) {
                    violations.add(source.getName() + " -> " + target.getName());
                }
            }
        }

        if (!violations.isEmpty()) {
            fail("Dilarang mengakses infrastructure modul lain:\n" + String.join("\n", violations));
        }
    }

    @Test
    void controller_must_only_access_own_application_layer() {
        Set<String> violations = new LinkedHashSet<>();

        for (JavaClass source : importedClasses) {
            String sourcePackage = source.getPackageName();
            if (!sourcePackage.matches("com\\.example\\.boost\\.[^.]+\\.api(\\..+)?")) {
                continue;
            }

            String sourceModule = moduleName(sourcePackage);
            for (Dependency dependency : source.getDirectDependenciesFromSelf()) {
                JavaClass target = dependency.getTargetClass();
                String targetPackage = target.getPackageName();

                if (!targetPackage.startsWith("com.example.boost.")) {
                    continue;
                }

                String targetModule = moduleName(targetPackage);
                if (sourceModule.equals(targetModule)) {
                    boolean allowed = targetPackage.startsWith("com.example.boost." + sourceModule + ".application")
                            || targetPackage.startsWith("com.example.boost." + sourceModule + ".api");
                    if (!allowed) {
                        violations.add(source.getName() + " -> " + target.getName());
                    }
                    continue;
                }

                boolean shared = targetPackage.startsWith("com.example.boost.domain")
                        || targetPackage.startsWith("com.example.boost.exception")
                        || targetPackage.startsWith("com.example.boost.context")
                        || targetPackage.startsWith("com.example.boost.security");
                if (!shared) {
                    violations.add(source.getName() + " -> " + target.getName());
                }
            }
        }

        if (!violations.isEmpty()) {
            fail("Controller hanya boleh bergantung ke application modul sendiri:\n" + String.join("\n", violations));
        }
    }

    @Test
    void domain_must_not_depend_on_web_or_controller_layer() {
        Set<String> violations = new LinkedHashSet<>();

        for (JavaClass source : importedClasses) {
            String sourcePackage = source.getPackageName();
            if (!sourcePackage.startsWith("com.example.boost.domain.")) {
                continue;
            }

            for (Dependency dependency : source.getDirectDependenciesFromSelf()) {
                JavaClass target = dependency.getTargetClass();
                String targetPackage = target.getPackageName();
                boolean dependsOnWeb = targetPackage.contains(".api.") || targetPackage.contains(".controller.");
                if (dependsOnWeb) {
                    violations.add(source.getName() + " -> " + target.getName());
                }
            }
        }

        if (!violations.isEmpty()) {
            fail("Domain/entity tidak boleh bergantung ke web/controller layer:\n" + String.join("\n", violations));
        }
    }

    private static boolean isFeatureModule(String module) {
        return "iam".equals(module)
                || "billing".equals(module)
                || "catalog".equals(module)
                || "scheduling".equals(module)
                || "notification".equals(module);
    }

    private static String moduleName(String packageName) {
        String prefix = "com.example.boost.";
        if (!packageName.startsWith(prefix)) {
            return "";
        }

        String remaining = packageName.substring(prefix.length());
        int dotIndex = remaining.indexOf('.');
        return dotIndex < 0 ? remaining : remaining.substring(0, dotIndex);
    }
}
