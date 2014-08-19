import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;
import org.apache.commons.lang.StringEscapeUtils; // To do the string escaping
 
/**
 * Servlet implementation class LoginServlet
 */

public class LoginServlet extends HttpServlet {
  
    // private final String userID = "admin";
    //   private final String password = "password";
    
    private String encrypted="";

    private static String escape(String raw) {
        return StringEscapeUtils.escapeHtml(raw);
    }
    protected void doPost(HttpServletRequest request,
			  HttpServletResponse response) throws ServletException, IOException {
 
        // get request parameters for userID and password
	//change this to grab studentBID entries from the database..
        String user = escape(request.getParameter("user"));
        String pwd = escape(request.getParameter("pwd"));
	String logbid= null;
        PrintWriter out=response.getWriter();
	Connection con=null;

	try{
	    logbid= validate(con,user,pwd);
	    if(!logbid.equals("")){
            HttpSession session = request.getSession(); //create a session here
	    //    session.setAttribute("user", "Swag");
            //setting session to expire in 30 mins
            session.setMaxInactiveInterval(10*60); 
	    session.setAttribute("logbid",logbid);
	    /*  Cookie userName = new Cookie("user", user);
	      Cookie pwdCook= new Cookie("pwd", encrypted);
	      Cookie bidCook= new Cookie("bid", logbid);
	     response.addCookie(userName);
	    response.addCookie(pwdCook);
	    response.addCookie(bidCook); */

            //Get the encoded URL string
            String encodedURL = response.encodeRedirectURL("http://cs.wellesley.edu:8080/trace/servlet/PickClass");  
            response.sendRedirect(encodedURL);
        }else{
            RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
	    //     PrintWriter out= response.getWriter();
            out.println("<font color=red>Either user name or password is wrong.</font>");
            rd.include(request, response);
	    }} catch (SQLException s){
	    out.println("Error: "+ s);
	}

	catch (Exception e) {
	    RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
	    out.println("<font color=red>Access Denied. Please log in.</font>");
	    
            rd.include(request, response);
	    // e.printStackTrace(out);
	}
	finally {
	    if(con!=null){
		try{
		    con.close();
		}
		catch(Exception e){
		    e.printStackTrace(out);
		}
	    }
	}
    }
	
    private String validate(Connection con, String user, String pwd) throws SQLException{
	String tutorbid="";
	try{ 
	    con=TraceDB.connect("trace_db");
	    
	    PreparedStatement sessionUsers= con.prepareStatement("select user, bid from login_user where user=? and cryp=password(?)");
	    sessionUsers.setString(1, user);
	    sessionUsers.setString(2, pwd);
	    ResultSet results = sessionUsers.executeQuery();
	    if(results.next()){
		tutorbid= Integer.toString(results.getInt("bid"));
	
	    }
		
	} catch (SQLException e){
	    return "";
	}catch (Exception e){
	    return "";
	}
	return tutorbid;
    }
      
 
   protected void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        doPost(req,res);
    }
}