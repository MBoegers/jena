/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package fm2.atlas;

import java.io.File ;
import java.io.FileInputStream ;
import java.io.IOException ;
import java.io.InputStream ;
import java.security.AccessControlException ;

import org.openjena.atlas.lib.IRILib ;
import org.openjena.atlas.lib.Lib ;
import org.openjena.atlas.web.TypedStream ;
import org.openjena.riot.Lang ;
import org.openjena.riot.WebContent ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.util.FileUtils ;

/** Location files in the filing system.
 *  A FileLocator can have a "current directory" - this is separate from any
 *  location mapping (see @link{LocationMapping}) as it applies only to files.
 */

public class LocatorFile implements Locator
{
    static Logger log = LoggerFactory.getLogger(LocatorFile.class) ;
    private String altDir = null ;
    private String altDirLogStr = "" ;

    public LocatorFile() { this(null) ; }
    
    public LocatorFile(String dir)
    {
        if ( dir != null )
        {
            if ( dir.endsWith("/") || dir.endsWith(java.io.File.separator) )
                dir = dir.substring(0,dir.length()-1) ;
            altDirLogStr = " ["+dir+"]" ;
        }
        altDir = dir ;
    }

    // Two LocatorFile are the same if they would look up names to the same files.
    
    @Override
    public boolean equals( Object other )
    {
        return
            other instanceof LocatorFile
            && Lib.equal(altDir, ((LocatorFile) other).altDir );
    }
    
    @Override
    public int hashCode()
    {
        if ( altDir == null ) return 57 ;
        return altDir.hashCode();
    }
    
    /** To a File, after processing the filename for file: or relative filename */
    public File toFile(String filenameIRI)
    {
        String scheme = FileUtils.getScheme(filenameIRI) ;
        String fn = filenameIRI ;
        // Windows : C:\\ is not a scheme name!
        if ( scheme != null && scheme.length() > 1 )
        {
            if ( ! scheme.equalsIgnoreCase("file") )
                // Not filename or a file: IRI
                return null ;
            fn = IRILib.IRIToFilename(filenameIRI) ;
        }
        // AltDir.
        // "/" is a path separator on Windows.
        if ( altDir != null && ! fn.startsWith("/") && ! fn.startsWith(File.pathSeparator) )
            fn = altDir+"/"+fn ;
        return new File(fn) ;
    }
    
    public boolean exists(String fileIRI)
    {
        File f = toFile(fileIRI) ;
        
        if ( f == null )
            return false ;
        
        return f.exists() ;
    }
    
    /** Opne anything that looks a bit like a file name */ 
    public TypedStream open(String filenameIRI)
    {
        File f = toFile(filenameIRI) ;

        try {
            if ( f == null || !f.exists() )
            {
                if ( StreamManager.logAllLookups && log.isTraceEnabled())
                    log.trace("Not found: "+filenameIRI+altDirLogStr) ;
                return null ;
            }
        } catch (AccessControlException e) {
            log.warn("Security problem testing for file", e);
            return null;
        }
        
        try {
            InputStream in = new FileInputStream(f) ;

            if ( StreamManager.logAllLookups && log.isTraceEnabled() )
                log.trace("Found: "+filenameIRI+altDirLogStr) ;
            
            // Guess content from extension.
            Lang lang = Lang.guess(filenameIRI) ;
            String contentType = WebContent.mapLangToContentType(lang) ;
            String charset = WebContent.getCharsetForContentType(contentType) ;
            return new TypedStream(in, contentType, charset) ;
        } catch (IOException ioEx)
        {
            // Includes FileNotFoundException
            // We already tested whether the file exists or not.
            log.warn("File unreadable (but exists): "+f.getPath()+" Exception: "+ioEx.getMessage()) ;
            return null ;
        }
    }
    
    public String getName()
    {
        String tmp = "LocatorFile" ;
        if ( altDir != null )
            tmp = tmp+"("+altDir+")" ;
        return tmp ;
    }
}
/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */