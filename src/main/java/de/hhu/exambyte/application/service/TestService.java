package de.hhu.exambyte.application.service;

import de.hhu.exambyte.domain.model.Test;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TestService {

    public List<Test> getAvailableTests() {
        Test test1 = new Test("Test1", 01);
        Test test2 = new Test("Test2", 02);
        Test test3 = new Test("Test3", 03);
        List<Test> tests = new ArrayList<>();
        tests.add(test1);
        tests.add(test2);
        tests.add(test3);
        return tests;
    }

/*    public Test createTest() {
    }*/
}