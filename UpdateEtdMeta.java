package backlog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

public class UpdateEtdMeta
{
  private static final int SIX_MONTHS = 6;
  private static final int TWELVE_MONTHS = 12;
  private static final int TWENTYFOUR_MONTHS = 24;
  private static final int CODE_1 = 1;
  private static final int CODE_2 = 2;
  private static final int CODE_3 = 3;

  private static final DateFormat DATE_OUTPUT = 
    new SimpleDateFormat( "MM/dd/yyyy" );
  private static final DateFormat MONTH_OUTPUT = 
    new SimpleDateFormat( "MM" );
  private static final DateFormat DAY_OUTPUT = 
    new SimpleDateFormat( "dd" );
  private static final DateFormat YEAR_OUTPUT = 
    new SimpleDateFormat( "yyyy" );

  private static int embargoCode;
  private static String proQuestID;
  private static File baseDir;
  private static File pdf;
  private static File xml;
  private static Date modDate;
  private static GregorianCalendar releaseDate;
  private static Vector<String> lines;

  public UpdateEtdMeta()
  {
  }

  public static void main( String[] args )
  {
    baseDir = new File( args[ 0 ] );
    proQuestID = args[ 1 ];
    embargoCode = Integer.parseInt( args[ 2 ] );

    try
    {
      identifyFiles();
      if ( pdf != null && xml != null )
      {
        calculateReleaseDate();
        populateLines();
        updateMetaData();
      }
      else
      {
        System.err.println( "Problem finding either PDF or XML file for ETD " + 
                            proQuestID );
      }
    }
    catch ( IOException ioe )
    {
      System.err.println( ioe.getMessage() );
    }
  }

  private static void identifyFiles()
    throws IOException
  {
    File[] files;

    files = baseDir.listFiles();
    pdf = null;
    xml = null;

    for ( File entry: files )
    {
      if ( entry.isFile() && 
           entry.getName().toLowerCase().endsWith( "pdf" ) )
        pdf = new File( entry.getCanonicalPath() );
      if ( entry.isFile() && 
           entry.getName().toLowerCase().endsWith( "xml" ) )
        xml = new File( entry.getCanonicalPath() );
    }
  }

  private static void calculateReleaseDate()
  {
    modDate = new Date( pdf.lastModified() );
    releaseDate = 
        new GregorianCalendar( Integer.parseInt( YEAR_OUTPUT.format( modDate ) ), 
                               Integer.parseInt( MONTH_OUTPUT.format( modDate ) ), 
                               Integer.parseInt( DAY_OUTPUT.format( modDate ) ) );
    switch ( embargoCode )
    {
      case CODE_1:
        releaseDate.add( GregorianCalendar.MONTH, SIX_MONTHS );
        break;
      case CODE_2:
        releaseDate.add( GregorianCalendar.MONTH, TWELVE_MONTHS );
        break;
      case CODE_3:
        releaseDate.add( GregorianCalendar.MONTH, TWENTYFOUR_MONTHS );
        break;
    }
  }

  private static void populateLines()
    throws FileNotFoundException, IOException
  {
    BufferedReader reader;
    String theLine;

    reader = new BufferedReader( new FileReader( xml ) );
    lines = new Vector<String>();
    theLine = null;

    while ( ( theLine = reader.readLine() ) != null )
      lines.add( theLine );

    reader.close();
  }

  private static void updateMetaData()
    throws IOException
  {
    BufferedWriter writer;

    writer = new BufferedWriter( new FileWriter( xml ) );
    for ( String copyLine: lines )
    {
      if ( copyLine.contains( "embargo_code" ) )
      {
        writer.write( "<!--" );
        writer.write( copyLine );
        writer.write( "-->" );
        writer.newLine();
        writer.write( copyLine.substring( 0, 
                                          copyLine.indexOf( "embargo_code" ) + 
                                          14 ) );
        writer.write( "4" );
        writer.write( copyLine.substring( copyLine.indexOf( "embargo_code" ) + 
                                          15 ) );
        writer.newLine();
      }
      else if ( copyLine.contains( "DISS_restriction" ) )
      {
        writer.write( "<!-- DISS_restriction block added by UCLA " + 
                      new Date() + " -->" );
        writer.newLine();
        writer.write( copyLine.substring( 0, copyLine.length() - 2 ) );
        writer.write( ">" );
        writer.newLine();
        writer.write( "<DISS_sales_restriction code=\"1\" remove=\"" + 
                      DATE_OUTPUT.format( releaseDate.getTime() ) + 
                      "\"/>" );
        writer.newLine();
        writer.write( "</DISS_restriction>" );
        writer.newLine();
      }
      else
      {
        writer.write( copyLine );
        writer.newLine();
      }
    }
    writer.flush();
    writer.close();
  }
}
