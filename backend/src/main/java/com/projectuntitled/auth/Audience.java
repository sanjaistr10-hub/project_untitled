package com.projectuntitled.auth;

/**
 * Who a token belongs to. Creators are platform-level accounts; members are scoped to a single
 * community (the same email on two creators' sites is two separate member accounts).
 */
public enum Audience {
    CREATOR,
    MEMBER
}
