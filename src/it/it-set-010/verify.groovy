import org.apache.commons.lang.StringUtils
import org.codehaus.plexus.util.FileUtils

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory

class Checker
{
    def result = true;
    def basedir;
    def buildLog;

    public Checker(File basedir) {
        this.basedir = basedir;
        this.buildLog = FileUtils.fileRead( new File( basedir, "build.log" ) );
    }

    def readXPath( String pom, String xPathExpression )
    {
        def stream = new FileInputStream( new File( basedir, pom ) );
        try
        {
            return XPathFactory.newInstance()
                    .newXPath()
                    .evaluate( xPathExpression, DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse( stream ).documentElement );
        }
        finally
        {
            stream.close();
        }
    }

    Checker check( String message, String pom, String xpath, String expected )
    {
        if ( result )
        {
            try
            {
                def actual = readXPath( pom, xpath )
                if ( !StringUtils.equals( expected, actual ) )
                {
                    System.out.println( pom + " [xpath:" + xpath + "] expected '" + expected + "' found '" + actual + "' : " + message );
                    result = false;
                }
            }
            catch ( Throwable t )
            {
                t.printStackTrace();
                result = false;
            }
        }
        return this;
    }

    Checker checkLog( String error )
    {
        if ( buildLog.contains(error) ) {
            System.out.println("Build error found! " + error + ", see the build log")
            result = false;
        }
        return this;
    }
}

return new Checker(basedir)
        .check( "pom.xml change", "pom.xml", "/project/version", "1.0")
        .check( "child/pom.xml change", "child/pom.xml", "/project/version", "2.0")
        .checkLog( "java.io.FileNotFoundException" )
        .result;
