package com.ufal.smartagro.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Validação automatizada de regras da Arquitetura Hexagonal com ArchUnit.
 *
 * Garante que:
 * 1. O núcleo de domínio (domain) seja completamente isolado de frameworks,
 *    anotações de persistência e camadas externas.
 * 2. A camada de aplicação (application) não dependa de controllers web, mappers
 *    web nem de entidades de persistência JPA.
 */
@AnalyzeClasses(packages = "com.ufal.smartagro", importOptions = {ImportOption.DoNotIncludeTests.class})
public class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domainMustNotDependOnOuterLayers =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "..application..",
                            "..adapters..",
                            "..config..",
                            "jakarta.persistence..",
                            "org.springframework.."
                    );

    @ArchTest
    static final ArchRule applicationMustNotDependOnControllersOrWebMappers =
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "..adapters.in.web.controller..",
                            "..adapters.in.web.mapper.."
                    );

    @ArchTest
    static final ArchRule applicationMustNotDependOnJpaEntities =
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..adapters.out.persistence.entity..");
}
