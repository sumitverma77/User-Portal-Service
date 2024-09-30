package com.security.user.service;

import com.security.user.Converter.UserConverter;
import com.security.user.dto.request.LoginRequest;
import com.security.user.dto.response.RegisterResponse;
import com.security.user.entity.User;
import com.security.user.exception.DuplicateUserNameException;
import com.security.user.repo.UserRepo;
import com.security.user.dto.request.RegisterRequest;
import com.security.user.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import static com.security.user.utils.AuthUtils.generateOtp;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    JWTService jwtService;
    @Autowired
    RedisTemplate<String , String> redisTemplate;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    EmailService emailService;
    public ResponseEntity<?> registerUser(RegisterRequest request) {

        if (userRepo.existsByUserNameAndIsActive(request.getEmail() , true)) {
            throw new DuplicateUserNameException("Username already exists: " + request.getEmail());
        }
        String otp = AuthUtils.generateOtp();
        emailService.sendOtpEmail(request.getEmail(), otp);
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        redisTemplate.opsForValue().set(request.getEmail(), otp, 10, TimeUnit.MINUTES);
        if(!userRepo.existsByUserNameAndIsActive(request.getEmail() , false)) {
            userRepo.save(UserConverter.toEntity(request));
        }
        return ResponseEntity.ok("OTP has been sent to : " + request.getEmail());
    }


    public String login(LoginRequest request)  {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
//        if (authentication == null) {
//            throw new UsernameNotFoundException("Invalid username or password");
//        }
        if(!authentication.isAuthenticated())
        {
             throw new UsernameNotFoundException("Username or password is incorrect");
        }

        return jwtService.generateToken(request.getEmail());

//        User user = userRepo.findByUserName(request.getEmail());
//        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
//            throw new UsernameNotFoundException("Invalid username or password");
//        }
//        return userDetailsService.loadUserByUsername(user.getUserName());
    }
    /*TODO: agr khud ka response bhejna ho
       jisme kuch extra attributes chahie to ye use kr lena
     */
//    public LoginResponse login2(LoginRequest request) {
//        Authentication authentication = authenticationManager
//                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
//        if (authentication == null) {
//            throw new UsernameNotFoundException("Invalid username or password");
//        }
//        return
//    }
}
