package com.example.utils;

/**
 * Consolidation of some constant strings
 */
public final class Const {
    // JWT token
    public final static String JWT_BLACK_LIST = "jwt:blacklist:";
    public final static String JWT_FREQUENCY = "jwt:frequency:";
    // Request rate limiting
    public final static String FLOW_LIMIT_COUNTER = "flow:counter:";
    public final static String FLOW_LIMIT_BLOCK = "flow:block:";
    // Email verification code
    public final static String VERIFY_EMAIL_LIMIT = "verify:email:limit:";
    public final static String VERIFY_EMAIL_DATA = "verify:email:data:";
    // Filter priority
    public final static int ORDER_FLOW_LIMIT = -101;
    public final static int ORDER_CORS = -102;
    // Custom request attributes
    public final static String ATTR_USER_ID = "userId";
    // Message queue
    public final static String MQ_MAIL = "mail";
    // User roles
    public final static String ROLE_DEFAULT = "user";
}

