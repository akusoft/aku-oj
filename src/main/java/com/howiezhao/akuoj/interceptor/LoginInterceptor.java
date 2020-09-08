package com.howiezhao.akuoj.interceptor;

import com.howiezhao.akuoj.akuLogin.dao.LoginTicket;
import com.howiezhao.akuoj.akuLogin.dao.User;
import com.howiezhao.akuoj.akuLogin.services.impl.UserServicesImpl;
import com.howiezhao.akuoj.utils.CookieUtil;
import com.howiezhao.akuoj.utils.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author LiMing
 * @since 2020/3/10 18:05
 **/
@Component
public class LoginInterceptor implements HandlerInterceptor {


    @Autowired
    private UserServicesImpl userServices;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        //获取cookie

        String ticket = CookieUtil.getValue(request, "ticket");
        if(!StringUtils.isBlank(ticket)){
            LoginTicket loginTicket = userServices.findLoginTicket(ticket);
            //检查凭证是否有效
            if(loginTicket !=null && loginTicket.getStatus()==0 && loginTicket.getExpired().after(new Date())){
                User user = userServices.selectUserById(loginTicket.getUserId());
                //将user存在ThreadLocal中解决线程同步问题
                hostHolder.setUser(user);

            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        User user = hostHolder.getUser();
        if(user!=null && modelAndView !=null){
            modelAndView.addObject("loginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        hostHolder.clear();
    }
}
