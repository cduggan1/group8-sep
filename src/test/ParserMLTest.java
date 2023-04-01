package test;

import main.ParserML;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ParserMLTest {

    @Test
    void predictString() throws IOException {
        ParserML ml = new ParserML(System.getProperty("user.dir")+"/src/main/ML");

        String testResult = ml.query("I like nature. I like fitness. I would like a place to eat food. An alarm is necessary for my safety. I need to wash clothes", false);
        String testResultClean = ml.query("I like nature. I like fitness. I would like a place to eat food. An alarm is necessary for my safety. I need to wash clothes", true);

        assertEquals("\"alarm\", \"Gym\", \"Cafeteria\", \"Alarm\", \"dryer\", \"washing-machine\"", testResult);
        assertEquals("\"alarm\", \"dryer\", \"washing-machine\"",testResultClean);
        System.out.println(testResultClean);

        if (testResult.equalsIgnoreCase("\"alarm\""))
            System.out.println("Parser working. ML not responding.");
    }

    @Test
    void predictNoML() {
        ParserML ml = new ParserML(System.getProperty("user.dir")+"/src/main/ML");
        assertEquals("[\"cable-television\", \"central-heating\", \"dishwasher\"]", ml.parseNoML("heating. dishwasher. TV").toString());

    }
}