package unibuc.adrianaparaschivei.backend.service;

import org.springframework.stereotype.Service;

@Service
public class PasswordPolicyService {
    //la vulnerbail puteam avea o parola si de un caracter, acum sunt mai multe criterii precum:
//    -  12 caractere lungime minima
//    - sa contina litere mici
//    - sa contina litere mari
//    - sa contina cifre
//    - sa contina simboluri
    public boolean isStrongPassword(String password) {
        if (password == null || password.length() < 12) {
            return false;
        }

        boolean hasLowercase = false;
        boolean hasUppercase = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;

        for (int i = 0; i < password.length(); i++) {
            char currentChar = password.charAt(i);

            if (Character.isLowerCase(currentChar)) {
                hasLowercase = true;
            } else if (Character.isUpperCase(currentChar)) {
                hasUppercase = true;
            } else if (Character.isDigit(currentChar)) {
                hasDigit = true;
            } else {
                hasSymbol = true;
            }
        }

        return hasLowercase && hasUppercase && hasDigit && hasSymbol;
    }

    public String requirementsMessage() {
        return "Password must have at least 12 characters, lowercase, uppercase, digit and symbol.";
    }
}
