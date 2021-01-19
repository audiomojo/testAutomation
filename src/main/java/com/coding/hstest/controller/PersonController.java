package com.coding.hstest.controller;

import com.coding.hstest.model.Person;
import com.coding.hstest.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping(value = "/v1")
@Slf4j
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping(value = "/get-person/{personId}")
    @PreAuthorize("hasAnyRole('READ_ONLY', 'READ_WRITE')")
    @Operation(security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<Object> getPerson(@PathVariable("personId") Integer personId) {
        return personService.getPerson(personId);
    }

    @PostMapping(value = "/post-person")
    @PreAuthorize("hasAnyRole('READ_WRITE')")
    @Operation(security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<Object> postPerson(@Valid @RequestBody Person person) {
        return personService.postPerson(person);
    }

    @DeleteMapping(value = "/delete-person/{personId}")
    @Operation(security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<Object> deletePerson(@PathVariable("personId") Integer personId) {
        return personService.deletePerson(personId);
    }

    //Thrown on use of @Validated with javax.validation
    //400
    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Object handleValidationError(ConstraintViolationException exception) {
        log.error("Error ConstraintViolationException: " + exception.getMessage());
        return new ResponseEntity<>(exception.getConstraintViolations().stream()
                .map(violation -> {
                    String propertyName = ((PathImpl) violation.getPropertyPath())
                            .getLeafNode().getName();
                    return "JSON Error: " + propertyName + " " + violation.getMessage();
                }).collect(Collectors.toList()), HttpStatus.BAD_REQUEST);
    }

    //Thrown on use of @Validated with @Valid and javax.validation
    //400
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Object handleMethodArgumentNotValidExceptionError(MethodArgumentNotValidException exception) {
        log.error("Error MethodArgumentNotValidException: " + exception.getMessage());
        return new ResponseEntity<>(exception.getBindingResult().getAllErrors().stream()
                .map(objectError -> {
                    Object o = objectError.unwrap(Object.class);
                    if (o instanceof ConstraintViolationImpl) {
                        return (ConstraintViolationImpl) o;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .map(violation -> {
                    String propertyName = ((PathImpl) violation.getPropertyPath())
                            .getLeafNode().getName();
                    return "JSON Error: " + propertyName + " " + violation.getMessage();
                }).collect(Collectors.toList()), HttpStatus.BAD_REQUEST);
    }

}
