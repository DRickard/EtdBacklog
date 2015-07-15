package backlog;

import java.io.BufferedWriter;
import java.io.File;

import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

public class WrteErcFile
{
  private static final String OUTPUT_FILE = "\\mrt-erc.txt";

  private static String proQuestID;
  private static File baseDir;
  private static File xml;
  private static Document input;
  private static String lastName;
  private static String firstName;
  private static String title;
  private static String year;

  public WrteErcFile()
  {
  }

  public static void main( String[] args )
  {
    baseDir = new File( args[ 0 ] );
    proQuestID = args[ 1 ];

    try
    {
      identifyXML();
      if ( xml != null )
      {
        input = 
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( xml );
        populateFields();
        writeOutput();
      }
      else
      {
        System.err.println( "Problem finding XML file for ETD " + 
                            proQuestID );
      }
    }
    catch ( IOException ioe )
    {
      System.err.println( ioe.getMessage() );
    }
    catch ( SAXException saxe )
    {
      System.err.println( saxe.getMessage() );
    }
    catch ( ParserConfigurationException pce )
    {
      System.err.println( pce.getMessage() );
    }
  }

  private static void identifyXML()
    throws IOException
  {
    File[] files;

    files = baseDir.listFiles();
    xml = null;
    for ( File entry: files )
    {
      if ( entry.isFile() && 
           entry.getName().toLowerCase().endsWith( "xml" ) )
        xml = new File( entry.getCanonicalPath() );
    }
  }

  private static void populateFields()
  {
    lastName = 
        input.getElementsByTagName( "DISS_surname" ).item( 0 ).getChildNodes().item( 0 ).getTextContent();
    firstName = 
        input.getElementsByTagName( "DISS_fname" ).item( 0 ).getChildNodes().item( 0 ).getTextContent();
    title = 
        input.getElementsByTagName( "DISS_title" ).item( 0 ).getChildNodes().item( 0 ).getTextContent();
    year = 
        input.getElementsByTagName( "DISS_comp_date" ).item( 0 ).getChildNodes().item( 0 ).getTextContent();
  }

  private static void writeOutput()
    throws IOException
  {
    BufferedWriter writer;
    writer = 
        new BufferedWriter( new FileWriter( new File( baseDir.getAbsolutePath().concat( OUTPUT_FILE ) ) ) );
    writer.write("erc:");
    writer.newLine();
    writer.write("who: ");
    writer.write(lastName + ", " + firstName);
    writer.newLine();
    writer.write("what: ");
    writer.write(title);
    writer.newLine();
    writer.write("when: ");
    writer.write(year);
    writer.newLine();
    writer.write("where: ");
    writer.write("ucla.etd:PQ".concat(proQuestID));
    writer.newLine();
    writer.flush();
    writer.close();
  }
}
