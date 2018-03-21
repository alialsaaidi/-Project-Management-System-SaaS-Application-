package com.carleton.CapstoneSystem.Controllers;

import com.carleton.CapstoneSystem.DTO.UserDTO;
import com.carleton.CapstoneSystem.models.Program;
import com.carleton.CapstoneSystem.models.Student;
import com.carleton.CapstoneSystem.utils.RequestErrorMessages;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;

@Controller
public class StudentController extends SignUpLogInController {


    public StudentController(BCryptPasswordEncoder bCryptPasswordEncoder) {
        super(bCryptPasswordEncoder);
    }

    public String validateStudent(UserDTO user) {
        if(user.getProgram()==null || Program.contains(user.getProgram())){
            return RequestErrorMessages.INVALID_PROGRAM;

        }
        return "";
    }
}
