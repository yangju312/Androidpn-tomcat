/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.androidpn.server.console.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.androidpn.server.util.Config;
import org.androidpn.server.xmpp.push.NotificationManager;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/** 
 * A controller class to process the notification related requests.  
 *
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class NotificationController extends MultiActionController {

    private NotificationManager notificationManager;

    public NotificationController() {
        notificationManager = new NotificationManager();
    }

    public ModelAndView list(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();
        // mav.addObject("list", null);
        mav.setViewName("notification/form");
        return mav;
    }

    public ModelAndView send(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    	Connection con =null;
        System.out.println("oasdasd");
        PreparedStatement psmt = null;
        String broadcast = ServletRequestUtils.getStringParameter(request,
                "broadcast", "Y");
        String username = ServletRequestUtils.getStringParameter(request,
                "username");
        String title = ServletRequestUtils.getStringParameter(request, "title");
        String message = ServletRequestUtils.getStringParameter(request,
                "message");
        String uri = ServletRequestUtils.getStringParameter(request, "uri");

        String apiKey = Config.getString("apiKey", "");
        logger.debug("apiKey=" + apiKey);

        if (broadcast.equalsIgnoreCase("Y")) {
           notificationManager.sendBroadcast(apiKey, title, message, uri);
        }else
        {
        	try{
        	 SimpleDateFormat Format = new SimpleDateFormat(  
		               "yyyy-MM-dd HH:mm:ss");
			 Date date=new Date();
			 String time=Format.format(date);
			notificationManager.sendNotifcationToUser(apiKey, username, title,
					message, time);
			
			String  url = this.getServletContext().getInitParameter("jdbcs");
			String  user = this.getServletContext().getInitParameter("user");
			String  password = this.getServletContext().getInitParameter("password");
			Class.forName("oracle.jdbc.driver.OracleDriver").newInstance(); 
			 con =DriverManager.getConnection(url,user,password);
			 psmt=con.prepareStatement("INSERT INTO tab_gps_send_message (pid,sendtitle,sendmessage,senduser,zt,zh) VALUES (seq_send_message.nextval,?,?,?,?,?)");
			 psmt.setString(1, title);
			 psmt.setString(2, message);
			 psmt.setString(3, username);
			 psmt.setInt(4, 1);
			 psmt.setString(5, uri);
			 
			 psmt.execute();
			 con.commit();
			}catch (Exception e) {
				e.printStackTrace();
				System.out.println(e.toString());
			} finally {
				try {
					if (psmt != null)
						psmt.close();
					if (con != null)
						con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
				
					e.printStackTrace();
				}
			}
        }

        ModelAndView mav = new ModelAndView();
        mav.setViewName("redirect:notification.do");
        return mav;
    }

}
