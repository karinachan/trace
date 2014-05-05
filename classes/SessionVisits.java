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

      private static final long serialVersionUID = 1L; //don't really know what this means
    private final String userID = "admin";
    private final String password = "password";
    private int visits;
    private final String SHOW_BUTTON = "show visits";
    private final String BN_INPUT = "bn"; //change this to B numbers
    private String CRN =""; 
    private HashMap<String,String> studentlist = new HashMap<String,String>(); 

    protected void doRequest(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        HttpSession session = req.getSession(true);
        res.setContentType("text/html");
        res.setHeader("pragma", "no-cache");
        PrintWriter out = res.getWriter();
	visits=0; //can't initialize to null 
        String self = res.encodeURL(req.getRequestURI());

	// out.println(visits);
        HashMap<String,String> inSession =
            (HashMap<String,String>) session.getAttribute("students_loggedin");
        if( inSession == null ) {
            inSession = new HashMap<String,String>();
            session.setAttribute("students_loggedin",inSession);
        }

	CRN = req.getParameter("crn"); //should be the class number
	//	out.println(CRN);

        Connection con = null;
	 String userName = null;
	    String sessionID = null;
        try {
	    pageheader(out,"Session Visits");
            con = TraceDB.connect("trace_db"); //need to change this to another one

	    Cookie [] cookies= req.getCookies();

	   

	    
	 
	    for (Cookie cookie: cookies){
		out.println(cookie.getName());
		if (cookie!=null){
		    if(cookie.getName().equals("user")){
			//		out.println("checking for userName");
			userName = cookie.getValue();
			//	out.println(userName);
		    }
		    if(cookie.getName().equals("JSESSIONID")){
			//	out.println("checking for ID");
			sessionID = cookie.getValue();
			//	out.println(sessionID); }
		}
		}}

       

            printWelcome(session,out, con);

            String bn = req.getParameter(BN_INPUT); //will change to B numbers
            String stuName = req.getParameter("title"); //will change to B numbers
	    String x = req.getParameter("x");
	    //   out.println("what is x when nothing is selected"+x);
	    //	    out.println(userName);
	    out.println("visits"+visits);
	    out.println("username"+userName);
	    if(visits==0){
		out.println("in if");
	       	visits = updateVisits(session, out);
		out.println("after updatevisits");
		PreparedStatement query = con.prepareStatement("select studname, students.bid from taking, students where crn=? and students.bid=taking.bid;");
	       	query.setString(1, CRN);
		ResultSet result = query.executeQuery();
	    
		while(result.next()){
		    String student = result.getString("studname");
		    String bid = result.getString("bid");
		    studentlist.put(bid, student);
		}
	    }
	    else {
       		RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
		out.println("<font color=red>Either user name or password is wrong.</font>");
		rd.include(req, res);
	    }
		
	    if (x!=null){ 
		//	out.println(bn+stuName);
		inSession.remove(bn);
		studentlist.put(bn, stuName);
	    } else if (inSession.size()!=0){
		List keys= new ArrayList(inSession.keySet());
		for (int i=0; i<keys.size(); i++) {
		    studentlist.remove(inSession.get(i));
		}}
	    
		else {
		    out.println("didn't need to do anything");
		}
		
	
            //need to add the crn part

            addtolist(con, inSession,out,bn,stuName, x);
            processShowinSession(req,out,self,inSession);
            printforms(out,con,self);
        }
        catch (SQLException e) {
            out.println("Error: " + e);

        }
        catch (Exception e) {
	    try{
	    if (!userName.equals(null)){
	    	RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
		  out.println("<font color=red>Either user name or password is wrong.</font>");
            rd.include(req, res);
	    }}
	    catch (Exception g){
		out.println("welp");
	    }
	    // e.printStackTrace(out);
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

    private int updateVisits(HttpSession session, PrintWriter out) {
        Integer visits = (Integer)session.getAttribute("visits");
	out.println("visits1"+visits);
            visits = (Integer) (visits.intValue()+1);
      
        // Store back in the session
        session.setAttribute("visits",(Integer) visits);
	out.println("visits2"+visits);
        return visits;
    }

    private void printWelcome(HttpSession session, PrintWriter out, Connection con) throws SQLException {
	//	out.println("welcome");
        Integer visits = (Integer)session.getAttribute("visits");
	PreparedStatement query = con.prepareStatement("SELECT tid, crn, roomnum, length from sessions where crn=?");
	query.setString(1, CRN);
	ResultSet rs = query.executeQuery();
	if(rs.next()){
	    int tid = rs.getInt("tid");
	    int crn = rs.getInt("crn");
	    String room = rs.getString("roomnum");
	    int length = rs.getInt("length");
	    PreparedStatement classquery = con.prepareStatement("SELECT className, vtype from classes where crn=?");
	    classquery.setInt(1,crn);
	    ResultSet classrs = classquery.executeQuery();
	    if(classrs.next()){
		String className = classrs.getString("className");
		String type = classrs.getString("vtype");
		PreparedStatement studentquery = con.prepareStatement("SELECT studname from students where bid=?");
		studentquery.setInt(1, tid);
		ResultSet studrs = studentquery.executeQuery();
		if(studrs.next()){
		    String studname = studrs.getString("studname");
		    out.println("<h1>"+className+" "+type+"<h1>");
		    out.println("<h2>Room: " + room + "     " + "Length: " + length + " hours" + "     " + "Tutor: " + studname+"</h2>");
		}
	    }
	}

	
	
    }
    //add students to the logged in list. Modify this so that single tutor + multiple tutor options
    //also revise the quantity so that the hashmap value is a <String,String[]?> (tutee,tutor)


    private void addtolist(Connection con, HashMap<String,String> loggedin, PrintWriter out, String bn, String stuName, String x) throws SQLException{
	//   out.println("in addtolist");
        //out.println("bn: "+bn);

	//	out.println("in afftolist"); 

        if( bn!=null && x==null) { //if B number exists
	    /*    out.println("<p>Thanks for logging in! <strong>"
		  +stuName+"</strong> ("+bn+"); we'll record your visit.\n");*/
	    //	    out.println("in if"); 

            String Curr = loggedin.get(bn);

            loggedin.put(bn,stuName);
	    try{
	    studentlist.remove(bn);
	    } catch (Exception e){
		out.println("Carry on");
	    }
	    /*  PreparedStatement visitingquery = con.prepareStatement("Select vid from sessions where crn=?");
	    out.println("crn"+CRN);
	    visitingquery.setString(1, CRN);
	    ResultSet results = visitingquery.executeQuery();
	    String vid = ""; 
	    out.println("in");
	    if(results.next())
		vid = results.getString("vid");
	    out.println("vid");
            PreparedStatement query = con.prepareStatement("INSERT into visiting (bid, vid) VALUES(?, ?)");
            query.setString(1, bn);
	    query.setString(2, vid); 
            query.executeUpdate();
            out.println("added to visiting");*/



        } else {
          out.println("Please enter something!");
        }
	//  out.println("reached end");
    }



    private void processShowinSession(HttpServletRequest req,
                                 PrintWriter out,
                                 String self,
                                 HashMap<String,String> loggedin) {

	showlogged(out,loggedin, self);
	    
    }

    private void showlogged(PrintWriter out, HashMap<String,String> loggedin, String self) {
        out.println("<p>Logged in students include: ");
        Set keys = loggedin.keySet();
        Iterator it = keys.iterator();
        out.println("<ul>");
        while (it.hasNext()) {
            String key = (String) it.next();
            out.println("<form method='post' action='"+self+"'>"+
			"<input type='hidden' name='"+BN_INPUT+"' value='"+key+"'>"+
			"<input type='hidden' name='title' value='"+(loggedin.get(key))+"'>"+
			"<input type='hidden' name='crn' value='"+CRN+"'>"+
			"<li><input type='submit' name='x' value='x'>" +(loggedin.get(key)) + "</form>");
        }
        out.println("</ul>");
    }

    //change this to show the list of kids in each class
    private void printforms(PrintWriter out, Connection con, String self)
        throws SQLException
    {

	Set keys = studentlist.keySet();
	Iterator it= keys.iterator();
	out.println("<ol>");
	while (it.hasNext()) {
	    String key = (String) it.next();
	   
	    

	    out.println("<form method='post' action='"+self+"'>"+
			"<input type='hidden' name='"+BN_INPUT+"' value='"+key+"'>"+
			"<input type='hidden' name='title' value='"+(studentlist.get(key))+"'>"+
			"<input type='hidden' name='crn' value='"+CRN+"'>"+
			"<li><input type='submit' value='Log in '> " +(studentlist.get(key))+"</form>");
	}
	out.println("</o1>");

	/*     Statement query = con.createStatement();
        ResultSet result = query.executeQuery("select students.bid, studname, classes.crn, className from taking, classes, students where students.bid=taking.bid and taking.crn=classes.crn order by studname;");

        out.println("<ol>");
        while(result.next()) {

            String bn = result.getString("students.bid");
            String stuName = result.getString("studname");
            //add classes later maybe if needed (need to check if the class even has HR/SI)
            if(!result.wasNull()) {
                out.println("<form method='post' action='"+self+"'>"+
                            "<input type='hidden' name='"+BN_INPUT+"' value='"+bn+"'>" +
                            "<input type='hidden' name='title' value='"+stuName+"'>\n"+
                            "<li><input type='submit' value='add to '> "+stuName+"</form>");
            } else {
                out.println("<li> &nbsp;"); // should never happen
            }
	    }*/

	//	while(
        //out.println("</ol>");
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

	 String user = req.getParameter("user");
        String pwd = req.getParameter("pwd");
	PrintWriter out = res.getWriter();
         
        if(userID.equals(user) && password.equals(pwd)){
            HttpSession session = req.getSession();
            session.setAttribute("user", "Swag");
            //setting session to expire in 30 mins
            session.setMaxInactiveInterval(30*60); 
            Cookie userName = new Cookie("user", user);
            res.addCookie(userName);

	    out.println("cookie added");

       
        }else if (visits>=0) {
	    out.println("chewy");
	    out.println(visits);
	    out.println("the user"+user);
	    out.println("thepwd"+ pwd);
	} else{
	    out.println("crunch");
	    out.println(visits);
            RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
	    //  PrintWriter out= res.getWriter();
            out.println("<font color=red>Either user name or password is wrong.</font>");
            rd.include(req, res);
        }
        doRequest(req,res);
    }

}