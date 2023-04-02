package test;

import main.Logger;
import main.ParserML;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ParserMLTest {

    @Test
    void predictString() throws IOException {
        ParserML ml = new ParserML(System.getProperty("user.dir")+"/src/main");

        String testResult = ml.query("I like nature. I like fitness. Internet is needed. I love my warmth.", false);
        String testResultClean = ml.query("I like nature. I like fitness. I would like a place to eat food. An alarm is necessary for my safety. warm is great", true);

        assertEquals("\"internet\", \"gym\", \"sports_hall\", \"disability_access\", \"tv_room\", \"dishwasher\", \"tv\", \"central-heating\"", testResult);
        assertEquals("\"alarm\", \"dishwasher\", \"central-heating\"",testResultClean);
        System.out.println(testResultClean);

        System.out.println("dishwash and wifi  " + ml.query("i like dish washing and need wi-fi", true));
        if (testResult.equalsIgnoreCase("\"alarm\""))
            System.out.println("Parser working. ML not responding.");

    }

    @Test
    void predictNoML() {
        ParserML ml = new ParserML(System.getProperty("user.dir")+"/src/main");
        assertEquals("[\"central-heating\", \"fast_wifi\", \"internet\"]", ml.parseNoML("heating. warm. wifi, wi-fi").toString());

    }

        @Test
        void speedTest() {
            System.out.println("Constructor Test");
            long constructorTotalTime = 0;

            ParserML ml = new ParserML(System.getProperty("user.dir") + "/src/main");

            for (int i = 0; i < 10; i++) {
                long startTime = System.nanoTime();
                ml = new ParserML(System.getProperty("user.dir") + "/src/main");
                long endTime = System.nanoTime();
                long duration = (endTime - startTime) / 1000000;
                constructorTotalTime += duration;
                System.out.println("Constructor iteration " + (i+1) + " took: " + duration + " ms");
            }

            System.out.println("Average Constructor time: " + (constructorTotalTime / 10) + " ms\n");

            System.out.println("ParseNoML TEST");
            long parseNoMLTotalTime = 0;

            for (int i = 0; i < 10; i++) {
                long startTime = System.nanoTime();
                String s = ml.parseNoML("I would love ethernet, wheelchair access, world wide web, like doing college work").toString();
                long endTime = System.nanoTime();
                long duration = (endTime - startTime) / 1000000;
                parseNoMLTotalTime += duration;
                System.out.println("ParseNoML with 4 entries iteration " + (i+1) + " took: " + duration + " ms");
            }


            System.out.println("Full ML Test with clean");
            long fullMLCleanTotalTime = 0;

            for (int i = 0; i < 10; i++) {
                long startTime = System.nanoTime();
                String s = ml.query("I would love ethernet, wheelchair access, world wide web, like doing college work", true).toString();
                long endTime = System.nanoTime();
                long duration = (endTime - startTime) / 1000000;
                fullMLCleanTotalTime += duration;
                System.out.println("Full ML with 4 entries and clean iteration " + (i+1) + " took: " + duration + " ms");
            }


            System.out.println("Full ML Test without clean");
            long fullMLNoCleanTotalTime = 0;

            for (int i = 0; i < 10; i++) {
                long startTime = System.nanoTime();
                String s = ml.query("I would love ethernet, wheelchair access, world wide web, like doing college work", false).toString();
                long endTime = System.nanoTime();
                long duration = (endTime - startTime) / 1000000;
                fullMLNoCleanTotalTime += duration;
                System.out.println("Full ML with 4 entries and no clean iteration " + (i+1) + " took: " + duration + " ms");
            }

            System.out.println("Average ParseNoML time: " + (parseNoMLTotalTime / 10) + " ms\n");
            System.out.println("Average ParseNoML time: " + (parseNoMLTotalTime / 10) + " ms\n");
            System.out.println("Average Full ML with clean time: " + (fullMLCleanTotalTime / 10) + " ms\n");
            System.out.println("Average Full ML without clean time: " + (fullMLNoCleanTotalTime / 10) + " ms\n");
        }

}