package com.codewithpcodes.oikon.handler;

import java.util.Map;

public record ErrorResponse(
        Map<String, String> errors
) {
}
