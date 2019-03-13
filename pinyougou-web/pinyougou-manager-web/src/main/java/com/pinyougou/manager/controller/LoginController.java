package com.pinyougou.manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller
public class LoginController {

    /** 注入身份认证管理器 */
    @Autowired
    private AuthenticationManager authenticationManager;

    /** 登录认证 */
    @RequestMapping("/user/login")
    public String login(String username, String password,
                        HttpServletRequest request){
        // 判断请求方式
        if (request.getMethod().equalsIgnoreCase("post")){
            String checkCode = (String) request.getSession().getAttribute("CHECKCODE_SERVER");
            if (!request.getParameter("code").equalsIgnoreCase(checkCode)) {
                return "redirect:/login.html";
            }
            System.out.println(username + "==" + password);
            // 创建用户名与密码认证对象
            UsernamePasswordAuthenticationToken token
                    = new UsernamePasswordAuthenticationToken(username,password);
            try {
                // 调用认证方法，返回认证对象
                Authentication authenticate = authenticationManager
                        .authenticate(token);
                // 判断是否认证成功
                if (authenticate.isAuthenticated()){
                    // 设置用户认证成功，往Session中添加认证通过信息
                    SecurityContextHolder.getContext()
                            .setAuthentication(authenticate);
                    // 重定向到登录成功页面
                    return "redirect:/admin/index.html";
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        // 重定向到登录页面
        return "redirect:/login.html";
    }

    // 显示登录用户名
    @GetMapping("/showLoginName")
    @ResponseBody
    public Map<String, String> showLoginName() {
        // 获取登录用户名
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        // 创建Map集合
        Map<String, String> data = new HashMap<>();
        data.put("loginName", loginName);
        return data;
    }

    @GetMapping("/user/checkCode")
    public void checkCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //服务器通知浏览器不要缓存
        response.setHeader("pragma","no-cache");
        response.setHeader("cache-control","no-cache");
        response.setHeader("expires","0");

        //在内存中创建一个长80，宽30的图片，默认黑色背景
        //参数一：长
        //参数二：宽
        //参数三：颜色
        int width = 110;
        int height = 36;
        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

        //获取画笔
        Graphics g = image.getGraphics();
        //设置画笔颜色为灰色
        g.setColor(Color.GRAY);
        //填充图片
        g.fillRect(0,0, width,height);

        //产生4个随机验证码，12Ey
        String checkCode = getCheckCode();
        //将验证码放入HttpSession中
        request.getSession().setAttribute("CHECKCODE_SERVER",checkCode);

        //设置画笔颜色为黄色
        g.setColor(Color.YELLOW);
        //设置字体的小大
        g.setFont(new Font("黑体",Font.BOLD,24));
        //向图片上写入验证码
        g.drawString(checkCode,30,25);

        //将内存中的图片输出到浏览器
        //参数一：图片对象
        //参数二：图片的格式，如PNG,JPG,GIF
        //参数三：图片输出到哪里去
        ImageIO.write(image,"PNG",response.getOutputStream());
    }
    /**
     * 产生4位随机字符串
     */
    private String getCheckCode() {
        String base = "0123456789ABCDEFGabcdefg";
        int size = base.length();
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        for(int i=1;i<=4;i++){
            //产生0到size-1的随机值
            int index = r.nextInt(size);
            //在base字符串中获取下标为index的字符
            char c = base.charAt(index);
            //将c放入到StringBuffer中去
            sb.append(c);
        }
        return sb.toString();
    }
}
