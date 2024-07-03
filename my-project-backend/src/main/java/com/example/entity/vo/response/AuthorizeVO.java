package com.example.entity.vo.response;

import lombok.Data;

import java.util.Date;

/**
 * User information response upon successful login verification
 */

@Data
public class AuthorizeVO {
    String username;
    String role;
    String token;
    Date expire;
}
