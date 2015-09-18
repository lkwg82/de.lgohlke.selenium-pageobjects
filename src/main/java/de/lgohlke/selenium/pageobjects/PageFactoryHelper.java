package de.lgohlke.selenium.pageobjects;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class PageFactoryHelper {
    private final WebDriver driver;

    private static List<Field> collectAllFieldsInHierarchy(Class clazz, List<Field> knownFields, String indent) {
        if (clazz == Object.class) {
            return ImmutableList.copyOf(knownFields);
        }
        log.debug("{}collect all fields in {}", indent, clazz.getSimpleName());

        List<Field> mergedList = new ArrayList<>(knownFields);
        for (Field f : clazz.getDeclaredFields()) {
            if (PageObject.class.isAssignableFrom(f.getType())
                    || (null != f.getAnnotation(ValidatePageObjectOnInit.class)) &&
                    null != f.getAnnotation(FindBy.class)) {
                mergedList.add(f);
            }
        }

        return collectAllFieldsInHierarchy(clazz.getSuperclass(), ImmutableList.copyOf(mergedList), indent + " ");
    }

    private static <T> void validatePageObject(T pageObject, List<Field> fields) {
        Class       clazz  = pageObject.getClass();
        List<Error> errors = new ArrayList<>();

        log.debug("validate PO {}@{}", pageObject.getClass().getSimpleName(), pageObject.hashCode());
        fields.stream()
              .filter(f -> null != f.getAnnotation(ValidatePageObjectOnInit.class))
              .filter(f -> null != f.getAnnotation(FindBy.class))
              .forEach(field -> {
                  try {
                      log.debug("validate webelement {} {} of PO {}@{}",
                                field.getType().getSimpleName(),
                                field.getName(),
                                pageObject.getClass().getSimpleName(),
                                pageObject.hashCode());
                      validateWebelement(pageObject, field);
                  } catch (NoSuchElementException e) {
                      errors.add(new Error(e, clazz, field, field.getAnnotation(FindBy.class)));
                  }
              });

        StringBuilder buffer = new StringBuilder();
        errors.forEach(e -> buffer.append("\nvalidation for " + e.getClazz() + "." + e.getField()
                                                                                      .getName() + " failed with findby: " + e
                .getFindBy()));

        if (buffer.length() > 0) {
            throw new NoSuchElementException(buffer.toString());
        }
    }

    private static <T> void validateWebelement(T pageObject, Field field) {
        field.setAccessible(true);
        try {
            Object o = field.get(pageObject);
            if (o instanceof WebElement) {
                ((WebElement) o).getLocation();
                log.debug("{}.{} is ok", pageObject.getClass().getSimpleName(), field.getName());
            } else {
                log.warn("this element [" + o + "] is not instance of " + WebElement.class);
            }
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static <T extends PageObject> void navigateToLocationIfPage(WebDriver driver, T pageObject) {
        if (pageObject instanceof Page) {
            String location = ((Page) pageObject).getLocation();
            log.debug("current location of {} is {}",pageObject,driver.getCurrentUrl());
            if (!location.isEmpty() && !driver.getCurrentUrl().equals(location)) {
                log.debug("try to get needed location: {}",location);
                driver.get(location);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends PageObject> void initPageObjectFields(T pageObject, List<Field> fields) {
        if (fields.isEmpty()) {
            return;
        }
        List<Field> sortedFields = fields.stream()
                                         .sorted((f1, f2) -> f1.getName()
                                                               .compareTo(f2.getName())).collect(toList());
        List<String> fieldList = sortedFields.stream()
                                             .map(f -> f.getType().getSimpleName() + " " + f.getName())
                                             .collect(toList());
        log.debug("initialize fields: \n - {}", Joiner.on("\n - ").join(fieldList));
        sortedFields
                .forEach(field -> {
                    if (PageObject.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        try {
                            PageObject nestedPO = initElements((Class<PageObject>) field.getType());
                            field.set(pageObject, nestedPO);
                        } catch (IllegalAccessException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                });
    }

    public <T extends PageObject> T initElements(Class<T> clazz) {
        return initElements(clazz, false);
    }

    public <T extends PageObject> T initElements(Class<T> clazz, boolean flat) {
        T pageObject = initDirectDeclaredElements(clazz);

        navigateToLocationIfPage(driver, pageObject);

        List<Field> fields = collectAllFieldsInHierarchy(clazz, new ArrayList<>(), "");

        if (!flat) {
            initPageObjectFields(pageObject, fields);
        }

        log.debug("call 'beforeInit' on {}@{}", pageObject.getClass().getSimpleName(), pageObject.hashCode());
        pageObject.beforeInit();
        validatePageObject(pageObject, fields);

        return pageObject;
    }

    private <T extends PageObject> T initDirectDeclaredElements(Class<T> clazz) {
        log.debug("### indirect initialize {}", clazz.getSimpleName());
        return PageFactory.initElements(driver, clazz);
    }

    @RequiredArgsConstructor
    @Getter
    private static class Error {
        private final NoSuchElementException exception;
        private final Class                  clazz;
        private final Field                  field;
        private final FindBy                 findBy;
    }
}
