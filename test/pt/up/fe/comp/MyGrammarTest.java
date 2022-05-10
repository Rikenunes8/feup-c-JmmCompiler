package pt.up.fe.comp;

import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.parser.JmmParserResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MyGrammarTest {
  String passPath = "test/fixtures/public/";
  String failPath = "test/fixtures/public/fail/syntactical/";

  void test(String path, boolean pass) {
    try {
      String jmm_code = Files.readString(Paths.get(path));
      JmmParserResult parserResult = TestUtils.parse(jmm_code);
      if (pass) {
        TestUtils.noErrors(parserResult.getReports());
      } else {
        TestUtils.mustFail(parserResult.getReports());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void findMaximum() {
    String filename  = "FindMaximum.jmm";
    test(passPath+filename, true);
  }

  @Test
  public void helloWorld() {
    String filename  = "HelloWorld.jmm";
    test(passPath+filename, true);
  }

  @Test
  public void lazysort() {
    String filename  = "Lazysort.jmm";
    test(passPath+filename, true);
  }

  @Test
  public void life() {
    String filename  = "Life.jmm";
    test(passPath+filename, true);
  }

  @Test
  public void monteCarloPi() {
    String filename  = "MonteCarloPi.jmm";
    test(passPath+filename, true);
  }

  @Test
  public void quickSort() {
    String filename  = "QuickSort.jmm";
    test(passPath+filename, true);
  }

  @Test
  public void ticTacToe() {
    String filename  = "TicTacToe.jmm";
    test(passPath+filename, true);
  }

  @Test
  public void whileAndIf() {
    String filename  = "WhileAndIf.jmm";
    test(passPath+filename, true);
  }

  @Test
  public void blowUp() {
    String filename  = "BlowUp.jmm";
    test(failPath+filename, false);
  }

  @Test
  public void completeWhileTest() {
    String filename  = "CompleteWhileTest.jmm";
    test(failPath+filename, false);
  }

  @Test
  public void lengthError() {
    String filename  = "LengthError.jmm";
    test(failPath+filename, false);
  }

  @Test
  public void missingRightPar() {
    String filename  = "MissingRightPar.jmm";
    test(failPath+filename, false);
  }

  @Test
  public void multipleSequential() {
    String filename  = "MultipleSequential.jmm";
    test(failPath+filename, false);
  }

  @Test
  public void nestedLoop() {
    String filename  = "NestedLoop.jmm";
    test(failPath+filename, false);
  }
}
