package com.projectuntitled.common;

/** An account that just signed in, and whether this sign-in created it. */
public record LoginResult<T>(T account, boolean newAccount) {
}
