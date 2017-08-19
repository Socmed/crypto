
package egl.http;

import egl.EagleException;
import org.json.simple.JSONStreamAware;

public final class ParameterException extends EagleException {

    private final JSONStreamAware errorResponse;

    ParameterException(JSONStreamAware errorResponse) {
        this.errorResponse = errorResponse;
    }

    JSONStreamAware getErrorResponse() {
        return errorResponse;
    }

}
