package com.coding.hstest.service;

import com.coding.hstest.model.Person;
import com.coding.hstest.model.PersonDAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.UnaryOperator;

@Service
public class PersonService {

    private final PersonDAO personDAO;

    public PersonService(PersonDAO personDAO) {
        this.personDAO = personDAO;
    }

    public ResponseEntity<Object> getPerson(Integer personId) {
        Optional<Person> person = personDAO.findById(personId);
        return person.<ResponseEntity<Object>>map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>("Cannot find Person with id: " + personId, HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<Object> postPerson(Person person) {
        Optional<Person> savedPerson = personDAO.findById(person.getId());
        return savedPerson.<ResponseEntity<Object>>map(
                value -> new ResponseEntity<>(personDAO.saveAndFlush(person), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(personDAO.saveAndFlush(person), HttpStatus.CREATED));
    }

    public ResponseEntity<Object> deletePerson(Integer personId) {
        Optional<Person> person = personDAO.findById(personId);
        UnaryOperator<Person> personUnaryOperator = p -> {
            personDAO.delete(p);
            return p;
        };
        return person.<ResponseEntity<Object>>map(
                value -> new ResponseEntity<>("deleted person with Id: " + personId, HttpStatus.OK))
                .orElseGet(
                        () -> new ResponseEntity<>("Cannot find Person with id: " + personId, HttpStatus.NOT_FOUND));
    }
}
