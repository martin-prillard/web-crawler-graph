package fr.paristech.telecom.inf344.crawler.robot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RobotMT {
    /* Protected fields and methods */
    protected BlockingQueue<String> candidates; /* just this modification */
    protected Set<String> done;
    
    protected final int initialQueueSize=10;
    
    protected RobotMT(String ua, long delay) {
        userAgentName=ua;
        delayBetweenRequests=delay;
    }

    protected abstract Comparator<String> comparator();
    
    protected abstract void initialize(String url);
    protected abstract void dealWith(String url, Set<String> s);

    protected void executionLoop(Set<String> seed,long seconds)
    {
        candidates.clear();
        done.clear();
        
        for(String s:seed)
            initialize(s);  
        candidates.addAll(seed);
    }
    
    protected final void processURL(String url) {
        Set<String> links=retrieveLinks(url);
        synchronized(done) {
            if(!done.contains(url)) {
                dealWith(url,links);
                done.add(url);
            }
        }
    }

    /* Private fields and methods */
    private final long delayBetweenRequests;
    private Map<String, Set<String>> exclusions=new Hashtable<String, Set<String>>();;
    private Map<String, Long> lastRequest=new Hashtable<String,Long>();
    private final String userAgentName;
    
    private static class HTMLLinkExtractionHandler extends ContentHandler {
        @Override
        public Object getContent(URLConnection urlc) throws IOException {
            InputStreamReader in = new InputStreamReader(
                    urlc.getInputStream(),
                    "ISO-8859-1");
            StringBuffer s = new StringBuffer();
            Set<String> urls = new HashSet<String>();
            
            try {
                int c=in.read();
            
                while(c != -1) {
                    s.append((char) c);
                    c=in.read();
                }
            } finally {
                in.close();
            }
        
            Matcher m=Pattern.compile("<\\s*(a|base|area)\\s[^>]*href\\s*=\\s*['\"]?(.*?)[#\\s'\">]",
                                                   Pattern.CASE_INSENSITIVE).matcher(s);
            
            if(!m.find()) {
                m=Pattern.compile("<\\s*(frame)\\s[^>]*src\\s*=\\s*['\"]?(.*?)[#\\s'\">]",
                                           Pattern.CASE_INSENSITIVE).matcher(s);
                if(!m.find()) {
                    m=Pattern.compile("<\\s*(meta)\\s[^>]*http-equiv\\s*=\\s*['\"]?(Refresh)['\"]?\\s+"+
                                              "content\\s*=[^>]*URL\\s*=\\s*(.*?)[#\\s'\">]", Pattern.CASE_INSENSITIVE).
                                              matcher(s);   
                    if(!m.find()) {
                        m=Pattern.compile("(location)\\s*=\\s*['\"]?(.*?)[#\\s'\">]",
                                               Pattern.CASE_INSENSITIVE).matcher(s);
                    }           
                }
            }
            
            URL context=urlc.getURL();
        
            m.reset();
        
            while(m.find()) {
                try {
                    if (m.group(1).equals("base"))
                        context=new URL(m.group(2));
                    else {
                        URL u=new URL(context,m.group(2));
                        String proto=u.getProtocol();
                        
                        String us=u.toString();
                        
                        if(us.equals(urlc.getURL().toString()) || !proto.equals("http"))
                            continue;
                        
                        if(u.getPath().isEmpty()) // Add a final / to empty paths
                            us=us+"/";
                        
                        urls.add(us);
                    }
                } catch (MalformedURLException e) { /* Ignore this malformed URL */ }
            }
            
            return urls;
        }
    }

    static {
        URLConnection.setContentHandlerFactory(new ContentHandlerFactory() {
            public ContentHandler createContentHandler(String mimetype) {
                if(mimetype.equals("text/html") || mimetype.equals("application/xhtml+xml"))
                    return new HTMLLinkExtractionHandler();
                else            
                    return null;
            }
        });
        
        System.setProperty("sun.net.client.defaultConnectTimeout","5000");
        System.setProperty("sun.net.client.defaultReadTimeout","5000");
    }
    
    @SuppressWarnings("unchecked")
    private final Set<String> retrieveLinks (String u) {
        URL url=null;
        
        try {
            url=new URL(u.toString());
        } catch(MalformedURLException e) {
            return null;
        }

        String host = url.getHost();
        int port = url.getPort();

        if (port == -1)
            port = 80;

        String fullhost = host + ":" + port;

        if(!exclusions.containsKey(fullhost)) {
            addExclusions(host,port);
        }

        Set<String> excl = exclusions.get(fullhost);

        if (excl != null)
            for(String item:excl)
                if (url.getFile().startsWith(item))
                    return null;

        if (lastRequest.containsKey(fullhost)) {
            long delay=delayBetweenRequests - 
              (System.currentTimeMillis()-lastRequest.get(fullhost));

            while(delay>0) {
                try {Thread.sleep(delay+1);} catch (InterruptedException e)
                  { /* Ignore */ }

                delay=delayBetweenRequests - 
                  (System.currentTimeMillis()-lastRequest.get(fullhost));
            }
        }

        lastRequest.put(fullhost,System.currentTimeMillis());

        URLConnection urlc;
        try {
            urlc = url.openConnection();
        } catch (IOException e) {
            // Connection impossible
            return null;
        }

        urlc.setRequestProperty("User-Agent", userAgentName);
        urlc.setRequestProperty("Accept", "application/xhtml+xml, text/html; q=0.9");

        try {
            return (Set<String>) urlc.getContent(new Class[] {Set.class});
        } catch (IOException e) {
            return null;
        }
    }
    
    private void addExclusions(String host, int port) {
        String fullhost=host+":"+port;
        HashSet<String> s=new HashSet<String>();

        try {
            URLConnection urlc = (new URL("http",host,port,"/robots.txt")).openConnection();
            urlc.setRequestProperty("User-Agent",userAgentName);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(urlc.getInputStream(),"ISO-8859-1"));

            String line;

            Pattern ua = Pattern.compile("^User-agent:\\s*(\\*|(?i).*"+userAgentName+".*)\\s*(#.*)?$"); 
            Pattern blank = Pattern.compile("^\\s*$");
            Pattern disallow = Pattern.compile("^Disallow:\\s*([^\\s#]+).*");

            while((line = in.readLine()) != null) {
                if (ua.matcher(line).matches()) {
                    while ((line = in.readLine()) !=null && !blank.matcher(line).matches()) {
                        Matcher m = disallow.matcher(line);
                        if(m.matches()) {
                            s.add(m.group(1));
                        }
                    }
                    break;  
                }
            }       
        } catch (IOException e) { /* Ignore this exception */ }

        exclusions.put(fullhost,s);
    }
}