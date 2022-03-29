import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.parser.JmmParserResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.*;

public class GrammarTest {

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


  List<String> goodPaths() {
    List<String> paths = new ArrayList<String>();

    String path = "test/fixtures/public/";
    String filename1  = "FindMaximum.jmm";
    String filename2  = "HelloWorld.jmm";
    String filename3  = "Lazysort.jmm";
    String filename4  = "Life.jmm";
    String filename5  = "MonteCarloPi.jmm";
    String filename6  = "QuickSort.jmm";
    String filename7  = "Simple.jmm";
    String filename8  = "TicTacToe.jmm";
    String filename9  = "WhileAndIf.jmm";

    
    //paths.add(path+filename1);
   // paths.add(path+filename2);
//    paths.add(path+filename3);
//    paths.add(path+filename4);
//    paths.add(path+filename5);
//    paths.add(path+filename6);
//    paths.add(path+filename7);
//    paths.add(path+filename8);
//    paths.add(path+filename9);

    return paths;
  }
  
  List<String> badPaths() {
    List<String> paths = new ArrayList<String>();
    String path = "test/fixtures/public/fail/syntactical/";

    String filename1  = "BlowUp.jmm";
    String filename2  = "CompleteWhileTest.jmm";
    String filename3  = "LengthError.jmm";
    String filename4  = "MissingRightPar.jmm";
    String filename5  = "MultipleSequential.jmm";
    String filename6  = "NestedLoop.jmm";
    
    paths.add(path+filename1);
    paths.add(path+filename2);
    paths.add(path+filename3);
    paths.add(path+filename4);
    paths.add(path+filename5);
    paths.add(path+filename6);

    return paths;
  }
  
  @Test
  public void goodCode() {
    List<String> good_paths = goodPaths();

    for (String path : good_paths) { 
      test(path, true);
    }
  }

  @Test
  public void badCode() {
    List<String> bad_paths = badPaths();

    for (String path : bad_paths) {
      test(path, false);
    }
  }
}
