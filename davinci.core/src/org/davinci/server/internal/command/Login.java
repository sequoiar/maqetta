package org.davinci.server.internal.command;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.davinci.server.Command;
import org.davinci.server.IDavinciServerConstants;
import org.davinci.server.ServerManager;
import org.davinci.server.user.User;

public class Login extends Command {

	@Override
	public void handleCommand(HttpServletRequest req, HttpServletResponse resp,
			User user) throws IOException {
		String name=req.getParameter("userName");
		String password=req.getParameter("password");
		String noRedirect=req.getParameter("noRedirect");
	
		String response=null;
			user=ServerManager.getServerManger().getUserManager().login(name, password);
			if (user!=null)
			{
				String redirect = (String)req.getSession().getAttribute(IDavinciServerConstants.REDIRECT_TO);
				this.responseString= (redirect!=null)? redirect : "OK" ;
				HttpSession session = req.getSession(true);
				session.setAttribute(IDavinciServerConstants.SESSION_USER, user);
			}
			else	
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);

	}

}
