import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;
/*
Goal: Establish sessions tracking.
1. Generate the list of people taking classes
2. When you click on their names then they are logged in
3. A cookie is stored and the length of their session is inserted

*/
public class SessionVisits extends HttpServlet
{

    private final String SHOW_BUTTON = "show visits";
    private final String BN_INPUT = "bn"; //change this to B numbers

    protected void doRequest(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        HttpSession session = req.getSession(true);
        res.setContentType("text/html");
        res.setHeader("pragma", "no-cache");
        PrintWriter out = res.getWriter();

        String self = res.encodeURL(req.getRequestURI());

        int visits = updateVisits(session);

        HashMap<String,String> inSession =
            (HashMap<String,String>) session.getAttribute("students_loggedin");
        if( inSession == null ) {
            inSession = new HashMap<String,String>();
            session.setAttribute("students_loggedin",inSession);
        }

        Connection con = null;
        try {

            con = TraceDB.connect("trace_db"); //need to change this to another one

            pageheader(out,"Visit sessions");

            printWelcome(session,out);

            String bn = req.getParameter(BN_INPUT); //will change to B numbers
            String stuName = req.getParameter("title"); //will change to B numbers

            //need to add the crn part

            addtolist(inSession,out,bn,stuName);
            processShowinSession(req,out,self,inSession);
            printforms(out,con,self);
        }
        catch (Exception e) {
            e.printStackTrace(out);
        }
        finally {
            if( con != null ) {
                try {
                    con.close();
                }
                catch( Exception e ) {
                    e.printStackTrace(out);
                }
            }
        }
        out.println("</body></html>");
    }

    private int updateVisits(HttpSession session) {
        Integer visits = (Integer)session.getAttribute("visits");
        if( visits == null ) {
            visits = (Integer) 0;
        } else {
            visits = (Integer) (visits.intValue()+1);
        }
        // Store back in the session
        session.setAttribute("visits",(Integer) visits);
        return visits;
    }

    private void printWelcome(HttpSession session, PrintWriter out) {
        Integer visits = (Integer)session.getAttribute("visits");
        if( visits > 1 ) {
                out.println ("<p>Welcome back!  You've visited " + visits + " times.\n");
            } else {
                out.println ("<p>Welcome! This page allows you to log students in.\n");
            }
    }
    //add students to the logged in list. Modify this so that single tutor + multiple tutor options
    //also revise the quantity so that the hashmap value is a <String,String[]?> (tutee,tutor)


    private void addtolist(HashMap<String,String> loggedin, PrintWriter out, String bn, String stuName) {
        out.println("in addtolist");
        //out.println("bn: "+bn);
        if( !bn.equals("")) { //if B number exists
            out.println("<p>Thanks for logging in! <strong>"
                        +stuName+"</strong> ("+bn+"); we'll record your visit.\n");


                String Curr = loggedin.get(bn);

            loggedin.put(bn,stuName);
        } else {
          out.println("Please enter something!");
        }
        out.println("reached end");
    }



    private void processShowinSession(HttpServletRequest req,
                                 PrintWriter out,
                                 String self,
                                 HashMap<String,String> loggedin) {
        out.println("<form method='post' action='"+self+"'>"
                    +"<input type='submit' name='submit' value='"+SHOW_BUTTON+"'>"
                    +"</form>\n");
        String submit = req.getParameter("submit");
        if( submit != null && submit.equals(SHOW_BUTTON) ) {
            showlogged(out,loggedin);
        }
    }

    private void showlogged(PrintWriter out, HashMap<String,String> loggedin) {
        out.println("<p>Logged in students include: ");
        Set keys = loggedin.keySet();
        Iterator it = keys.iterator();
        out.println("<ul>");
        while (it.hasNext()) {
            String key = (String) it.next();
            out.println("<li>" + key + " => " + (loggedin.get(key)).toString());
        }
        out.println("</ul>");
    }

    //change this to show the list of kids in each class
    private void printforms(PrintWriter out, Connection con, String self)
        throws SQLException
    {

        Statement query = con.createStatement();
        ResultSet result = query.executeQuery("select students.bid, studname, classes.crn, className from taking, classes, students where students.bid=taking.bid and taking.crn=classes.crn order by studname;");

        out.println("<ol>");
        while(result.next()) {

            String bn = result.getString("students.bid");
            String stuName = result.getString("studname");
            //add classes later maybe if needed (need to check if the class even has HR/SI)
            if(!result.wasNull()) {
                out.println("<form method='post' action='"+self+"'>"+
                            "<input type='hidden' name='BN_INPUT' value='"+bn+"'>" +
                            "<input type='hidden' name='title' value='"+stuName+"'>\n"+
                            "<li><input type='submit' value='add to '> "+stuName+"</form>");
            } else {
                out.println("<li> &nbsp;"); // should never happen
            }
        }
        out.println("</ol>");
    }

    private void pageheader(PrintWriter out, String title) {
        out.println("<!doctype html>\n"
                    + "<html lang='en'>\n"
                    + "<head>\n"
                    + "<title>"+ title + "</title>\n"
                    + "<meta charset='utf-8'>\n"
                    + "<link rel='stylesheet' type='text/css' href='../css/webdb-style.css'>\n"
                    + "</head>\n");

    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        doRequest(req,res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        doRequest(req,res);
    }

}
