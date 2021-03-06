package com.carleton.CapstoneSystem.Controllers;

import com.carleton.CapstoneSystem.DTO.CoordinatorDTO;
import com.carleton.CapstoneSystem.DTO.ProfessorDTO;
import com.carleton.CapstoneSystem.DTO.StudentDTO;
import com.carleton.CapstoneSystem.DTO.UserDTO;
import com.carleton.CapstoneSystem.auth.JWTAuthenticationFilter;
import com.carleton.CapstoneSystem.models.*;
import com.carleton.CapstoneSystem.repositories.CoordinatorRepository;
import com.carleton.CapstoneSystem.repositories.ProfessorRepository;
import com.carleton.CapstoneSystem.repositories.StudentRepository;
import com.carleton.CapstoneSystem.repositories.UserRepository;
import com.carleton.CapstoneSystem.utils.RequestErrorMessages;
import com.mysql.jdbc.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;

import javax.jws.soap.SOAPBinding;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.security.Principal;
import java.util.regex.Pattern;

@Controller
public class SignUpLogInController {





    @Autowired
    StudentRepository studentRepository;
    @Autowired
    ProfessorRepository professorRepository;
    @Autowired
    CoordinatorRepository coordinatorRepository;

    UserRepository userRepository;

    @Autowired
    StudentController studentController;

    protected BCryptPasswordEncoder bCryptPasswordEncoder;



    public SignUpLogInController(BCryptPasswordEncoder bCryptPasswordEncoder){
        this.bCryptPasswordEncoder=bCryptPasswordEncoder;
    }
    /**
     *
     * @param user the user that must be authenticated
     * @return a response to whethere the login is successful or no.
     */
    public Response logIn(UserDTO user){
        String invalidRequestBody =validateUserLogIn(user);
        if(!invalidRequestBody.isEmpty()){
            throw new WebApplicationException(invalidRequestBody, Response.Status.BAD_REQUEST);
        }
        String invalidContent =validateUserContent(user);

         if (!invalidContent.isEmpty()){
             throw new WebApplicationException(invalidContent, Response.Status.BAD_REQUEST);
         }

         UserDTO responseUser = new UserDTO(userRepository.findByUserName(user.getUsername()));
         responseUser.setToken(JWTAuthenticationFilter.getToken(responseUser.getUsername()));

         return Response.status(Response.Status.OK).entity(responseUser).build();
    }

    public Response getCurrentUser(Principal principal) {
        if(principal == null) {
            return Response.status(Response.Status.OK).build();
        }

        String username = principal.getName();

        if(StringUtils.isNullOrEmpty(username)) {
            return Response.status(Response.Status.OK).build();
        }


        UserRepository userRepository = getUserRepository(username);
        WebUser user = userRepository.findByUserName(username);

        if (user == null) {
            return Response.status(Response.Status.OK).build();
        }

        return Response.status(Response.Status.OK).entity(getUserDTOResponse(user)).build();
    }

    private UserDTO getUserDTOResponse(WebUser user) {
        if(user == null)
            return null;

        Role userRole = user.getRole();

        if (userRole.equals(Role.STUDENT)) {
            return new StudentDTO(studentRepository.findByUserName(user.getUserName()));
        } else if (userRole.equals(Role.PROFESSOR)) {
            return new ProfessorDTO(professorRepository.findByUserName(user.getUserName()));
        }

        return new CoordinatorDTO(coordinatorRepository.findByUserName(user.getUserName()));
    }

    /**
     *
     * @param user to be validate upon loging in
     * @return a descriptive string of the error message that could be caused by the input
     */
    private String validateUserLogIn(UserDTO user){
        String returnMessage="";
        if(user==null){
            returnMessage=RequestErrorMessages.NO_USER;

        }else if(user.getUsername()==null){
            returnMessage=RequestErrorMessages.NO_USERNAME;
        }else if( user.getPassword()==null){
            returnMessage=RequestErrorMessages.NO_PASSWORD;
        }
        return returnMessage;
    }

    /**
     *
     * @param user which contents are to be validated
     * @return a descriptive string of the error message that could be caused by the input
     */
    private String validateUserContent(UserDTO user){
        WebUser userdb=null;
        Student studentdb=studentRepository.findByUserName(user.getUsername());
        Professor professordb = professorRepository.findByUserName(user.getUsername());
        Coordinator coordinatordb = coordinatorRepository.findByUserName(user.getUsername());

        if(studentdb!=null){
            userdb=studentdb;
            userRepository=studentRepository;
        }else if (professordb!=null){
            userdb=professordb;
            userRepository=professorRepository;
        }else{
            userRepository=coordinatorRepository;
            userdb=coordinatordb;
        }
            String returnMessage="";
        if(userdb==null){
            returnMessage= RequestErrorMessages.INVALID_USERNAME;
        }else if (!bCryptPasswordEncoder.matches(user.getPassword(),userdb.getPassword())) {
            returnMessage= RequestErrorMessages.INCORRECT_PASSWORD;
        }
        return returnMessage;
    }

    /**
     *
     * @param user to be validate upon siging up
     * @return a descriptive string of the error message that could be caused by the input
     */
    public Response signUp(UserDTO user){
        String invalidRequestBody = validateUserLogIn(user);
        if(!invalidRequestBody.isEmpty()){
            throw new WebApplicationException(invalidRequestBody, Response.Status.BAD_REQUEST);
        }

        String invalidContent =validateSignUpInput(user);

        if (!invalidContent.isEmpty()){
            throw new WebApplicationException(invalidContent, Response.Status.BAD_REQUEST);
        }


        if(user.getRole()==Role.STUDENT){
            userRepository=studentRepository;

        }else if (user.getRole()==Role.PROFESSOR){
            userRepository=professorRepository;

        }else{
            userRepository=coordinatorRepository;

        }
        String duplication = validateDuplication(user,userRepository);
        if (!duplication.isEmpty()){
            throw new WebApplicationException(duplication, Response.Status.BAD_REQUEST);
        }
        saveUser(user);



        return Response.status(Response.Status.CREATED).build();

    }

    private UserRepository getUserRepository(String username) {
        Student studentdb=studentRepository.findByUserName(username);
        Professor professordb = professorRepository.findByUserName(username);

        if(studentdb!=null){
            return studentRepository;
        }else if (professordb!=null){
            return professorRepository;
        }

        return coordinatorRepository;

    }

    /**
     *
     * @param user to be validate upon signing up
     * @return a descriptive message if there was an error or not
     */
    private String validateSignUpInput(UserDTO user) {
        String returnMessage="";
        if (!isEmailValid(user.getEmail())) {
            returnMessage= RequestErrorMessages.INVALID_EMAIL;
        } else if(user.getRole() == null || !Role.contains(user.getRole())) {
            returnMessage= RequestErrorMessages.INVALID_ROLE;
        } else if (StringUtils.isNullOrEmpty(user.getFirstname())) {
            returnMessage = RequestErrorMessages.NO_FIRST_NAME;
        } else if (StringUtils.isNullOrEmpty(user.getLastname())) {
            returnMessage = RequestErrorMessages.NO_LAST_NAME;
        }else if(user.getRole()==Role.STUDENT){
            returnMessage=studentController.validateStudent(user);
        }


        return returnMessage;

    }

    private String validateDuplication(UserDTO user,UserRepository userRepository){

            String returnMessage="";
        if (userRepository.findByUserName(user.getUsername()) != null){
            returnMessage = RequestErrorMessages.DUPLICATE_USERNAME;
        } else if (userRepository.findByEmail(user.getEmail()) != null){
            returnMessage = RequestErrorMessages.DUPLICATE_EMAIL;
        } else if (userRepository.findByIdentifier(user.getIdentifier()) != null){
            returnMessage = RequestErrorMessages.NO_IDENTIFIER;
        }
        return returnMessage;
    }
    private void saveUser(UserDTO user){
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        if(user.getRole()==Role.STUDENT){
            studentRepository.save(new Student(user));

        }else if(user.getRole()==Role.PROFESSOR){
            professorRepository.save(new Professor(user));
        }else {
            coordinatorRepository.save(new Coordinator(user));
        }
    }

    /**
     *
     * @param email to validate
     * @return true if the email is valid, false otherwise
     */
    public  boolean isEmailValid(String email)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if(email==null){
            return false;
        }
        return pat.matcher(email).matches();
    }
}
