package com.bipro.ths.controller;

import com.bipro.ths.Validator.UserValidator;
import com.bipro.ths.dto.UserDto;
import com.bipro.ths.model.Meeting;
import com.bipro.ths.model.Role;
import com.bipro.ths.model.User;
import com.bipro.ths.service.MeetingService;
import com.bipro.ths.service.RoleService;
import com.bipro.ths.service.SecurityService;
import com.bipro.ths.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.*;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private MeetingService meetingService;

    @Value("${profile.upload.image.path}")
    private String UPLOADED_FOLDER ;

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String registration(Model model) {
        List<String> usernameList = userService.findAllUsernames();
        model.addAttribute("usernameList", usernameList);
        User user  =  new User();
        user.setFullName("");
        user.setUsername("");
        user.setEmail("");
        user.setPhone("");
        model.addAttribute("user", user);
        model.addAttribute("role", "");
        return "user/registration";
    }

//    @ResponseBody
    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registration(@ModelAttribute("userDtoForm") UserDto userDtoForm, BindingResult bindingResult, Model model) {
        User user = new User();
        user.setFullName(userDtoForm.getFullName());
        user.setUsername(userDtoForm.getUsername());
        user.setPassword(userDtoForm.getPassword());
        user.setPasswordConfirm(userDtoForm.getPasswordConfirm());
        user.setEmail(userDtoForm.getEmail());
        user.setPhone("");
        user.setAccountEnabled(true);

        Set<Role> roles = new HashSet<Role>();
        Role role = roleService.findAllByName(userDtoForm.getRoles());
        System.out.println("reg76  " + role.getName() + " | " + userDtoForm.getRoles());
        roles.add(role);
        user.setRoles( roles);
        System.out.println("reg77  " + user.getFullName() + " | " + user.getUsername() +
                " | " +user.getPassword() + " | " +user.getEmail() + " | "  +user.getRoles() + " | " + role.getName());
        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            List<String> usernameList = userService.findAllUsernames();
            model.addAttribute("usernameList", usernameList);
            model.addAttribute("user", user);
//            model.addAttribute("role", role.getName());
//            model.addAttribute("fullName1", bindingResult.getFieldErrors("username"));
            return "user/registration.html";
        }
        userService.save(user);
//        securityService.autologin(userDtoForm.getUsername(), userDtoForm.getPasswordConfirm());
        return "redirect:/login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model, String error, String logout, HttpServletRequest request, HttpSession session ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
//            String username = ((UserDetails)principal).getUsername();
            return "redirect:/";
        }
        if (error != null) {
            AuthenticationException ex = (AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            if (ex != null) {
                error = ex.getMessage();
                    model.addAttribute("errorMessage", error.toUpperCase());
            }
        }
        if (logout != null) {
            SecurityContextHolder.clearContext();
            if (session != null) {
//                request.getSession().invalidate();
                session.invalidate();
                System.out.println("Session Invalidate");
            }

            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "user/login.html";
    }


    @RequestMapping(value = {"/userProfile/{id}", "/viewUser/{id}"}, method = RequestMethod.GET)
    public String viewUser(Model model, @PathVariable("id") String id, HttpServletRequest request) {
        User user = userService.findAllById(Integer.parseInt(id));
//        HttpSession session = request.getSession(false);
//        User currentUser  = (User)session.getAttribute("currentUser");
        Set<Role> roles = user.getRoles();
        String roleList="";
        for (Iterator<Role> it = roles.iterator(); it.hasNext(); ) {
            Role role = it.next();
            roleList = roleList + role.getName() + ", ";
        }

        model.addAttribute("user", user);
        model.addAttribute("role", roleList);
//        System.out.println("time " + System.currentTimeMillis());
        model.addAttribute("time", System.currentTimeMillis());
        String pathab = Paths.get("").toAbsolutePath().toString() + UPLOADED_FOLDER +  user.getId()+".jpg";
//        byte[] decodedBytes = Base64.getUrlDecoder().decode();
//        String decodedUrl = new String(decodedBytes);
//        String encodedUrl = Base64.getUrlEncoder().encodeToString(pathab.getBytes());
//
//        System.out.println(encodedUrl);
        return "user/userProfile.html";
    }


    @RequestMapping(value = "/userUpdate", method = RequestMethod.POST)
    public String userUpdate(@ModelAttribute("userDtoForm") UserDto userDtoForm, BindingResult bindingResult, Model model,
                             @RequestParam("file") MultipartFile file, HttpSession session, RedirectAttributes redirectAttributes) {
//        UPLOADED_FOLDER = "D://temp//";
//        UPLOADED_FOLDER = Paths.get("").toAbsolutePath().toString()+"//src//main//resources//static//img//profile//";
//        String UPLOADED_FOLDER = servletContext.getContextPath()+"//src//main//resources//static//img//profile//";
//        String UPLOADED_FOLDER=session.getServletContext().getRealPath("//src//main//resources//static//img//profile//");
//        System.out.println("UPLOADED_FOLDER " + UPLOADED_FOLDER);
//        if (file.isEmpty()) {
////            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
//            return "redirect:/file/uploadStatus";
//        }
        User user = userService.findAllById(userDtoForm.getId());
        try {
//        user.setId(userDtoForm.getId());
        user.setFullName(userDtoForm.getFullName());
//        user.setUsername(userDtoForm.getUsername());
//        user.setPassword(userDtoForm.getPassword());
//        user.setPasswordConfirm(userDtoForm.getPasswordConfirm());
        user.setEmail(userDtoForm.getEmail());
        user.setPhone(userDtoForm.getPhone());
//        user.setAccountEnabled(true);
            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
//            Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
            Path path = Paths.get(Paths.get("").toAbsolutePath().toString() + UPLOADED_FOLDER +  user.getId()+".jpg");
            System.out.println(Paths.get("").toAbsolutePath().toString());
            Files.write(path, bytes);
//            redirectAttributes.addFlashAttribute("message","You successfully uploaded '" + file.getOriginalFilename() + "'");
        } catch (IOException e) {
            e.printStackTrace();
        }


//        Set<Role> roles = new HashSet<Role>();
//        Role role = roleService.findAllByName(userDtoForm.getRoles());
//        roles.add(role);
//        user.setRoles( roles);
//        System.out.println("userUpdate 77  " + user.getFullName() + " | " + user.getUsername() +
//                " | " +user.getPassword() + " | " +user.getEmail() + " | "  +user.getRoles() + " | ");
//        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
//            List<String> usernameList = userService.findAllUsernames();
//            model.addAttribute("usernameList", usernameList);
//            model.addAttribute("user", user);
//            model.addAttribute("role", role.getName());
//            model.addAttribute("fullName1", bindingResult.getFieldErrors("username"));
            return "user/registration.html";
        }
       try {
           userService.save(user);
           redirectAttributes.addFlashAttribute("message", "User Information Successfully Updated." );
       } catch (Exception e){
           System.err.println("Exception " + e);
           redirectAttributes.addFlashAttribute("errorMessage", "User Information Successfully Updated.");
       }
        return "redirect:/userProfile/"+user.getId();
    }


    @RequestMapping(value = { "/viewAllUser/{role}" }, method = RequestMethod.GET)
    public String viewAll(ModelMap model, @PathVariable("role") String role, HttpServletRequest request, HttpSession session, Principal principal) {
//        User currentUser = (User)session.getAttribute("currentUser");
//        System.out.println("currentUser p " + principal.getName());
//        if (!principal.getName().equalsIgnoreCase("admin") ){
//            return "redirect:/";
//        }
//        String title  = "List of All " + role.trim().toUpperCase() + "Users" ;  ;
//        List<User> userList = new ArrayList<>();
//        switch (role.trim().toUpperCase()) {
//            case "ADMIN":
//                userList = userService.findAllByRoles(roleService.findAllByName(role.trim().toUpperCase()));
//                break;
//            case "TEACHER":
//                 userList = userService.findAllByRoles(roleService.findAllByName(role.trim().toUpperCase()));
//                break;
//            case "STUDENT":
//                userList = userService.findAllByRoles(roleService.findAllByName(role.trim().toUpperCase()));
//                break;
//        }
        List<User> userList = userService.findAllByRoles(roleService.findAllByName(role.trim().toUpperCase()));
        model.addAttribute("role",  role.trim().toUpperCase() );
        model.addAttribute("userList", userList);
//        System.out.println(userList);
        return "user/viewAllUser.html";
    }

    @RequestMapping(value = {"/userStatusToggle/{role}/{id}"}, method = RequestMethod.GET)
    public String statusToggle(@PathVariable("id") String id, @PathVariable("role") String role, ModelMap model) {
        User user = userService.findAllById(Integer.parseInt(id));
        if (user.isAccountEnabled() == true){
            user.setAccountEnabled(false);
        } else  if (user.isAccountEnabled() == false){
            user.setAccountEnabled(true);
        }
        userService.save(user);
        return "redirect:/viewAllUser/"+role;
    }

    @RequestMapping(value = "/deleteUser/{role}/{id}", method = RequestMethod.GET)
    public String delete(@PathVariable("id") String id, @PathVariable("role") String role, ModelMap model){
        User user = userService.findAllById(Integer.parseInt(id));
        List<Meeting> meetingList =  new ArrayList<>();
        if (role.equalsIgnoreCase("DOCTOR")){
            meetingList = meetingService.findByDoctorId(Long.parseLong(id));
        }else if (role.equalsIgnoreCase("PATIENT")){
            meetingList = meetingService.findByPatientId(Long.parseLong(id));
        }
        for ( Meeting meeting:	meetingList	 ) {
            meetingService.delete(meeting);
        }
            userService.delete(user);
        return "redirect:/viewAllUser/"+role;
    }

//    @ResponseBody
//        @RequestMapping(value = "/usernames", method = RequestMethod.GET)
//    public  List<String> usernames(Model model, HttpServletRequest request, HttpServletResponse response) {
//        HttpSession session;
//        SecurityContextHolder.clearContext();
//        System.out.println("55555555555");
//        session = request.getSession(false);
//        if(session != null) {
//            session.invalidate();
//        }
//            return userService.findAllUsernames();
//    }


//    @RequestMapping(value = "/logout", method = RequestMethod.GET)
//    public String logout(Model model, HttpServletRequest request, HttpServletResponse response) {
//        HttpSession session;
//        SecurityContextHolder.clearContext();
//        System.out.println("55555555555");
//        session = request.getSession(false);
//        if(session != null) {
//            session.invalidate();
//        }
//
//        return "/";
//    }

//    @RequestMapping(value = {"/login-error"}, method = RequestMethod.GET)
//    public String login(HttpServletRequest request, Model model) {
//        HttpSession session = request.getSession(false);
//        String errorMessage = null;
//        if (session != null) {
//            AuthenticationException ex = (AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
//            if (ex != null) {
//                errorMessage = ex.getMessage();
//            }
//        }
//        model.addAttribute("errorMessage", errorMessage);
//        return "user/login.html";
//    }

    @RequestMapping(value = {"/", "/welcome", "/home", "/index"}, method = RequestMethod.GET)
    public String welcome(Model model) {
//        System.out.println("Home " + servletContext.getContextPath());
//        return "index.html";
        return "index.html";
    }

    @RequestMapping(value = {"/home"}, method = RequestMethod.GET)
    public String welcome1(Model model) {
        return "home.html";
    }
}